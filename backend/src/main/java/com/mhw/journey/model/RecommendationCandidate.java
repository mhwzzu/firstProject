package com.mhw.journey.model;

import javax.persistence.*;

@Entity
@Table(name = "recommendation_candidates")
public class RecommendationCandidate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long runId;
    private Long placeId;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, length = 80) private String city;
    @Column(length = 80) private String category;
    @Column(length = 80) private String bestSeason;
    @Column(nullable = false) private Integer score;
    @Column(length = 2000) private String reason;
    @Column(length = 1200) private String scoreBreakdown;
    @Column(nullable = false, length = 80) private String sourceLabel;
    @Column(length = 500) private String sourceUrl;
    @Column(length = 80) private String estimate;
    public Long getId() { return id; }
    public Long getRunId() { return runId; }
    public void setRunId(Long runId) { this.runId = runId; }
    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long placeId) { this.placeId = placeId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getBestSeason() { return bestSeason; }
    public void setBestSeason(String bestSeason) { this.bestSeason = bestSeason; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getScoreBreakdown() { return scoreBreakdown; }
    public void setScoreBreakdown(String scoreBreakdown) { this.scoreBreakdown = scoreBreakdown; }
    public String getSourceLabel() { return sourceLabel; }
    public void setSourceLabel(String sourceLabel) { this.sourceLabel = sourceLabel; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public String getEstimate() { return estimate; }
    public void setEstimate(String estimate) { this.estimate = estimate; }
}
