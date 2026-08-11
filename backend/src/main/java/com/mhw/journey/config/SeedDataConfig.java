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
                            "2025-04-19", 5, "风很轻，我们沿着湖边一直走到天黑。", "日落,散步,春天", "🌅",
                            "TRIP", "", "浙江", "西湖区", 30.259, 120.139),
                    memory("巷子里的炭火烤肉", MemoryType.RESTAURANT, "上海", "愚园路",
                            "2025-02-14", 5, "临时走进的小店，牛肋条意外成为年度最佳。", "烤肉,约会,宝藏店", "🥩",
                            "BBQ", "炭火牛肋条", "上海", "长宁区", 31.221, 121.431),
                    memory("海风吹过鼓浪屿", MemoryType.TRIP, "厦门", "鼓浪屿",
                            "2024-10-03", 5, "没有赶景点，只是找小路、听钢琴、喝冰咖啡。", "海边,慢旅行,咖啡", "🌊",
                            "TRIP", "", "福建", "思明区", 24.448, 118.063),
                    memory("南京的鸭屎香奶茶", MemoryType.RESTAURANT, "南京", "老门东",
                            "2024-06-22", 5, "雨天躲进小店，热奶茶和老街特别搭。", "奶茶,鸭屎香,老街", "🧋",
                            "MILK_TEA", "鸭屎香奶茶", "江苏", "秦淮区", 32.021, 118.788),
                    memory("长沙第一杯幽兰拿铁", MemoryType.RESTAURANT, "长沙", "五一广场",
                            "2025-06-08", 5, "奶油顶和碧根果碎太会了，适合边逛边喝。", "奶茶,茶颜悦色,城市限定", "🧋",
                            "MILK_TEA", "幽兰拿铁", "湖南", "芙蓉区", 28.194, 112.982),
                    memory("杭州桂花乌龙奶茶", MemoryType.RESTAURANT, "杭州", "武林路",
                            "2025-04-20", 4, "桂花香很轻，像把西湖边的春天装进杯子里。", "奶茶,桂花,乌龙", "🧋",
                            "MILK_TEA", "桂花乌龙奶茶", "浙江", "拱墅区", 30.274, 120.159)
            ));
        };
    }

    private Memory memory(String title, MemoryType type, String city, String address,
                          String date, int rating, String note, String tags, String emoji,
                          String category, String specialty, String province, String district,
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
        memory.setCategory(category);
        memory.setSpecialty(specialty);
        memory.setProvince(province);
        memory.setDistrict(district);
        memory.setLatitude(latitude);
        memory.setLongitude(longitude);
        return memory;
    }
}
