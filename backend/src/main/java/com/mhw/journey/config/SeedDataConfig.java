package com.mhw.journey.config;

import com.mhw.journey.model.Memory;
import com.mhw.journey.model.MemoryType;
import com.mhw.journey.repository.MemoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Arrays;

@Configuration
public class SeedDataConfig {
    @Bean
    CommandLineRunner seedMemories(MemoryRepository repository) {
        return args -> {
            if (repository.count() > 0) {
                return;
            }
            repository.saveAll(Arrays.asList(
                    memory("西湖边的第一场日落", MemoryType.TRIP, "杭州", "北山街",
                            "2025-04-19", 5, "风很轻，我们沿着湖边一直走到天黑。", "日落,散步,春天", "🌅", 30.259, 120.139),
                    memory("巷子里的炭火烤肉", MemoryType.RESTAURANT, "上海", "愚园路",
                            "2025-02-14", 5, "临时走进的小店，牛肋条意外成为年度最佳。", "烤肉,约会,宝藏店", "🥩", 31.221, 121.431),
                    memory("海风吹过鼓浪屿", MemoryType.TRIP, "厦门", "鼓浪屿",
                            "2024-10-03", 5, "没有赶景点，只是找小路、听钢琴、喝冰咖啡。", "海边,慢旅行,咖啡", "🌊", 24.448, 118.063),
                    memory("雨天的热汤面", MemoryType.RESTAURANT, "南京", "老门东",
                            "2024-06-22", 4, "躲雨时吃到的一碗皮肚面，热气腾腾特别安心。", "面馆,雨天,老街", "🍜", 32.021, 118.788)
            ));
        };
    }

    private Memory memory(String title, MemoryType type, String city, String address,
                          String date, int rating, String note, String tags, String emoji,
                          double latitude, double longitude) {
        Memory memory = new Memory();
        memory.setTitle(title);
        memory.setType(type);
        memory.setCity(city);
        memory.setAddress(address);
        memory.setVisitedAt(LocalDate.parse(date));
        memory.setRating(rating);
        memory.setNote(note);
        memory.setTags(tags);
        memory.setEmoji(emoji);
        memory.setLatitude(latitude);
        memory.setLongitude(longitude);
        return memory;
    }
}

