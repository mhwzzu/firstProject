package com.mhw.journey.discovery;

import java.util.List;

public interface ContentDiscoveryProvider {
    String name();
    boolean isConfigured();
    List<DiscoveredContent> search(DiscoveryPlatform platform, String query, int limit);
}
