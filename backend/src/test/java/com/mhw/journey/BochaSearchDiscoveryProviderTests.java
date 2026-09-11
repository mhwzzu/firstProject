package com.mhw.journey;

import com.mhw.journey.discovery.BochaSearchDiscoveryProvider;
import com.mhw.journey.discovery.DiscoveredContent;
import com.mhw.journey.discovery.DiscoveryPlatform;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BochaSearchDiscoveryProviderTests {
    @Test
    void mapsBochaWebSearchResultsAndKeepsOnlyRequestedPlatform() {
        RestTemplate rest = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(rest).build();
        server.expect(requestTo("https://api.bochaai.com/v1/web-search"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andRespond(withSuccess("{\"data\":{\"webPages\":{\"value\":[{\"name\":\"杭州秋日散步\",\"url\":\"https://www.xiaohongshu.com/explore/123\",\"snippet\":\"九溪近期攻略\",\"datePublished\":\"2026-09-10T08:00:00+08:00\"},{\"name\":\"无关结果\",\"url\":\"https://example.com/1\",\"snippet\":\"忽略\"}]}}}", MediaType.APPLICATION_JSON));

        BochaSearchDiscoveryProvider provider = new BochaSearchDiscoveryProvider(rest, true, "test-key", "https://api.bochaai.com/v1");
        List<DiscoveredContent> result = provider.search(DiscoveryPlatform.XIAOHONGSHU, "杭州 秋季", 3);

        assertThat(provider.isConfigured()).isTrue();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("杭州秋日散步");
        assertThat(result.get(0).getSnippet()).contains("九溪");
        server.verify();
    }
}
