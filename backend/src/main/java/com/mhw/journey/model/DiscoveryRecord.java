package com.mhw.journey.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "discovery_records", indexes = {
        @Index(name = "idx_discovery_space_query", columnList = "spaceId,queryKey"),
        @Index(name = "idx_discovery_fingerprint", columnList = "fingerprint")
})
public class DiscoveryRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long spaceId;
    @Column(nullable = false, length = 40) private String provider;
    @Column(nullable = false, length = 24) private String platform;
    @Column(nullable = false, length = 64) private String queryKey;
    @Column(nullable = false, length = 500) private String queryText;
    @Column(nullable = false, length = 500) private String title;
    @Column(length = 1200) private String snippet;
    @Column(nullable = false, length = 1000) private String sourceUrl;
    private LocalDateTime publishedAt;
    @Column(nullable = false) private LocalDateTime discoveredAt;
    @Column(nullable = false, length = 64) private String fingerprint;
    @Column(length = 160) private String placeKeyword;
    @Column(nullable = false, length = 24) private String status;

    @PrePersist void created() { if (discoveredAt == null) discoveredAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Long getSpaceId() { return spaceId; }
    public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getQueryKey() { return queryKey; }
    public void setQueryKey(String queryKey) { this.queryKey = queryKey; }
    public String getQueryText() { return queryText; }
    public void setQueryText(String queryText) { this.queryText = queryText; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
    public LocalDateTime getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(LocalDateTime discoveredAt) { this.discoveredAt = discoveredAt; }
    public String getFingerprint() { return fingerprint; }
    public void setFingerprint(String fingerprint) { this.fingerprint = fingerprint; }
    public String getPlaceKeyword() { return placeKeyword; }
    public void setPlaceKeyword(String placeKeyword) { this.placeKeyword = placeKeyword; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
