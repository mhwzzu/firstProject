package com.mhw.journey.service;

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
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RecommendationService {
    private final PreferenceProfileRepository profiles; private final RecommendationRunRepository runs;
    private final RecommendationCandidateRepository candidates; private final PlaceRepository places;
    private final PlaceSearchService placeSearch; private final WeatherService weather;

    public RecommendationService(PreferenceProfileRepository profiles, RecommendationRunRepository runs, RecommendationCandidateRepository candidates, PlaceRepository places, PlaceSearchService placeSearch, WeatherService weather) {
        this.profiles=profiles;this.runs=runs;this.candidates=candidates;this.places=places;this.placeSearch=placeSearch;this.weather=weather;
    }

    public RecommendationResponse today(Long spaceId, DecisionQuery query) {
        List<PreferenceProfile> members=members(spaceId); PreferenceProfile primary=members.get(0);
        String city=clean(query.getCity(),primary.getCity()); WeatherService.Snapshot snapshot=weather.current(city);
        String keyword=keyword(members,query,"餐厅");
        RecommendationRun run=run(spaceId,"TODAY",city,context(query,snapshot.label),6);
        List<PlaceSearchResult> found=placeSearch.search(keyword,city);
        if(found.isEmpty()) found=fallbackPlaces(city,keyword);
        List<RecommendationResponse.Candidate> output=new ArrayList<>(); int index=0;
        for(PlaceSearchResult result:found){
            if(index++>=6) break;
            Place place=upsert(result,city,keyword);
            int score=Math.max(70,95-index*4+(snapshot.live?2:0)-budgetPenalty(query,primary));
            RecommendationCandidate saved=candidates.save(candidate(run,result.getName(),city,"吃喝 · "+keyword,null,score,
                    reason(members,query,snapshot.label,"当前时段适合就近安排"),"偏好 30/30 · 时间天气 18/20 · 可达性 19/20 · 预算与营业 13/15 · 新鲜度 14/15",
                    isFallback(result)?"本地降级地点（请配置高德 Web 服务 Key）":"高德地点搜索",mapUrl(result),estimate(query,primary)));
            saved.setPlaceId(place.getId()); candidates.save(saved);
            output.add(view(saved,result,query));
        }
        return response(run,snapshot.label,snapshot.status,query,output);
    }

    public RecommendationResponse weekend(Long spaceId, DecisionQuery query) {
        List<PreferenceProfile> members=members(spaceId); PreferenceProfile primary=members.get(0);
        String city=clean(query.getCity(),primary.getCity()); WeatherService.Snapshot snapshot=weather.current(city);
        String keyword=keyword(members,query,"周末去处");
        RecommendationRun run=run(spaceId,"WEEKEND",city,context(query,snapshot.label),24);
        List<PlaceSearchResult> found=placeSearch.search(keyword,city);
        if(found.isEmpty()) found=fallbackPlaces(city,keyword);
        List<RecommendationResponse.Candidate> output=new ArrayList<>(); int index=0;
        for(PlaceSearchResult result:found){
            if(index++>=6) break;
            int score=Math.max(68,93-index*4-budgetPenalty(query,primary));
            RecommendationCandidate saved=candidates.save(candidate(run,result.getName(),city,"下个周末 · "+keyword,seasonLabel(),score,
                    reason(members,query,snapshot.label,"适合安排成一个可调整的周末计划"),"偏好 28/30 · 季节日期 22/25 · 可达性 17/20 · 预算 13/15 · 新鲜度 9/10",
                    isFallback(result)?"本地降级地点（请配置高德 Web 服务 Key）":"高德地点搜索",mapUrl(result),estimate(query,primary)));
            Place place=upsert(result,city,keyword); saved.setPlaceId(place.getId()); candidates.save(saved);
            output.add(view(saved,result,query));
        }
        return response(run,snapshot.label,snapshot.status,query,output);
    }

    private List<PreferenceProfile> members(Long spaceId) { List<PreferenceProfile> all=profiles.findBySpaceId(spaceId); if(all.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT,"请先完成偏好设置"); return all; }
    private RecommendationRun run(Long spaceId,String type,String city,String context,int hours){RecommendationRun run=new RecommendationRun();run.setSpaceId(spaceId);run.setType(type);run.setCity(city);run.setRulesVersion("v1.1-decision-map");run.setContextJson(context);run.setExpiresAt(LocalDateTime.now().plusHours(hours));return runs.save(run);}
    private RecommendationCandidate candidate(RecommendationRun run,String title,String city,String category,String bestSeason,int score,String reason,String breakdown,String source,String url,String estimate){RecommendationCandidate c=new RecommendationCandidate();c.setRunId(run.getId());c.setTitle(title);c.setCity(city);c.setCategory(category);c.setBestSeason(bestSeason);c.setScore(score);c.setReason(reason);c.setScoreBreakdown(breakdown);c.setSourceLabel(source);c.setSourceUrl(url);c.setEstimate(estimate);return c;}
    private Place upsert(PlaceSearchResult input,String city,String category){String external=input.getId();Place place=places.findBySourceAndExternalId("amap",external).orElseGet(Place::new);place.setSource("amap");place.setExternalId(external);place.setTitle(input.getName());place.setCity(clean(input.getCity(),city));place.setDistrict(input.getDistrict());place.setAddress(input.getAddress());place.setLatitude(input.getLatitude());place.setLongitude(input.getLongitude());place.setCategory(category);return places.save(place);}
    private RecommendationResponse response(RecommendationRun run,String weatherLabel,String weatherStatus,DecisionQuery query,List<RecommendationResponse.Candidate> list){
        String status=weatherStatus+(query.hasOrigin()?" · 已按当前位置估算距离":" · 未获取定位，已按城市中心估算距离");
        return new RecommendationResponse(run.getType(),run.getCity(),weatherLabel,status,querySummary(query),query.hasOrigin()?"当前位置":run.getCity()+"市中心",run.getGeneratedAt(),list);
    }
    private RecommendationResponse.Candidate view(RecommendationCandidate saved,PlaceSearchResult place,DecisionQuery query){
        Double lat=place.getLatitude(),lon=place.getLongitude(); double[] origin=query.hasOrigin()?new double[]{query.getLatitude(),query.getLongitude()}:cityCenter(saved.getCity());
        PlaceSearchService.RouteSnapshot route=lat==null||lon==null?null:placeSearch.drivingRoute(origin[0],origin[1],lat,lon);
        Double distance=route==null?(lat==null||lon==null?null:round(haversine(origin[0],origin[1],lat,lon))):round(route.getMeters()/1000d);
        Integer minutes=route==null?(distance==null?null:Math.max(5,(int)Math.ceil(distance*3.2+5))):Math.max(1,(int)Math.ceil(route.getSeconds()/60d));
        String routeStatus=route==null?"距离与车程为直线距离估算":"高德驾车路径数据";
        return new RecommendationResponse.Candidate(saved.getId(),saved.getTitle(),saved.getCity(),saved.getCategory(),saved.getBestSeason(),saved.getScore(),saved.getReason(),saved.getScoreBreakdown(),saved.getSourceLabel(),saved.getSourceUrl(),saved.getEstimate(),place.getDistrict(),place.getAddress(),lat,lon,distance,minutes,routeStatus,navigationUrl(place));
    }
    private List<PlaceSearchResult> fallbackPlaces(String city,String keyword){double[] c=cityCenter(city);List<PlaceSearchResult> list=new ArrayList<>();for(int i=1;i<=6;i++){double offset=(i-3)*.012;list.add(new PlaceSearchResult("fallback-"+city+"-"+keyword+"-"+i,city+" · "+keyword+"推荐 "+i,"",city,"","未配置地图服务，地点为示例位置",c[0]+offset,c[1]-offset));}return list;}
    private String keyword(List<PreferenceProfile> people,DecisionQuery query,String fallback){String explicit=firstTag(query.getTags(),"");if(!explicit.isEmpty())return explicit;String prompt=firstTag(query.getPrompt(),"");if(!prompt.isEmpty()&&!looksLikeSentence(prompt))return prompt;return firstTag(combined(people,"food"),fallback);}
    private boolean looksLikeSentence(String value){return value.length()>12||value.contains("想")||value.contains("去")||value.contains("周末");}
    private String combined(List<PreferenceProfile> profiles,String field){StringBuilder b=new StringBuilder();for(PreferenceProfile p:profiles){String v="food".equals(field)?p.getFoodTags():p.getTravelTags();if(v!=null&&!v.trim().isEmpty()){if(b.length()>0)b.append("、");b.append(v);}}return b.toString();}
    private String firstTag(String tags,String fallback){if(tags==null||tags.trim().isEmpty())return fallback;String result=tags.split("[,，、/| ]")[0].trim();return result.isEmpty()?fallback:result;}
    private String reason(List<PreferenceProfile> people,DecisionQuery query,String weather,String tail){String preferences=combined(people,"food");if(preferences.trim().isEmpty())preferences="你们设置的默认探索偏好";String prompt=query.getPrompt()==null||query.getPrompt().trim().isEmpty()?"":("；已纳入“"+truncate(query.getPrompt(),40)+"”");return "结合双方偏好（"+preferences+"）、"+weather+"和"+tail+prompt+"。";}
    private int budgetPenalty(DecisionQuery query,PreferenceProfile profile){Integer budget=query.getBudget()==null?profile.getBudget():query.getBudget();return budget!=null&&budget<50?5:0;}
    private String estimate(DecisionQuery query,PreferenceProfile profile){Integer budget=query.getBudget()==null?profile.getBudget():query.getBudget();return budget==null?"预算可再确认":"预计不高于 ¥"+budget;}
    private String seasonLabel(){int month=java.time.LocalDate.now().getMonthValue();return month>=3&&month<=5?"春季正当时":month>=6&&month<=8?"夏季出发":"适合周末慢游";}
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
