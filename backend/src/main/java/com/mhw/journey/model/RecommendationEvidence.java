package com.mhw.journey.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation_evidence", indexes = @Index(name = "idx_evidence_candidate", columnList = "candidateId"))
public class RecommendationEvidence {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long candidateId;
    @Column(nullable = false, length = 24) private String platform;
    @Column(nullable = false, length = 500) private String title;
    @Column(length = 1000) private String summary;
    @Column(nullable = false, length = 1000) private String sourceUrl;
    @Column(nullable = false, length = 40) private String relationLabel;
    private LocalDateTime publishedAt;
    @Column(nullable = false) private LocalDateTime discoveredAt;

    @PrePersist void created() { if (discoveredAt == null) discoveredAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public String getRelationLabel() { return relationLabel; }
    public void setRelationLabel(String relationLabel) { this.relationLabel = relationLabel; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public LocalDateTime getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(LocalDateTime discoveredAt) { this.discoveredAt = discoveredAt; }
}
