package com.mhw.journey.model;

import javax.persistence.*;

@Entity
@Table(name = "preference_profiles", uniqueConstraints = @UniqueConstraint(columnNames = {"spaceId", "userId"}))
public class PreferenceProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long spaceId;
    @Column(nullable = false) private Long userId;
    @Column(nullable = false, length = 80) private String city;
    private Integer budget = 150;
    private Integer travelRadiusKm = 12;
    @Column(length = 1000) private String foodTags = "";
    @Column(length = 1000) private String activityTags = "";
    @Column(length = 1000) private String travelTags = "";
    private boolean onboardingComplete;
    public Long getId() { return id; }
    public Long getSpaceId() { return spaceId; }
    public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public Integer getBudget() { return budget; }
    public void setBudget(Integer budget) { this.budget = budget; }
    public Integer getTravelRadiusKm() { return travelRadiusKm; }
    public void setTravelRadiusKm(Integer travelRadiusKm) { this.travelRadiusKm = travelRadiusKm; }
    public String getFoodTags() { return foodTags; }
    public void setFoodTags(String foodTags) { this.foodTags = foodTags; }
    public String getActivityTags() { return activityTags; }
    public void setActivityTags(String activityTags) { this.activityTags = activityTags; }
    public String getTravelTags() { return travelTags; }
    public void setTravelTags(String travelTags) { this.travelTags = travelTags; }
    public boolean isOnboardingComplete() { return onboardingComplete; }
    public void setOnboardingComplete(boolean onboardingComplete) { this.onboardingComplete = onboardingComplete; }
}
