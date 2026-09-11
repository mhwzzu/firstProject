package com.mhw.journey.discovery;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class BraveSearchDiscoveryProvider implements ContentDiscoveryProvider {
    private final RestTemplate rest;
    private final boolean enabled;
    private final String apiKey;

    public BraveSearchDiscoveryProvider(RestTemplate rest,
                                        @Value("${discovery.enabled:false}") boolean enabled,
                                        @Value("${discovery.brave-api-key:}") String apiKey) {
        this.rest = rest; this.enabled = enabled; this.apiKey = apiKey;
    }

    @Override public String name() { return "Brave Search"; }
    @Override public boolean isConfigured() { return enabled && apiKey != null && !apiKey.trim().isEmpty(); }

    @Override
    public List<DiscoveredContent> search(DiscoveryPlatform platform, String query, int limit) {
        if (!isConfigured()) throw new IllegalStateException("Brave Search 尚未配置");
        String scopedQuery = "site:" + platform.getDomain() + " " + query;
        URI uri = UriComponentsBuilder.fromHttpUrl("https://api.search.brave.com/res/v1/web/search")
                .queryParam("q", scopedQuery).queryParam("count", Math.max(1, Math.min(limit, 10)))
                .queryParam("freshness", "pm").queryParam("search_lang", "zh-hans")
                .build().encode().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Subscription-Token", apiKey.trim());
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        JsonNode root = rest.exchange(uri, HttpMethod.GET, new HttpEntity<Void>(headers), JsonNode.class).getBody();
        List<DiscoveredContent> output = new ArrayList<>();
        if (root == null) return output;
        for (JsonNode item : root.path("web").path("results")) {
            String url = text(item, "url");
            if (url.isEmpty() || !belongsTo(url, platform.getDomain())) continue;
            output.add(new DiscoveredContent(platform, clean(text(item, "title"), 500),
                    clean(text(item, "description"), 1000), url, parseDate(text(item, "page_age"))));
        }
        return output;
    }

    private boolean belongsTo(String url, String domain) {
        try { String host = URI.create(url).getHost(); return host != null && (host.equals(domain) || host.endsWith("." + domain)); }
        catch (Exception ignored) { return false; }
    }
    private String text(JsonNode node, String field) { return node.path(field).asText("").trim(); }
    private String clean(String value, int max) {
        String clean = value.replaceAll("<[^>]*>", " ").replace("&amp;", "&").replace("&quot;", "\"")
                .replace("&#39;", "'").replaceAll("\\s+", " ").trim();
        return clean.length() <= max ? clean : clean.substring(0, max - 1) + "…";
    }
    private LocalDateTime parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.of("Asia/Shanghai")).toLocalDateTime(); }
        catch (Exception ignored) { return null; }
    }
}
