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
                    location == null ? null : location[1],
                    location == null ? null : location[0]
            ));
        }
        return results;
    }

    /** Returns a route only when the configured Web service key can provide one. */
    public RouteSnapshot drivingRoute(double originLatitude, double originLongitude, double destinationLatitude, double destinationLongitude) {
        if (amapKey == null || amapKey.trim().isEmpty()) return null;
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl("https://restapi.amap.com/v3/direction/driving")
                    .queryParam("key", amapKey)
                    .queryParam("origin", originLongitude + "," + originLatitude)
                    .queryParam("destination", destinationLongitude + "," + destinationLatitude)
                    .queryParam("strategy", 0)
                    .build().encode().toUri();
            JsonNode root = restTemplate.getForObject(uri, JsonNode.class);
            JsonNode path = root == null ? null : root.path("route").path("paths").path(0);
            if (root == null || !"1".equals(root.path("status").asText()) || path == null || path.isMissingNode()) return null;
            double meters = Double.parseDouble(path.path("distance").asText("0"));
            int seconds = Integer.parseInt(path.path("duration").asText("0"));
            return meters > 0 && seconds > 0 ? new RouteSnapshot(meters, seconds) : null;
        } catch (Exception ignored) {
            return null;
        }
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
            return null;
        }
        String[] parts = location.split(",");
        try {
            return new double[] {Double.parseDouble(parts[0]), Double.parseDouble(parts[1])};
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    public static class RouteSnapshot {
        private final double meters;
        private final int seconds;
        RouteSnapshot(double meters, int seconds) { this.meters = meters; this.seconds = seconds; }
        public double getMeters() { return meters; }
        public int getSeconds() { return seconds; }
    }
}
