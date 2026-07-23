package com.mhw.journey.dto;

public class DestinationRecommendation {
    private final String city;
    private final String province;
    private final String emoji;
    private final String bestSeason;
    private final String reason;
    private final int matchScore;

    public DestinationRecommendation(String city, String province, String emoji,
                                     String bestSeason, String reason, int matchScore) {
        this.city = city;
        this.province = province;
        this.emoji = emoji;
        this.bestSeason = bestSeason;
        this.reason = reason;
        this.matchScore = matchScore;
    }

    public String getCity() { return city; }
    public String getProvince() { return province; }
    public String getEmoji() { return emoji; }
    public String getBestSeason() { return bestSeason; }
    public String getReason() { return reason; }
    public int getMatchScore() { return matchScore; }
}

