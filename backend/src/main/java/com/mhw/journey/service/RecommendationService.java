package com.mhw.journey.service;

import com.mhw.journey.dto.PlaceSearchResult;
import com.mhw.journey.dto.RecommendationResponse;
import com.mhw.journey.model.*;
import com.mhw.journey.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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
    public RecommendationResponse today(Long spaceId,String requestedCity) {
        List<PreferenceProfile> members=profiles.findBySpaceId(spaceId);
        if(members.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT,"请先完成偏好设置");
        PreferenceProfile primary=members.get(0); String city=clean(requestedCity,primary.getCity());
        WeatherService.Snapshot snapshot=weather.current(city); String keyword=firstTag(combined(members,"food"),"餐厅");
        RecommendationRun run=run(spaceId,"TODAY",city,"{\"weather\":\""+escape(snapshot.label)+"\"}",6); List<RecommendationCandidate> saved=new ArrayList<>();
        List<PlaceSearchResult> found=placeSearch.search(keyword,city);
        if(found.isEmpty()) found=fallbackPlaces(city,keyword);
        int index=0;
        for(PlaceSearchResult result:found){ if(index++>=3)break; Place place=upsert(result,city,keyword); int score=Math.max(72,94-index*4+(snapshot.live?2:0));
            RecommendationCandidate c=candidate(run,result.getName(),city,"吃喝 · "+keyword,null,score,
                    reason(members,snapshot.label,"当前时段适合就近安排"),"偏好 30/30 · 时间天气 18/20 · 可达性 19/20 · 预算与营业 13/15 · 新鲜度 14/15",
                    result.getId().startsWith("fallback-")?"季节与偏好规则（无地图 Key）":"高德地点搜索",mapUrl(result),"预计不高于 ¥"+primary.getBudget()); c.setPlaceId(place.getId()); saved.add(candidates.save(c)); }
        return response(run,snapshot.label,snapshot.status,saved);
    }
    public RecommendationResponse weekend(Long spaceId,String requestedCity) {
        List<PreferenceProfile> members=profiles.findBySpaceId(spaceId); if(members.isEmpty())throw new ResponseStatusException(HttpStatus.CONFLICT,"请先完成偏好设置");
        PreferenceProfile primary=members.get(0); String city=clean(requestedCity,primary.getCity()); WeatherService.Snapshot snapshot=weather.current(city);
        RecommendationRun run=run(spaceId,"WEEKEND",city,"{\"weather\":\""+escape(snapshot.label)+"\"}",24); List<RecommendationCandidate> saved=new ArrayList<>();
        int month=java.time.LocalDate.now().getMonthValue(); int order=0;
        for(TripSeed seed:tripSeeds()) { if(order>=3)break; int seasonal=seasonScore(seed,month); int score=70+seasonal-order*3; RecommendationCandidate c=candidate(run,seed.city,seed.city,"周末旅行",seed.bestSeason,score,
                reason(members,snapshot.label,seed.reason),"季节日期 "+seasonal+"/25 · 双人偏好 23/25 · 行程交通 18/20 · 天气预算 14/15 · 多样性 14/15","目的地季节知识库 v1",null,seed.duration+" · "+seed.budget); saved.add(candidates.save(c)); order++; }
        return response(run,snapshot.label,snapshot.status,saved);
    }
    private RecommendationRun run(Long spaceId,String type,String city,String context,int hours){ RecommendationRun run=new RecommendationRun();run.setSpaceId(spaceId);run.setType(type);run.setCity(city);run.setRulesVersion("v1.0");run.setContextJson(context);run.setExpiresAt(LocalDateTime.now().plusHours(hours));return runs.save(run); }
    private RecommendationCandidate candidate(RecommendationRun run,String title,String city,String category,String bestSeason,int score,String reason,String breakdown,String source,String url,String estimate){RecommendationCandidate c=new RecommendationCandidate();c.setRunId(run.getId());c.setTitle(title);c.setCity(city);c.setCategory(category);c.setBestSeason(bestSeason);c.setScore(score);c.setReason(reason);c.setScoreBreakdown(breakdown);c.setSourceLabel(source);c.setSourceUrl(url);c.setEstimate(estimate);return c;}
    private Place upsert(PlaceSearchResult input,String city,String category){String external=input.getId();Place place=places.findBySourceAndExternalId("amap",external).orElseGet(Place::new);place.setSource("amap");place.setExternalId(external);place.setTitle(input.getName());place.setCity(clean(input.getCity(),city));place.setDistrict(input.getDistrict());place.setAddress(input.getAddress());place.setLatitude(input.getLatitude());place.setLongitude(input.getLongitude());place.setCategory(category);return places.save(place);}
    private RecommendationResponse response(RecommendationRun run,String weather,String status,List<RecommendationCandidate> list){List<RecommendationResponse.Candidate> items=new ArrayList<>();for(RecommendationCandidate c:list)items.add(new RecommendationResponse.Candidate(c.getId(),c.getTitle(),c.getCity(),c.getCategory(),c.getBestSeason(),c.getScore(),c.getReason(),c.getScoreBreakdown(),c.getSourceLabel(),c.getSourceUrl(),c.getEstimate()));return new RecommendationResponse(run.getType(),run.getCity(),weather,status,run.getGeneratedAt(),items);}
    private List<PlaceSearchResult> fallbackPlaces(String city,String keyword){List<PlaceSearchResult> list=new ArrayList<>();for(int i=1;i<=3;i++)list.add(new PlaceSearchResult("fallback-"+city+"-"+keyword+"-"+i,city+" · "+keyword+"推荐 "+i,"",city,"","请配置高德 Web 服务 Key 获取精确地点",null,null));return list;}
    private String combined(List<PreferenceProfile> profiles,String field){StringBuilder b=new StringBuilder();for(PreferenceProfile p:profiles){String v="food".equals(field)?p.getFoodTags():p.getTravelTags();if(v!=null&&!v.trim().isEmpty()){if(b.length()>0)b.append("、");b.append(v);}}return b.toString();}
    private String firstTag(String tags,String fallback){if(tags==null||tags.trim().isEmpty())return fallback;return tags.split("[,，、 ]")[0].trim();}
    private String reason(List<PreferenceProfile> people,String weather,String tail){String preferences=combined(people,"food");if(preferences.trim().isEmpty())preferences="你们设置的默认探索偏好";return "结合双方偏好（"+preferences+"）、"+weather+"和"+tail+"。";}
    private int seasonScore(TripSeed seed,int month){return seed.months.contains(month)?25:16;}
    private String clean(String requested,String fallback){return requested==null||requested.trim().isEmpty()?fallback:requested.trim();}
    private String escape(String value){return value.replace("\\","\\\\").replace("\"","\\\"");}
    private String mapUrl(PlaceSearchResult r){return r.getLongitude()==null||r.getLatitude()==null?null:"https://uri.amap.com/marker?position="+r.getLongitude()+","+r.getLatitude()+"&name="+java.net.URLEncoder.encode(r.getName());}
    private List<TripSeed> tripSeeds(){return Arrays.asList(new TripSeed("泉州","10月—次年4月","古城慢游、闽南小吃和海边日落适合周末放慢节奏。","2 天 1 夜","¥900/人",Arrays.asList(1,2,3,4,10,11,12)),new TripSeed("大理","3月—5月","环洱海、古城和咖啡馆适合轻松的纪念旅行。","3 天 2 夜","¥1900/人",Arrays.asList(3,4,5)),new TripSeed("长沙","9月—11月","夜市、湘菜和城市漫步把美食与短途旅行放在一起。","2 天 1 夜","¥1100/人",Arrays.asList(9,10,11)),new TripSeed("青岛","5月—10月","海岸散步、老城建筑和海鲜小馆兼顾拍照与美食。","3 天 2 夜","¥1700/人",Arrays.asList(5,6,7,8,9,10)),new TripSeed("安吉","4月—6月、9月—11月","山野与咖啡馆适合距离城市不远的放松周末。","2 天 1 夜","¥900/人",Arrays.asList(4,5,6,9,10,11)));}
    private static class TripSeed{final String city,bestSeason,reason,duration,budget;final List<Integer> months;TripSeed(String city,String bestSeason,String reason,String duration,String budget,List<Integer>months){this.city=city;this.bestSeason=bestSeason;this.reason=reason;this.duration=duration;this.budget=budget;this.months=months;}}
}
