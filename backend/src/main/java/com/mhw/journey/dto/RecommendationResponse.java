package com.mhw.journey.dto;
import java.time.LocalDateTime;
import java.util.List;
public class RecommendationResponse {
    private String type; private String city; private String weather; private String sourceStatus; private LocalDateTime generatedAt; private List<Candidate> candidates;
    public RecommendationResponse(String type,String city,String weather,String sourceStatus,LocalDateTime generatedAt,List<Candidate> candidates){this.type=type;this.city=city;this.weather=weather;this.sourceStatus=sourceStatus;this.generatedAt=generatedAt;this.candidates=candidates;}
    public String getType(){return type;} public String getCity(){return city;} public String getWeather(){return weather;} public String getSourceStatus(){return sourceStatus;} public LocalDateTime getGeneratedAt(){return generatedAt;} public List<Candidate> getCandidates(){return candidates;}
    public static class Candidate { private Long id; private String title; private String city; private String category; private String bestSeason; private Integer score; private String reason; private String scoreBreakdown; private String sourceLabel; private String sourceUrl; private String estimate;
        public Candidate(Long id,String title,String city,String category,String bestSeason,Integer score,String reason,String scoreBreakdown,String sourceLabel,String sourceUrl,String estimate){this.id=id;this.title=title;this.city=city;this.category=category;this.bestSeason=bestSeason;this.score=score;this.reason=reason;this.scoreBreakdown=scoreBreakdown;this.sourceLabel=sourceLabel;this.sourceUrl=sourceUrl;this.estimate=estimate;}
        public Long getId(){return id;} public String getTitle(){return title;} public String getCity(){return city;} public String getCategory(){return category;} public String getBestSeason(){return bestSeason;} public Integer getScore(){return score;} public String getReason(){return reason;} public String getScoreBreakdown(){return scoreBreakdown;} public String getSourceLabel(){return sourceLabel;} public String getSourceUrl(){return sourceUrl;} public String getEstimate(){return estimate;}
    }
}
