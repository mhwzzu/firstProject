package com.mhw.journey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JourneyApplicationTests {
    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Test
    void summaryEndpointReturnsSeededData() {
        String body = rest.getForObject("http://localhost:" + port + "/api/summary", String.class);
        assertThat(body).contains("\"totalMemories\":4");
        assertThat(body).contains("\"restaurantCount\":2");
        assertThat(body).contains("\"tripCount\":2");
    }
}

