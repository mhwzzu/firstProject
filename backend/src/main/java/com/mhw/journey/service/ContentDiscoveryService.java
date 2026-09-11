package com.mhw.journey.service;

import com.mhw.journey.discovery.*;
import com.mhw.journey.model.DiscoveryRecord;
import com.mhw.journey.repository.DiscoveryRecordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ContentDiscoveryService {
    private final DiscoveryRecordRepository records;
    private final ContentDiscoveryProvider provider;
    private final int cacheMinutes;
    private final int resultLimit;

    public ContentDiscoveryService(DiscoveryRecordRepository records, ContentDiscoveryProvider provider,
                                   @Value("${discovery.cache-minutes:60}") int cacheMinutes,
                                   @Value("${discovery.result-limit:3}") int resultLimit) {
        this.records=records; this.provider=provider; this.cacheMinutes=Math.max(5, cacheMinutes); this.resultLimit=Math.max(1, Math.min(resultLimit, 5));
    }

    public DiscoveryBatch discover(Long spaceId, String type, String city, String prompt, String tags, boolean forceRefresh) {
        String topic = query(type, city, prompt, tags);
        String queryKey = hash(type + "|" + city + "|" + clean(prompt) + "|" + clean(tags) + "|" + LocalDate.now().getMonthValue());
        if (!forceRefresh) {
            List<DiscoveryRecord> cached = records.findBySpaceIdAndQueryKeyAndDiscoveredAtAfterOrderByDiscoveredAtDesc(
                    spaceId, queryKey, LocalDateTime.now().minusMinutes(cacheMinutes));
            if (!cached.isEmpty()) return cachedBatch(cached);
        }
        List<String> unavailable = new ArrayList<>();
        List<String> connected = new ArrayList<>();
        List<DiscoveryRecord> discovered = new ArrayList<>();
        if (!provider.isConfigured()) {
            for (DiscoveryPlatform platform : DiscoveryPlatform.values()) unavailable.add(platform.getLabel());
            return new DiscoveryBatch("DEGRADED", "近期攻略源尚未连接；配置 BRAVE_SEARCH_API_KEY 后即可自动发现四个平台公开内容",
                    connected, unavailable, LocalDateTime.now(), discovered);
        }
        for (DiscoveryPlatform platform : DiscoveryPlatform.values()) {
            try {
                List<DiscoveredContent> items = provider.search(platform, topic, resultLimit);
                connected.add(platform.getLabel());
                for (DiscoveredContent item : items) discovered.add(records.save(record(spaceId, queryKey, topic, item)));
            } catch (Exception ignored) { unavailable.add(platform.getLabel()); }
        }
        String status = discovered.isEmpty() ? "DEGRADED" : unavailable.isEmpty() ? "LIVE" : "PARTIAL";
        String message = "LIVE".equals(status) ? "四个平台近期公开内容已更新" :
                "PARTIAL".equals(status) ? "近期攻略已更新，部分来源暂时不可用" : "本次没有获取到近期公开内容，已使用地图与偏好结果";
        return new DiscoveryBatch(status, message, connected, unavailable, LocalDateTime.now(), deduplicate(discovered));
    }

    private DiscoveryBatch cachedBatch(List<DiscoveryRecord> cached) {
        LinkedHashSet<String> connected = new LinkedHashSet<>();
        for (DiscoveryRecord record : cached) connected.add(record.getPlatform());
        List<String> unavailable = new ArrayList<>();
        for (DiscoveryPlatform platform : DiscoveryPlatform.values()) if (!connected.contains(platform.getLabel())) unavailable.add(platform.getLabel());
        String status = unavailable.isEmpty() ? "LIVE" : "PARTIAL";
        return new DiscoveryBatch(status, "使用最近一次自动发现结果", new ArrayList<>(connected), unavailable,
                cached.get(0).getDiscoveredAt(), deduplicate(cached));
    }

    private DiscoveryRecord record(Long spaceId, String queryKey, String query, DiscoveredContent item) {
        DiscoveryRecord record = new DiscoveryRecord(); record.setSpaceId(spaceId); record.setProvider(provider.name());
        record.setPlatform(item.getPlatform().getLabel()); record.setQueryKey(queryKey); record.setQueryText(query);
        record.setTitle(item.getTitle()); record.setSnippet(item.getSnippet()); record.setSourceUrl(item.getUrl());
        record.setPublishedAt(item.getPublishedAt()); record.setDiscoveredAt(LocalDateTime.now());
        record.setFingerprint(hash(item.getUrl() + "|" + item.getTitle())); record.setPlaceKeyword(placeKeyword(item.getTitle())); record.setStatus("ACTIVE");
        return record;
    }
    private List<DiscoveryRecord> deduplicate(List<DiscoveryRecord> input) {
        LinkedHashMap<String,DiscoveryRecord> unique = new LinkedHashMap<>();
        for (DiscoveryRecord record : input) if (!unique.containsKey(record.getFingerprint())) unique.put(record.getFingerprint(), record);
        return new ArrayList<>(unique.values());
    }
    private String query(String type, String city, String prompt, String tags) {
        String season = season(); String intent = clean(prompt).isEmpty() ? clean(tags) : clean(prompt);
        if (intent.isEmpty()) intent = "情侣 约会 美食 旅行";
        return city + " " + season + " " + ("WEEKEND".equals(type) ? "周末 攻略" : "近期 去哪玩 吃什么") + " " + intent;
    }
    private String season() { int month=LocalDate.now().getMonthValue(); return month<=2||month==12?"冬季":month<=5?"春季":month<=8?"夏季":"秋季"; }
    private String placeKeyword(String title) {
        if (title == null) return ""; String clean=title.replaceAll("[【】\\[\\]（）()#]", " ").split("[-_|｜]")[0].trim();
        return clean.length() <= 60 ? clean : clean.substring(0, 60);
    }
    private String clean(String value) { return value == null ? "" : value.trim().replaceAll("\\s+", " "); }
    private String hash(String input) {
        try { byte[] bytes=MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8)); StringBuilder out=new StringBuilder(); for(byte b:bytes) out.append(String.format("%02x",b & 0xff)); return out.toString(); }
        catch (Exception exception) { return Integer.toHexString(input.hashCode()); }
    }
}
