package com.mhw.journey.controller;

import com.mhw.journey.discovery.ContentDiscoveryProvider;
import com.mhw.journey.service.SessionService;
import com.mhw.journey.service.SpaceService;
import com.mhw.journey.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/integrations")
public class IntegrationController {
    private final ContentDiscoveryProvider discovery; private final WeatherService weather; private final SessionService sessions; private final SpaceService spaces;
    public IntegrationController(ContentDiscoveryProvider discovery,WeatherService weather,SessionService sessions,SpaceService spaces){this.discovery=discovery;this.weather=weather;this.sessions=sessions;this.spaces=spaces;}
    @GetMapping("/status") public Map<String,Object> status(HttpServletRequest request){spaces.requireCurrentSpace(sessions.requireUserId(request));
        Map<String,Object> result=new LinkedHashMap<>();result.put("checkedAt",LocalDateTime.now());
        result.put("discovery",item(discovery.name(),discovery.isConfigured(),discovery.isConfigured()?"已启用近期公开内容发现":"未配置搜索凭证"));
        result.put("amap",item("高德地图与天气",weather.isConfigured(),weather.isConfigured()?"地点、路线与天气凭证已配置":"未配置高德 Web 服务 Key"));return result;}
    private Map<String,Object> item(String name,boolean configured,String message){Map<String,Object> item=new LinkedHashMap<>();item.put("name",name);item.put("configured",configured);item.put("message",message);return item;}
}
