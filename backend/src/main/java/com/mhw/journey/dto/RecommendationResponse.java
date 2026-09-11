package com.mhw.journey.dto;

import java.time.LocalDateTime;
import java.util.List;

public class RecommendationResponse {
    private final String type;
    private final String city;
    private final String weather;
    private final String sourceStatus;
    private final String querySummary;
    private final String locationStatus;
    private final LocalDateTime generatedAt;
    private final String discoveryStatus;
    private final List<String> connectedSources;
    private final List<String> unavailableSources;
    private final LocalDateTime refreshedAt;
    private final List<Candidate> candidates;

    public RecommendationResponse(String type, String city, String weather, String sourceStatus, String querySummary,
                                  String locationStatus, LocalDateTime generatedAt, String discoveryStatus,
                                  List<String> connectedSources, List<String> unavailableSources,
                                  LocalDateTime refreshedAt, List<Candidate> candidates) {
        this.type=type; this.city=city; this.weather=weather; this.sourceStatus=sourceStatus; this.querySummary=querySummary;
        this.locationStatus=locationStatus; this.generatedAt=generatedAt; this.discoveryStatus=discoveryStatus;
        this.connectedSources=connectedSources; this.unavailableSources=unavailableSources; this.refreshedAt=refreshedAt;
        this.candidates=candidates;
    }
    public String getType(){return type;} public String getCity(){return city;} public String getWeather(){return weather;}
    public String getSourceStatus(){return sourceStatus;} public String getQuerySummary(){return querySummary;}
    public String getLocationStatus(){return locationStatus;} public LocalDateTime getGeneratedAt(){return generatedAt;}
    public String getDiscoveryStatus(){return discoveryStatus;} public List<String> getConnectedSources(){return connectedSources;}
    public List<String> getUnavailableSources(){return unavailableSources;} public LocalDateTime getRefreshedAt(){return refreshedAt;}
    public List<Candidate> getCandidates(){return candidates;}

    public static class Candidate {
        private final Long id; private final String title; private final String city; private final String category;
        private final String bestSeason; private final Integer score; private final String reason; private final String scoreBreakdown;
        private final String sourceLabel; private final String sourceUrl; private final String estimate; private final String district;
        private final String address; private final Double latitude; private final Double longitude; private final Double distanceKm;
        private final Integer travelMinutes; private final String travelDataStatus; private final String navigationUrl;
        private final List<Evidence> evidence;

        public Candidate(Long id,String title,String city,String category,String bestSeason,Integer score,String reason,
                         String scoreBreakdown,String sourceLabel,String sourceUrl,String estimate,String district,String address,
                         Double latitude,Double longitude,Double distanceKm,Integer travelMinutes,String travelDataStatus,
                         String navigationUrl,List<Evidence> evidence) {
            this.id=id;this.title=title;this.city=city;this.category=category;this.bestSeason=bestSeason;this.score=score;
            this.reason=reason;this.scoreBreakdown=scoreBreakdown;this.sourceLabel=sourceLabel;this.sourceUrl=sourceUrl;
            this.estimate=estimate;this.district=district;this.address=address;this.latitude=latitude;this.longitude=longitude;
            this.distanceKm=distanceKm;this.travelMinutes=travelMinutes;this.travelDataStatus=travelDataStatus;
            this.navigationUrl=navigationUrl;this.evidence=evidence;
        }
        public Long getId(){return id;} public String getTitle(){return title;} public String getCity(){return city;}
        public String getCategory(){return category;} public String getBestSeason(){return bestSeason;} public Integer getScore(){return score;}
        public String getReason(){return reason;} public String getScoreBreakdown(){return scoreBreakdown;} public String getSourceLabel(){return sourceLabel;}
        public String getSourceUrl(){return sourceUrl;} public String getEstimate(){return estimate;} public String getDistrict(){return district;}
        public String getAddress(){return address;} public Double getLatitude(){return latitude;} public Double getLongitude(){return longitude;}
        public Double getDistanceKm(){return distanceKm;} public Integer getTravelMinutes(){return travelMinutes;}
        public String getTravelDataStatus(){return travelDataStatus;} public String getNavigationUrl(){return navigationUrl;}
        public List<Evidence> getEvidence(){return evidence;}
    }

    public static class Evidence {
        private final String platform; private final String title; private final String summary; private final String sourceUrl;
        private final String relationLabel; private final LocalDateTime publishedAt; private final LocalDateTime discoveredAt;
        public Evidence(String platform,String title,String summary,String sourceUrl,String relationLabel,
                        LocalDateTime publishedAt,LocalDateTime discoveredAt) {
            this.platform=platform;this.title=title;this.summary=summary;this.sourceUrl=sourceUrl;
            this.relationLabel=relationLabel;this.publishedAt=publishedAt;this.discoveredAt=discoveredAt;
        }
        public String getPlatform(){return platform;} public String getTitle(){return title;} public String getSummary(){return summary;}
        public String getSourceUrl(){return sourceUrl;} public String getRelationLabel(){return relationLabel;}
        public LocalDateTime getPublishedAt(){return publishedAt;} public LocalDateTime getDiscoveredAt(){return discoveredAt;}
    }
}
