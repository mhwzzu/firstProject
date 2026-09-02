package com.mhw.journey.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recommendation_runs")
public class RecommendationRun {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long spaceId;
    @Column(nullable = false, length = 20) private String type;
    @Column(nullable = false, length = 80) private String city;
    @Column(nullable = false, length = 30) private String rulesVersion;
    @Column(nullable = false) private LocalDateTime generatedAt;
    @Column(nullable = false) private LocalDateTime expiresAt;
    @Column(length = 2000) private String contextJson;
    @PrePersist void generated() { if (generatedAt == null) generatedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Long getSpaceId() { return spaceId; }
    public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getRulesVersion() { return rulesVersion; }
    public void setRulesVersion(String rulesVersion) { this.rulesVersion = rulesVersion; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public String getContextJson() { return contextJson; }
    public void setContextJson(String contextJson) { this.contextJson = contextJson; }
}
