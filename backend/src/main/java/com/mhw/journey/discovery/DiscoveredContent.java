package com.mhw.journey.discovery;

import java.time.LocalDateTime;

public class DiscoveredContent {
    private final DiscoveryPlatform platform;
    private final String title;
    private final String snippet;
    private final String url;
    private final LocalDateTime publishedAt;

    public DiscoveredContent(DiscoveryPlatform platform, String title, String snippet, String url, LocalDateTime publishedAt) {
        this.platform = platform; this.title = title; this.snippet = snippet; this.url = url; this.publishedAt = publishedAt;
    }
    public DiscoveryPlatform getPlatform() { return platform; }
    public String getTitle() { return title; }
    public String getSnippet() { return snippet; }
    public String getUrl() { return url; }
    public LocalDateTime getPublishedAt() { return publishedAt; }
}
