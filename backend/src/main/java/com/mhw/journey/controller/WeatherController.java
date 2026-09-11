package com.mhw.journey.controller;

import com.mhw.journey.dto.WeatherResponse;
import com.mhw.journey.model.CoupleSpace;
import com.mhw.journey.service.SessionService;
import com.mhw.journey.service.SpaceService;
import com.mhw.journey.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {
    private final WeatherService weather; private final SessionService sessions; private final SpaceService spaces;
    public WeatherController(WeatherService weather,SessionService sessions,SpaceService spaces){this.weather=weather;this.sessions=sessions;this.spaces=spaces;}
    @GetMapping public WeatherResponse detail(@RequestParam(required=false)String city,HttpServletRequest request){
        CoupleSpace space=spaces.requireCurrentSpace(sessions.requireUserId(request));
        String requested=city==null||city.trim().isEmpty()?space.getDefaultCity():city.trim(); return weather.detail(requested);
    }
}
