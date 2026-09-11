package com.mhw.journey.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class WeatherResponse {
    private final boolean available; private final String city; private final String weather; private final String temperature;
    private final String humidity; private final String windDirection; private final String windPower; private final String reportTime;
    private final String advice; private final LocalDateTime fetchedAt; private final List<Day> days; private final String source;
    public WeatherResponse(boolean available,String city,String weather,String temperature,String humidity,String windDirection,
                           String windPower,String reportTime,String advice,LocalDateTime fetchedAt,List<Day> days,String source){
        this.available=available;this.city=city;this.weather=weather;this.temperature=temperature;this.humidity=humidity;
        this.windDirection=windDirection;this.windPower=windPower;this.reportTime=reportTime;this.advice=advice;
        this.fetchedAt=fetchedAt;this.days=days;this.source=source;
    }
    public static WeatherResponse unavailable(String city,String message){return new WeatherResponse(false,city,"天气暂不可用","","","","","",message,LocalDateTime.now(),Collections.<Day>emptyList(),"高德天气");}
    public boolean isAvailable(){return available;} public String getCity(){return city;} public String getWeather(){return weather;}
    public String getTemperature(){return temperature;} public String getHumidity(){return humidity;} public String getWindDirection(){return windDirection;}
    public String getWindPower(){return windPower;} public String getReportTime(){return reportTime;} public String getAdvice(){return advice;}
    public LocalDateTime getFetchedAt(){return fetchedAt;} public List<Day> getDays(){return days;} public String getSource(){return source;}
    public static class Day {
        private final String date;private final String week;private final String dayWeather;private final String nightWeather;
        private final String high;private final String low;private final String windDirection;private final String windPower;private final String suitability;
        public Day(String date,String week,String dayWeather,String nightWeather,String high,String low,String windDirection,String windPower,String suitability){this.date=date;this.week=week;this.dayWeather=dayWeather;this.nightWeather=nightWeather;this.high=high;this.low=low;this.windDirection=windDirection;this.windPower=windPower;this.suitability=suitability;}
        public String getDate(){return date;} public String getWeek(){return week;} public String getDayWeather(){return dayWeather;} public String getNightWeather(){return nightWeather;}
        public String getHigh(){return high;} public String getLow(){return low;} public String getWindDirection(){return windDirection;} public String getWindPower(){return windPower;} public String getSuitability(){return suitability;}
    }
}
