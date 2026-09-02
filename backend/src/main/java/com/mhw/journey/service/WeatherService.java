package com.mhw.journey.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WeatherService {
    private final RestTemplate rest; private final String amapKey;
    public WeatherService(RestTemplate rest, @Value("${amap.key:}") String amapKey){this.rest=rest;this.amapKey=amapKey;}
    public Snapshot current(String city) {
        if (amapKey == null || amapKey.trim().isEmpty()) return new Snapshot("未配置天气服务", "已使用季节规则；配置 AMAP_KEY 后可获得实时天气", false);
        try {
            String url=UriComponentsBuilder.fromHttpUrl("https://restapi.amap.com/v3/weather/weatherInfo").queryParam("key",amapKey).queryParam("city",city).queryParam("extensions","base").build().encode().toUriString();
            JsonNode root=rest.getForObject(url,JsonNode.class);
            JsonNode live=root==null?null:root.path("lives").path(0);
            if(root==null || !"1".equals(root.path("status").asText()) || live==null || live.isMissingNode()) return new Snapshot("天气暂不可用", "高德天气服务没有返回有效数据；已降级为季节规则", false);
            String weather=live.path("weather").asText("天气未知"); String temp=live.path("temperature").asText("");
            return new Snapshot(weather + (temp.isEmpty()?"":" · "+temp+"°C"), "高德天气实时数据", true);
        } catch (Exception ignored) { return new Snapshot("天气暂不可用", "天气服务超时；已降级为季节规则", false); }
    }
    public static class Snapshot { public final String label; public final String status; public final boolean live; Snapshot(String label,String status,boolean live){this.label=label;this.status=status;this.live=live;} }
}
