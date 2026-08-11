package com.mhw.journey.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mhw.journey.dto.PlaceSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class PlaceSearchService {
    private final RestTemplate restTemplate;
    private final String amapKey;

    public PlaceSearchService(RestTemplate restTemplate, @Value("${amap.key:}") String amapKey) {
        this.restTemplate = restTemplate;
        this.amapKey = amapKey;
    }

    public List<PlaceSearchResult> search(String keywords, String city) {
        if (amapKey == null || amapKey.trim().isEmpty() || keywords == null || keywords.trim().isEmpty()) {
            return Collections.emptyList();
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://restapi.amap.com/v3/place/text")
                .queryParam("key", amapKey)
                .queryParam("keywords", keywords.trim())
                .queryParam("offset", 10)
                .queryParam("page", 1)
                .queryParam("extensions", "base");
        if (city != null && !city.trim().isEmpty()) {
            builder.queryParam("city", city.trim());
        }

        URI uri = builder.build().encode().toUri();
        JsonNode root = restTemplate.getForObject(uri, JsonNode.class);
        if (root == null || !"1".equals(root.path("status").asText())) {
            return Collections.emptyList();
        }

        List<PlaceSearchResult> results = new ArrayList<>();
        for (JsonNode poi : root.path("pois")) {
            double[] location = parseLocation(text(poi, "location"));
            results.add(new PlaceSearchResult(
                    text(poi, "id"),
                    text(poi, "name"),
                    text(poi, "pname"),
                    text(poi, "cityname"),
                    text(poi, "adname"),
                    text(poi, "address"),
                    location[1],
                    location[0]
            ));
        }
        return results;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isArray() && value.size() > 0) {
            return value.get(0).asText("");
        }
        return value.asText("");
    }

    private double[] parseLocation(String location) {
        if (location == null || !location.contains(",")) {
            return new double[] {0, 0};
        }
        String[] parts = location.split(",");
        try {
            return new double[] {Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
        } catch (NumberFormatException exception) {
            return new double[] {0, 0};
        }
    }
}
