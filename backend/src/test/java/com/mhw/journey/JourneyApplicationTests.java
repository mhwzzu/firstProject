package com.mhw.journey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JourneyApplicationTests {
    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;

    @Test
    void privateSpaceSupportsRecommendationsFeedbackAndPlans() {
        HttpHeaders owner = register("owner@example.com", "小明");
        String space = exchange("/api/v1/spaces", HttpMethod.POST, owner, map("name", "周末搭子", "city", "杭州"));
        assertThat(space).contains("周末搭子");
        String preferences = exchange("/api/v1/preferences", HttpMethod.PUT, owner, map("city", "杭州", "budget", 180, "travelRadiusKm", 15, "foodTags", "川菜,咖啡", "activityTags", "展览", "travelTags", "海边,慢旅行"));
        assertThat(preferences).contains("\"onboardingComplete\":true");
        String invite = exchange("/api/v1/spaces/current/invites", HttpMethod.POST, owner, null);
        HttpHeaders partner = register("partner@example.com", "小雨");
        String joined = exchange("/api/v1/spaces/join", HttpMethod.POST, partner, map("token", textField(invite, "token")));
        assertThat(joined).contains("小明").contains("小雨");
        String today = exchange("/api/v1/recommendations/today?city=%E6%9D%AD%E5%B7%9E&prompt=%E5%91%A8%E6%9C%AB%E6%83%B3%E5%92%96%E5%95%A1&tags=%E5%92%96%E5%95%A1&budget=120&travelMinutes=30&latitude=30.2741&longitude=120.1551", HttpMethod.GET, owner, null);
        assertThat(today).contains("\"type\":\"TODAY\"").contains("sourceStatus").contains("scoreBreakdown")
                .contains("querySummary").contains("locationStatus").contains("distanceKm").contains("travelDataStatus").contains("navigationUrl")
                .contains("\"discoveryStatus\":\"DEGRADED\"").contains("connectedSources").contains("unavailableSources").contains("refreshedAt").contains("evidence");
        long candidateId = firstId(today);
        String feedback = exchange("/api/v1/candidates/" + candidateId + "/feedback", HttpMethod.POST, partner, map("action", "WANT"));
        assertThat(feedback).contains("\"action\":\"WANT\"");
        String plan = exchange("/api/v1/plans", HttpMethod.POST, owner, map("candidateId", candidateId, "budget", 180, "note", "一起去看看"));
        long planId = firstId(plan);
        assertThat(exchange("/api/v1/plans/" + planId + "/complete", HttpMethod.POST, owner, null)).contains("\"status\":\"COMPLETED\"");
        assertThat(exchange("/api/v1/plans", HttpMethod.GET, partner, null)).contains("\"title\"");
    }

    private HttpHeaders register(String email, String name) {
        ResponseEntity<String> response = rest.postForEntity(url("/api/v1/auth/register"), map("email", email, "displayName", name, "password", "safe-password-123"), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.COOKIE, Collections.singletonList(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)));
        return headers;
    }
    private String exchange(String path, HttpMethod method, HttpHeaders headers, Object body) {
        ResponseEntity<String> response = rest.exchange(url(path), method, new HttpEntity<Object>(body, headers), String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue(); return response.getBody();
    }
    private String url(String path) { return "http://localhost:" + port + path; }
    private long firstId(String json) { Matcher m = Pattern.compile("\\\"id\\\":(\\d+)").matcher(json); assertThat(m.find()).isTrue(); return Long.parseLong(m.group(1)); }
    private String textField(String json, String field) { Matcher m = Pattern.compile("\\\"" + field + "\\\":\\\"([^\\\"]+)\\\"").matcher(json); assertThat(m.find()).isTrue(); return m.group(1); }
    private Map<String,Object> map(Object... values) { Map<String,Object> result = new HashMap<>(); for(int i=0;i<values.length;i+=2) result.put(values[i].toString(), values[i+1]); return result; }
}
