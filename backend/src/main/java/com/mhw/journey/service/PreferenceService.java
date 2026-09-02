package com.mhw.journey.service;
import com.mhw.journey.dto.PreferenceRequest;
import com.mhw.journey.model.PreferenceProfile;
import com.mhw.journey.repository.PreferenceProfileRepository;
import org.springframework.stereotype.Service;
@Service
public class PreferenceService {
    private final PreferenceProfileRepository profiles;
    public PreferenceService(PreferenceProfileRepository profiles){this.profiles=profiles;}
    public PreferenceProfile get(Long spaceId,Long userId){return profiles.findBySpaceIdAndUserId(spaceId,userId).orElseGet(() -> {PreferenceProfile p=new PreferenceProfile();p.setSpaceId(spaceId);p.setUserId(userId);p.setCity("杭州");return profiles.save(p);});}
    public PreferenceProfile save(Long spaceId,Long userId,PreferenceRequest request){PreferenceProfile p=get(spaceId,userId);p.setCity(request.getCity().trim());p.setBudget(request.getBudget());p.setTravelRadiusKm(request.getTravelRadiusKm());p.setFoodTags(clean(request.getFoodTags()));p.setActivityTags(clean(request.getActivityTags()));p.setTravelTags(clean(request.getTravelTags()));p.setOnboardingComplete(true);return profiles.save(p);}
    private String clean(String value){return value==null?"":value.trim();}
}
