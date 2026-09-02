package com.mhw.journey.service;

import com.mhw.journey.dto.DashboardSummary;
import com.mhw.journey.dto.DestinationRecommendation;
import com.mhw.journey.model.Memory;
import com.mhw.journey.model.MemoryType;
import com.mhw.journey.repository.MemoryRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MemoryService {
    private final MemoryRepository repository;

    public MemoryService(MemoryRepository repository) {
        this.repository = repository;
    }

    public List<Memory> findAll(Long spaceId) {
        return repository.findAllBySpaceIdOrderByVisitedAtDesc(spaceId);
    }

    public Memory create(Long spaceId, Memory memory) {
        memory.setId(null);
        memory.setSpaceId(spaceId);
        if (memory.getEmoji() == null || memory.getEmoji().trim().isEmpty()) {
            memory.setEmoji(memory.getType() == MemoryType.RESTAURANT ? "🍜" : "🧳");
        }
        return repository.save(memory);
    }

    public Memory update(Long spaceId, Long id, Memory incoming) {
        Memory memory = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("没有找到这条回忆"));
        if (!spaceId.equals(memory.getSpaceId())) throw new SecurityException("无权修改这条回忆");
        memory.setTitle(incoming.getTitle());
        memory.setType(incoming.getType());
        memory.setCity(incoming.getCity());
        memory.setAddress(incoming.getAddress());
        memory.setVisitedAt(incoming.getVisitedAt());
        memory.setRating(incoming.getRating());
        memory.setNote(incoming.getNote());
        memory.setTags(incoming.getTags());
        memory.setEmoji(incoming.getEmoji());
        memory.setCategory(incoming.getCategory());
        memory.setSpecialty(incoming.getSpecialty());
        memory.setPlaceId(incoming.getPlaceId());
        memory.setProvince(incoming.getProvince());
        memory.setDistrict(incoming.getDistrict());
        memory.setLatitude(incoming.getLatitude());
        memory.setLongitude(incoming.getLongitude());
        return repository.save(memory);
    }

    public void delete(Long spaceId, Long id) {
        Memory memory = repository.findById(id).orElseThrow(() -> new NoSuchElementException("没有找到这条回忆"));
        if (!spaceId.equals(memory.getSpaceId())) throw new SecurityException("无权删除这条回忆");
        repository.delete(memory);
    }

    public DashboardSummary getSummary(Long spaceId) {
        List<Memory> all = repository.findAllBySpaceIdOrderByVisitedAtDesc(spaceId);
        long cities = all.stream().map(Memory::getCity).filter(Objects::nonNull).distinct().count();
        return new DashboardSummary(
                all.size(),
                repository.countBySpaceIdAndType(spaceId, MemoryType.RESTAURANT),
                repository.countBySpaceIdAndType(spaceId, MemoryType.TRIP),
                cities
        );
    }

    public List<DestinationRecommendation> recommend(Long spaceId) {
        Set<String> visited = repository.findAllBySpaceIdOrderByVisitedAtDesc(spaceId).stream()
                .map(Memory::getCity)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<DestinationRecommendation> pool = Arrays.asList(
                new DestinationRecommendation("泉州", "福建", "🏮", "10月—次年4月",
                        "古城慢游、闽南小吃和海边日落，很适合两个人边走边吃。", 96),
                new DestinationRecommendation("大理", "云南", "🌤️", "3月—5月",
                        "环洱海骑行、古城咖啡馆和苍山晚风，节奏松弛又浪漫。", 93),
                new DestinationRecommendation("长沙", "湖南", "🌶️", "9月—11月",
                        "夜市密集、湘菜丰富，适合把旅行重点放在一起吃好吃的。", 91),
                new DestinationRecommendation("青岛", "山东", "🌊", "5月—10月",
                        "海岸散步、老城区建筑和海鲜小馆，拍照与美食都兼顾。", 89),
                new DestinationRecommendation("阿勒泰", "新疆", "🏔️", "6月—9月",
                        "草原、湖泊和星空适合一场更特别的长途纪念旅行。", 87)
        );

        return pool.stream()
                .filter(item -> !visited.contains(item.getCity()))
                .limit(3)
                .collect(Collectors.toList());
    }

    public List<Memory> findMilkTea(Long spaceId) {
        return repository.findBySpaceIdAndCategoryIgnoreCaseOrderByVisitedAtDesc(spaceId, "MILK_TEA");
    }
}
