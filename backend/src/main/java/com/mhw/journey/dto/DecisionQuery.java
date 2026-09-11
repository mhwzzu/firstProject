package com.mhw.journey.dto;

/**
 * A user-controlled recommendation request.  It deliberately keeps the raw
 * prompt as well as the explicit filters so the UI can show and edit the
 * assumptions used for a recommendation snapshot.
 */
public class DecisionQuery {
    private final String city;
    private final String prompt;
    private final String tags;
    private final Double latitude;
    private final Double longitude;
    private final Integer budget;
    private final Integer travelMinutes;
    private final Integer variation;

    public DecisionQuery(String city, String prompt, String tags, Double latitude, Double longitude,
                         Integer budget, Integer travelMinutes, Integer variation) {
        this.city = city;
        this.prompt = prompt;
        this.tags = tags;
        this.latitude = latitude;
        this.longitude = longitude;
        this.budget = budget;
        this.travelMinutes = travelMinutes;
        this.variation = variation == null ? 0 : Math.max(0, variation);
    }

    public String getCity() { return city; }
    public String getPrompt() { return prompt; }
    public String getTags() { return tags; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }
    public Integer getBudget() { return budget; }
    public Integer getTravelMinutes() { return travelMinutes; }
    public Integer getVariation() { return variation; }
    public boolean hasOrigin() { return latitude != null && longitude != null; }
}
