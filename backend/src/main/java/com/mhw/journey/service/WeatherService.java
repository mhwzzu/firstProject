package com.mhw.journey.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.mhw.journey.dto.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherService {
    private final RestTemplate rest;
    private final String amapKey;

    public WeatherService(RestTemplate rest, @Value("${amap.key:}") String amapKey) {
        this.rest = rest;
        this.amapKey = amapKey;
    }

    public boolean isConfigured() {
        return amapKey != null && !amapKey.trim().isEmpty();
    }

    public Snapshot current(String city) {
        if (!isConfigured()) return new Snapshot("未配置天气服务", "已使用季节规则；配置 AMAP_KEY 后可获得实时天气", false);
        try {
            JsonNode root = request(city, "base");
            JsonNode live = root == null ? null : root.path("lives").path(0);
            if (!valid(root) || live == null || live.isMissingNode()) {
                return new Snapshot("天气暂不可用", "高德天气服务没有返回有效数据；已降级为季节规则", false);
            }
            String weather = live.path("weather").asText("天气未知");
            String temp = live.path("temperature").asText("");
            return new Snapshot(weather + (temp.isEmpty() ? "" : " · " + temp + "°C"), "高德天气实时数据", true);
        } catch (Exception ignored) {
            return new Snapshot("天气暂不可用", "天气服务超时；已降级为季节规则", false);
        }
    }

    public WeatherResponse detail(String city) {
        if (!isConfigured()) return WeatherResponse.unavailable(city, "尚未配置高德 Web 服务 Key");
        try {
            JsonNode liveRoot = request(city, "base");
            JsonNode forecastRoot = request(city, "all");
            if (!valid(liveRoot) || !valid(forecastRoot)) return WeatherResponse.unavailable(city, "高德天气暂时没有返回有效数据");

            JsonNode live = liveRoot.path("lives").path(0);
            JsonNode forecast = forecastRoot.path("forecasts").path(0);
            List<WeatherResponse.Day> days = new ArrayList<>();
            for (JsonNode cast : forecast.path("casts")) {
                days.add(new WeatherResponse.Day(
                        cast.path("date").asText(""),
                        cast.path("week").asText(""),
                        cast.path("dayweather").asText(""),
                        cast.path("nightweather").asText(""),
                        cast.path("daytemp").asText(""),
                        cast.path("nighttemp").asText(""),
                        cast.path("daywind").asText(""),
                        cast.path("daypower").asText(""),
                        suitability(cast)));
            }

            String weather = live.path("weather").asText("");
            String temperature = live.path("temperature").asText("");
            return new WeatherResponse(true,
                    value(live, "city", city), weather, temperature,
                    live.path("humidity").asText(""), live.path("winddirection").asText(""),
                    live.path("windpower").asText(""), live.path("reporttime").asText(""),
                    currentAdvice(weather, temperature), LocalDateTime.now(), days, "高德天气");
        } catch (Exception ignored) {
            return WeatherResponse.unavailable(city, "天气服务连接超时，请稍后重试");
        }
    }

    private JsonNode request(String city, String extensions) {
        String url = UriComponentsBuilder.fromHttpUrl("https://restapi.amap.com/v3/weather/weatherInfo")
                .queryParam("key", amapKey).queryParam("city", weatherCityCode(city)).queryParam("extensions", extensions)
                .build().encode().toUriString();
        return rest.getForObject(url, JsonNode.class);
    }

    private String weatherCityCode(String city) {
        if (city == null) return "";
        if (city.contains("杭州")) return "330100";
        if (city.contains("上海")) return "310000";
        if (city.contains("北京")) return "110000";
        if (city.contains("广州")) return "440100";
        if (city.contains("成都")) return "510100";
        return city.trim();
    }

    private boolean valid(JsonNode root) { return root != null && "1".equals(root.path("status").asText()); }
    private String value(JsonNode node, String field, String fallback) {
        String value = node.path(field).asText("").trim(); return value.isEmpty() ? fallback : value;
    }
    private String suitability(JsonNode cast) {
        String text = cast.path("dayweather").asText("") + cast.path("nightweather").asText("");
        if (text.contains("雨") || text.contains("雪")) return "建议准备室内备选";
        if (text.contains("雷") || text.contains("暴")) return "不建议安排长时间户外";
        int high = number(cast.path("daytemp").asText(""), 22);
        if (high >= 34) return "注意防晒与补水";
        if (high <= 5) return "注意保暖，适合短时户外";
        return "适合安排户外活动";
    }
    private String currentAdvice(String weather, String temperature) {
        if (weather.contains("雨") || weather.contains("雪")) return "带好雨具，优先选择室内与短距离路线";
        int value = number(temperature, 22);
        if (value >= 34) return "天气偏热，建议避开正午并安排室内停靠点";
        if (value <= 8) return "温度偏低，适合暖食与较短的户外路线";
        return "体感适宜，可以优先安排散步和户外路线";
    }
    private int number(String value, int fallback) { try { return Integer.parseInt(value); } catch (Exception ignored) { return fallback; } }

    public static class Snapshot {
        public final String label; public final String status; public final boolean live;
        Snapshot(String label, String status, boolean live) { this.label=label; this.status=status; this.live=live; }
    }
}
