package com.mhw.journey.discovery;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Primary
@Component
public class BochaSearchDiscoveryProvider implements ContentDiscoveryProvider {
    private final RestTemplate rest;
    private final boolean enabled;
    private final String apiKey;
    private final String baseUrl;

    public BochaSearchDiscoveryProvider(RestTemplate rest,
                                        @Value("${discovery.enabled:false}") boolean enabled,
                                        @Value("${discovery.bocha-api-key:}") String apiKey,
                                        @Value("${discovery.bocha-api-base-url:https://api.bochaai.com/v1}") String baseUrl) {
        this.rest = rest;
        this.enabled = enabled;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl == null ? "https://api.bochaai.com/v1" : baseUrl.replaceAll("/+$", "");
    }

    @Override
    public String name() {
        return "博查 Web Search";
    }

    @Override
    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.trim().isEmpty();
    }

    @Override
    public List<DiscoveredContent> search(DiscoveryPlatform platform, String query, int limit) {
        if (!isConfigured()) throw new IllegalStateException("博查搜索尚未配置");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey.trim());
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query", "site:" + platform.getDomain() + " " + query);
        // Social platforms are indexed unevenly. A hard one-month filter often returns no result,
        // so query the live index without a hard cut-off and let the recommendation layer expose
        // publication dates and rank recent evidence higher.
        body.put("freshness", "noLimit");
        body.put("summary", true);
        body.put("count", Math.max(1, Math.min(limit, 10)));

        URI endpoint = URI.create(baseUrl + "/web-search");
        JsonNode root = rest.postForObject(endpoint, new HttpEntity<Map<String, Object>>(body, headers), JsonNode.class);
        List<DiscoveredContent> output = new ArrayList<>();
        if (root == null) return output;
        if (root.has("code") && root.path("code").asInt(200) != 200) {
            throw new IllegalStateException("博查搜索返回错误状态：" + root.path("code").asInt());
        }

        JsonNode values = root.path("data").path("webPages").path("value");
        if (!values.isArray()) values = root.path("webPages").path("value");
        for (JsonNode item : values) {
            String url = text(item, "url");
            if (url.isEmpty() || !belongsTo(url, platform.getDomain())) continue;
            String title = clean(text(item, "name"), 500);
            if (!useful(url, title, platform)) continue;
            String summary = text(item, "summary");
            if (summary.isEmpty()) summary = text(item, "snippet");
            String city = query == null ? "" : query.trim().split("\\s+")[0];
            if (!city.isEmpty() && !(title + " " + summary).contains(city)) continue;
            output.add(new DiscoveredContent(platform, title, clean(summary, 1000), url, parseDate(text(item, "datePublished"))));
        }
        return output;
    }

    private boolean belongsTo(String url, String domain) {
        try {
            String host = URI.create(url).getHost();
            return host != null && (host.equals(domain) || host.endsWith("." + domain));
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean useful(String url, String title, DiscoveryPlatform platform) {
        try {
            URI parsed = URI.create(url); String path = parsed.getPath(); String host = parsed.getHost();
            if (path == null || path.isEmpty() || "/".equals(path) || "/explore".equals(path)) return false;
            if (host != null && (host.startsWith("vdisk.") || host.startsWith("open.") || host.startsWith("help."))) return false;
        } catch (Exception ignored) { return false; }
        String compact = title.replaceAll("\\s+", "");
        return !compact.equals(platform.getLabel()) && !compact.startsWith(platform.getLabel() + "-");
    }

    private String text(JsonNode node, String field) {
        return node.path(field).asText("").trim();
    }

    private String clean(String value, int max) {
        String clean = value.replaceAll("<[^>]*>", " ")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replaceAll("\\s+", " ")
                .trim();
        return clean.length() <= max ? clean : clean.substring(0, max - 1) + "…";
    }

    private LocalDateTime parseDate(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return OffsetDateTime.parse(value).atZoneSameInstant(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
        } catch (Exception ignored) {
            try {
                return LocalDateTime.parse(value);
            } catch (Exception ignoredAgain) {
                return null;
            }
        }
    }
}
