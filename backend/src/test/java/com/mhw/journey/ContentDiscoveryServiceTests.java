package com.mhw.journey;

import com.mhw.journey.discovery.*;
import com.mhw.journey.model.DiscoveryRecord;
import com.mhw.journey.repository.DiscoveryRecordRepository;
import com.mhw.journey.service.ContentDiscoveryService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ContentDiscoveryServiceTests {
    @Test
    void discoversAndLabelsAllConfiguredPlatforms() {
        DiscoveryRecordRepository repository = mock(DiscoveryRecordRepository.class);
        ContentDiscoveryProvider provider = mock(ContentDiscoveryProvider.class);
        when(provider.isConfigured()).thenReturn(true);
        when(provider.name()).thenReturn("test-search");
        when(provider.search(any(DiscoveryPlatform.class), anyString(), anyInt())).thenAnswer(invocation -> {
            DiscoveryPlatform platform = invocation.getArgument(0);
            return Collections.singletonList(new DiscoveredContent(platform, platform.getLabel() + "杭州周末灵感",
                    "适合两个人的近期公开攻略", "https://www." + platform.getDomain() + "/note/1", LocalDateTime.now()));
        });
        when(repository.save(any(DiscoveryRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContentDiscoveryService service = new ContentDiscoveryService(repository, provider, 60, 3);
        DiscoveryBatch batch = service.discover(1L, "WEEKEND", "杭州", "想安静散步", "咖啡,秋景", true);

        assertThat(batch.getStatus()).isEqualTo("LIVE");
        assertThat(batch.getConnectedSources()).containsExactly("小红书", "抖音", "微博", "知乎");
        assertThat(batch.getUnavailableSources()).isEmpty();
        assertThat(batch.getRecords()).hasSize(4);
        assertThat(batch.getRecords()).allSatisfy(record -> {
            assertThat(record.getSpaceId()).isEqualTo(1L);
            assertThat(record.getQueryText()).contains("杭州").contains("周末").contains("想安静散步");
            assertThat(record.getFingerprint()).hasSize(64);
        });
        verify(provider, times(4)).search(any(DiscoveryPlatform.class), anyString(), eq(3));
    }
}
