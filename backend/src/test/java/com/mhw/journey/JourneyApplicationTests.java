package com.mhw.journey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JourneyApplicationTests {
    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Test
    void summaryEndpointReturnsSeededData() {
        HttpHeaders headers = loginHeaders();
        String body = rest.exchange("http://localhost:" + port + "/api/summary",
                org.springframework.http.HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class).getBody();
        assertThat(body).contains("\"totalMemories\":6");
        assertThat(body).contains("\"restaurantCount\":4");
        assertThat(body).contains("\"tripCount\":2");
    }

    private HttpHeaders loginHeaders() {
        Map<String, String> login = new HashMap<>();
        login.put("username", "mhwzzu");
        login.put("password", "change-me-now");
        ResponseEntity<String> response = rest.postForEntity("http://localhost:" + port + "/api/auth/login", login, String.class);
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.COOKIE, Collections.singletonList(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE)));
        return headers;
    }
}
