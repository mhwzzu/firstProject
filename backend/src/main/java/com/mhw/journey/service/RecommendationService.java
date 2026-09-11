package com.mhw.journey.service;

import com.mhw.journey.discovery.DiscoveryBatch;
import com.mhw.journey.dto.DecisionQuery;
import com.mhw.journey.dto.PlaceSearchResult;
import com.mhw.journey.dto.RecommendationResponse;
import com.mhw.journey.model.*;
import com.mhw.journey.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RecommendationService {
    private final PreferenceProfileRepository profiles;
    private final RecommendationRunRepository runs;
    private final RecommendationCandidateRepository candidates;
    private final PlaceRepository places;
    private final CandidateFeedbackRepository feedback;
    private final RecommendationEvidenceRepository evidence;
    private final PlaceSearchService placeSearch;
    private final WeatherService weather;
    private final ContentDiscoveryService discovery;

    public RecommendationService(PreferenceProfileRepository profiles, RecommendationRunRepository runs,
                                 RecommendationCandidateRepository candidates, PlaceRepository places,
                                 CandidateFeedbackRepository feedback, RecommendationEvidenceRepository evidence,
                                 PlaceSearchService placeSearch, WeatherService weather, ContentDiscoveryService discovery) {
        this.profiles=profiles; this.runs=runs; this.candidates=candidates; this.places=places; this.feedback=feedback;
        this.evidence=evidence; this.placeSearch=placeSearch; this.weather=weather; this.discovery=discovery;
    }

    public RecommendationResponse today(Long spaceId, DecisionQuery query, boolean forceRefresh) {
        return recommend(spaceId, "TODAY", query, forceRefresh);
    }

    public RecommendationResponse weekend(Long spaceId, DecisionQuery query, boolean forceRefresh) {
        return recommend(spaceId, "WEEKEND", query, forceRefresh);
    }

    private RecommendationResponse recommend(Long spaceId, String type, DecisionQuery query, boolean forceRefresh) {
        List<PreferenceProfile> members = members(spaceId);
        PreferenceProfile primary = members.get(0);
        String city = clean(query.getCity(), primary.getCity());
        WeatherService.Snapshot snapshot = weather.current(city);
        String keyword = keyword(members, query, "TODAY".equals(type) ? "餐厅" : "周末去处");
        DiscoveryBatch batch = discovery.discover(spaceId, type, city, query.getPrompt(), query.getTags(), forceRefresh);
        RecommendationRun run = run(spaceId, type, city, context(query, snapshot.label), "TODAY".equals(type) ? 6 : 24);

        List<PlaceSearchResult> found = discoverPlaces(batch, city, keyword);
        if (found.isEmpty()) found = fallbackPlaces(city, keyword);
        rotate(found, query.getVariation());

        List<RecommendationResponse.Candidate> output = new ArrayList<>();
        int index = 0;
        for (PlaceSearchResult result : found) {
            if (index >= 6) break;
            int position = ++index;
            String category = ("TODAY".equals(type) ? "吃喝 · " : "周末 · ") + keyword;
            int base = ("TODAY".equals(type) ? 95 : 93) - position * 4 + (snapshot.live ? 2 : 0) - budgetPenalty(query, primary);
            int learning = preferenceSignal(members, result.getName());
            int score = Math.max(62, Math.min(99, base + learning));
            List<DiscoveryRecord> related = isFallback(result) ? Collections.<DiscoveryRecord>emptyList() : relatedEvidence(batch.getRecords(), result.getName(), position);
            String sourceLabel = isFallback(result) ? "本地降级地点" : related.isEmpty() ? "高德地点搜索" : "高德地点 · 近期公开内容";
            String reason = reason(members, query, snapshot.label,
                    "WEEKEND".equals(type) ? "适合安排成一个可调整的周末计划" : "当前时段适合就近安排",
                    related, learning);
            String breakdown = "双方偏好 30 · 时间天气 " + (snapshot.live ? "20" : "15") + " · 可达性 20 · 新鲜来源 " + (related.isEmpty() ? "0" : "15") + " · 行为学习 " + signed(learning);
            RecommendationCandidate saved = candidates.save(candidate(run, result.getName(), city, category,
                    "WEEKEND".equals(type) ? seasonLabel() : null, score, reason, breakdown, sourceLabel,
                    mapUrl(result), estimate(query, primary)));
            Place place = upsert(result, city, keyword);
            saved.setPlaceId(place.getId());
            candidates.save(saved);
            List<RecommendationResponse.Evidence> evidenceViews = saveEvidence(saved.getId(), related, result.getName());
            output.add(view(saved, result, query, evidenceViews));
        }
        return response(run, snapshot, batch, query, output);
    }

    private List<PlaceSearchResult> discoverPlaces(DiscoveryBatch batch, String city, String keyword) {
        LinkedHashMap<String, PlaceSearchResult> unique = new LinkedHashMap<>();
        for (PlaceSearchResult result : placeSearch.search(keyword, city)) unique.put(result.getId(), result);
        int attempts = 0;
        for (DiscoveryRecord record : batch.getRecords()) {
            if (attempts >= 3) break;
            if (record.getPlaceKeyword() == null || record.getPlaceKeyword().trim().isEmpty()) continue;
            attempts++;
            List<PlaceSearchResult> matches = placeSearch.search(record.getPlaceKeyword(), city);
            if (!matches.isEmpty()) unique.put(matches.get(0).getId(), matches.get(0));
        }
        return new ArrayList<>(unique.values());
    }

    private List<DiscoveryRecord> relatedEvidence(List<DiscoveryRecord> records, String placeTitle, int position) {
        List<DiscoveryRecord> direct = new ArrayList<>();
        String needle = normalize(placeTitle);
        for (DiscoveryRecord item : records) {
            String haystack = normalize(item.getTitle() + " " + nullToEmpty(item.getSnippet()));
            if (!needle.isEmpty() && haystack.contains(needle)) direct.add(item);
            if (direct.size() == 3) return direct;
        }
        if (!direct.isEmpty()) return direct;
        if (records.isEmpty()) return direct;
        int start = Math.floorMod(position - 1, records.size());
        Set<String> usedPlatforms=new HashSet<>();
        for (int i=0; i<records.size() && direct.size()<3; i++) {DiscoveryRecord candidate=records.get((start+i)%records.size());if(usedPlatforms.add(candidate.getPlatform()))direct.add(candidate);}
        return direct;
    }

    private List<RecommendationResponse.Evidence> saveEvidence(Long candidateId, List<DiscoveryRecord> records, String placeTitle) {
        List<RecommendationResponse.Evidence> views = new ArrayList<>();
        String needle = normalize(placeTitle);
        for (DiscoveryRecord record : records) {
            boolean direct = normalize(record.getTitle() + " " + nullToEmpty(record.getSnippet())).contains(needle);
            RecommendationEvidence item = new RecommendationEvidence(); item.setCandidateId(candidateId);
            item.setPlatform(record.getPlatform()); item.setTitle(record.getTitle()); item.setSummary(record.getSnippet());
            item.setSourceUrl(record.getSourceUrl()); item.setRelationLabel(direct ? "提及此地点" : "同主题公开灵感");
            item.setPublishedAt(record.getPublishedAt()); item.setDiscoveredAt(record.getDiscoveredAt()); evidence.save(item);
            views.add(new RecommendationResponse.Evidence(item.getPlatform(), item.getTitle(), item.getSummary(), item.getSourceUrl(),
                    item.getRelationLabel(), item.getPublishedAt(), item.getDiscoveredAt()));
        }
        return views;
    }

    private int preferenceSignal(List<PreferenceProfile> members, String title) {
        int score = 0;
        for (PreferenceProfile member : members) {
            for (CandidateFeedback signal : feedback.findByUserId(member.getUserId())) {
                Optional<RecommendationCandidate> previous = candidates.findById(signal.getCandidateId());
                if (!previous.isPresent() || !similar(previous.get().getTitle(), title)) continue;
                if ("WANT".equals(signal.getAction()) || "VISITED".equals(signal.getAction())) score += 4;
                if ("SKIP".equals(signal.getAction())) score -= 5;
            }
        }
        return Math.max(-10, Math.min(10, score));
    }

    private boolean similar(String left, String right) {
        String a=normalize(left), b=normalize(right); return !a.isEmpty() && (a.equals(b) || a.contains(b) || b.contains(a));
    }
    private String normalize(String value) { return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[\\s·•|｜_—-]", ""); }
    private void rotate(List<PlaceSearchResult> found, Integer variation) {
        if (found.size() > 1 && variation != null && variation > 0) Collections.rotate(found, -(variation % found.size()));
    }

    private List<PreferenceProfile> members(Long spaceId) {
        List<PreferenceProfile> all=profiles.findBySpaceId(spaceId);
        if(all.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT,"请先完成偏好设置");
        return all;
    }
    private RecommendationRun run(Long spaceId,String type,String city,String context,int hours){
        RecommendationRun run=new RecommendationRun(); run.setSpaceId(spaceId); run.setType(type); run.setCity(city);
        run.setRulesVersion("v1.2-live-discovery"); run.setContextJson(context); run.setExpiresAt(LocalDateTime.now().plusHours(hours)); return runs.save(run);
    }
    private RecommendationCandidate candidate(RecommendationRun run,String title,String city,String category,String bestSeason,int score,String reason,String breakdown,String source,String url,String estimate){
        RecommendationCandidate c=new RecommendationCandidate(); c.setRunId(run.getId()); c.setTitle(title); c.setCity(city); c.setCategory(category);
        c.setBestSeason(bestSeason); c.setScore(score); c.setReason(reason); c.setScoreBreakdown(breakdown); c.setSourceLabel(source);
        c.setSourceUrl(url); c.setEstimate(estimate); return c;
    }
    private Place upsert(PlaceSearchResult input,String city,String category){
        String external=input.getId(); Place place=places.findBySourceAndExternalId("amap",external).orElseGet(Place::new);
        place.setSource("amap"); place.setExternalId(external); place.setTitle(input.getName()); place.setCity(clean(input.getCity(),city));
        place.setDistrict(input.getDistrict()); place.setAddress(input.getAddress()); place.setLatitude(input.getLatitude());
        place.setLongitude(input.getLongitude()); place.setCategory(category); return places.save(place);
    }
    private RecommendationResponse response(RecommendationRun run,WeatherService.Snapshot weatherSnapshot,DiscoveryBatch batch,
                                            DecisionQuery query,List<RecommendationResponse.Candidate> list){
        String mapStatus=weatherSnapshot.status+(query.hasOrigin()?" · 已按当前位置计算距离":" · 未获取定位，按城市中心估算距离");
        String status=mapStatus+" · "+batch.getMessage();
        return new RecommendationResponse(run.getType(),run.getCity(),weatherSnapshot.label,status,querySummary(query),
                query.hasOrigin()?"当前位置":run.getCity()+"市中心",run.getGeneratedAt(),batch.getStatus(),batch.getConnectedSources(),
                batch.getUnavailableSources(),batch.getRefreshedAt(),list);
    }
    private RecommendationResponse.Candidate view(RecommendationCandidate saved,PlaceSearchResult place,DecisionQuery query,
                                                   List<RecommendationResponse.Evidence> evidenceViews){
        Double lat=place.getLatitude(),lon=place.getLongitude(); double[] origin=query.hasOrigin()?new double[]{query.getLatitude(),query.getLongitude()}:cityCenter(saved.getCity());
        PlaceSearchService.RouteSnapshot route=lat==null||lon==null?null:placeSearch.drivingRoute(origin[0],origin[1],lat,lon);
        Double distance=route==null?(lat==null||lon==null?null:round(haversine(origin[0],origin[1],lat,lon))):round(route.getMeters()/1000d);
        Integer minutes=route==null?(distance==null?null:Math.max(5,(int)Math.ceil(distance*3.2+5))):Math.max(1,(int)Math.ceil(route.getSeconds()/60d));
        String routeStatus=route==null?"距离与车程为直线距离估算":"高德驾车路径数据";
        return new RecommendationResponse.Candidate(saved.getId(),saved.getTitle(),saved.getCity(),saved.getCategory(),saved.getBestSeason(),
                saved.getScore(),saved.getReason(),saved.getScoreBreakdown(),saved.getSourceLabel(),saved.getSourceUrl(),saved.getEstimate(),
                place.getDistrict(),place.getAddress(),lat,lon,distance,minutes,routeStatus,navigationUrl(place),evidenceViews);
    }
    private List<PlaceSearchResult> fallbackPlaces(String city,String keyword){
        double[] c=cityCenter(city); List<PlaceSearchResult> list=new ArrayList<>();
        for(int i=1;i<=6;i++){double offset=(i-3)*.012; list.add(new PlaceSearchResult("fallback-"+city+"-"+keyword+"-"+i,
                city+" · "+keyword+"灵感 "+i,"",city,"","地图实时检索暂不可用，当前为可替换的示意位置",c[0]+offset,c[1]-offset));}
        return list;
    }
    private String keyword(List<PreferenceProfile> people,DecisionQuery query,String fallback){
        String explicit=firstTag(query.getTags(),""); if(!explicit.isEmpty())return mapKeyword(explicit);
        String prompt=firstTag(query.getPrompt(),""); if(!prompt.isEmpty()&&!looksLikeSentence(prompt))return mapKeyword(prompt);
        String preferences=combined(people,"food"); if (preferences.trim().isEmpty()) preferences=combined(people,"activity");
        return mapKeyword(firstTag(preferences,fallback));
    }
    private String mapKeyword(String input){String value=input==null?"":input.trim();if(value.matches(".*(秋景|赏花|自然|风景|户外|爬山).*"))return "景点";if(value.matches(".*(散步|公园|绿道).*"))return "公园";if(value.matches(".*(展览|博物|艺术).*"))return "博物馆";if(value.matches(".*(暖食|吃|美食|晚餐|餐厅).*"))return "餐厅";return value;}
    private boolean looksLikeSentence(String value){return value.length()>12||value.contains("想")||value.contains("去")||value.contains("周末");}
    private String combined(List<PreferenceProfile> people,String field){
        StringBuilder b=new StringBuilder(); for(PreferenceProfile p:people){String v="food".equals(field)?p.getFoodTags():p.getActivityTags();
        if(v!=null&&!v.trim().isEmpty()){if(b.length()>0)b.append("、");b.append(v);}} return b.toString();
    }
    private String firstTag(String tags,String fallback){if(tags==null||tags.trim().isEmpty())return fallback;String result=tags.split("[,，、/| ]")[0].trim();return result.isEmpty()?fallback:result;}
    private String reason(List<PreferenceProfile> people,DecisionQuery query,String weather,String tail,List<DiscoveryRecord> related,int learning){
        String preferences=combined(people,"food"); if(preferences.trim().isEmpty())preferences="你们设置的探索偏好";
        String prompt=query.getPrompt()==null||query.getPrompt().trim().isEmpty()?"":("；已纳入“"+truncate(query.getPrompt(),40)+"”");
        String sources=related.isEmpty()?"":"；参考了 "+platforms(related)+" 的公开索引内容";
        String learned=learning>0?"；你们过去对相似地点有过心动记录":learning<0?"；已降低曾跳过的相似地点权重":"";
        return "结合双方偏好（"+preferences+"）、"+weather+"和"+tail+prompt+sources+learned+"。";
    }
    private String platforms(List<DiscoveryRecord> records){LinkedHashSet<String> labels=new LinkedHashSet<>();for(DiscoveryRecord r:records)labels.add(r.getPlatform());return String.join("、",labels);}
    private String signed(int value){return value>0?"+"+value:String.valueOf(value);}
    private int budgetPenalty(DecisionQuery query,PreferenceProfile profile){Integer budget=query.getBudget()==null?profile.getBudget():query.getBudget();return budget!=null&&budget<50?5:0;}
    private String estimate(DecisionQuery query,PreferenceProfile profile){Integer budget=query.getBudget()==null?profile.getBudget():query.getBudget();return budget==null?"预算可再确认":"预计不高于 ¥"+budget;}
    private String seasonLabel(){int month=LocalDate.now().getMonthValue();return month>=3&&month<=5?"春季正当时":month>=6&&month<=8?"夏季出发":month>=9&&month<=11?"秋季正当时":"适合冬日慢游";}
    private String querySummary(DecisionQuery q){List<String> parts=new ArrayList<>();if(q.getPrompt()!=null&&!q.getPrompt().trim().isEmpty())parts.add("“"+truncate(q.getPrompt(),32)+"”");if(q.getTags()!=null&&!q.getTags().trim().isEmpty())parts.add(q.getTags());if(q.getBudget()!=null)parts.add("¥"+q.getBudget()+"以内");if(q.getTravelMinutes()!=null)parts.add(q.getTravelMinutes()+" 分钟内");return parts.isEmpty()?"根据你们已保存的偏好生成":String.join(" · ",parts);}
    private String context(DecisionQuery q,String weather){return "{\"weather\":\""+escape(weather)+"\",\"prompt\":\""+escape(nullToEmpty(q.getPrompt()))+"\",\"tags\":\""+escape(nullToEmpty(q.getTags()))+"\"}";}
    private String navigationUrl(PlaceSearchResult p){if(p.getLongitude()==null||p.getLatitude()==null)return null;return "https://uri.amap.com/navigation?to="+p.getLongitude()+","+p.getLatitude()+","+urlEncode(p.getName())+"&mode=car&coordinate=gaode";}
    private String mapUrl(PlaceSearchResult p){if(p.getLongitude()==null||p.getLatitude()==null)return null;return "https://uri.amap.com/marker?position="+p.getLongitude()+","+p.getLatitude()+"&name="+urlEncode(p.getName());}
    private String urlEncode(String value){try{return URLEncoder.encode(value,"UTF-8");}catch(UnsupportedEncodingException e){return value;}}
    private boolean isFallback(PlaceSearchResult p){return p.getId()!=null&&p.getId().startsWith("fallback-");}
    private String clean(String requested,String fallback){return requested==null||requested.trim().isEmpty()?fallback:requested.trim();}
    private String truncate(String value,int max){String clean=value.trim();return clean.length()<=max?clean:clean.substring(0,max)+"…";}
    private String nullToEmpty(String value){return value==null?"":value;}
    private String escape(String value){return value.replace("\\","\\\\").replace("\"","\\\"");}
    private double[] cityCenter(String city){if(city!=null&&city.contains("杭州"))return new double[]{30.2741,120.1551};if(city!=null&&city.contains("上海"))return new double[]{31.2304,121.4737};if(city!=null&&city.contains("北京"))return new double[]{39.9042,116.4074};if(city!=null&&city.contains("广州"))return new double[]{23.1291,113.2644};if(city!=null&&city.contains("成都"))return new double[]{30.5728,104.0668};return new double[]{30.2741,120.1551};}
    private double haversine(double aLat,double aLon,double bLat,double bLon){double dLat=Math.toRadians(bLat-aLat),dLon=Math.toRadians(bLon-aLon);double x=Math.sin(dLat/2)*Math.sin(dLat/2)+Math.cos(Math.toRadians(aLat))*Math.cos(Math.toRadians(bLat))*Math.sin(dLon/2)*Math.sin(dLon/2);return 6371*2*Math.atan2(Math.sqrt(x),Math.sqrt(1-x));}
    private Double round(double value){return Math.round(value*10d)/10d;}
}
