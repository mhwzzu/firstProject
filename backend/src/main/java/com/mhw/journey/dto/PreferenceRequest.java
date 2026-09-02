package com.mhw.journey.dto;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
public class PreferenceRequest {
    @NotBlank private String city;
    @Min(0) @Max(10000) private Integer budget;
    @Min(1) @Max(200) private Integer travelRadiusKm;
    private String foodTags=""; private String activityTags=""; private String travelTags="";
    public String getCity(){return city;} public void setCity(String city){this.city=city;} public Integer getBudget(){return budget;} public void setBudget(Integer budget){this.budget=budget;} public Integer getTravelRadiusKm(){return travelRadiusKm;} public void setTravelRadiusKm(Integer travelRadiusKm){this.travelRadiusKm=travelRadiusKm;} public String getFoodTags(){return foodTags;} public void setFoodTags(String foodTags){this.foodTags=foodTags;} public String getActivityTags(){return activityTags;} public void setActivityTags(String activityTags){this.activityTags=activityTags;} public String getTravelTags(){return travelTags;} public void setTravelTags(String travelTags){this.travelTags=travelTags;}
}
