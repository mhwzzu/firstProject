package com.mhw.journey.controller;

import com.mhw.journey.dto.DashboardSummary;
import com.mhw.journey.dto.DestinationRecommendation;
import com.mhw.journey.model.Memory;
import com.mhw.journey.service.MemoryService;
import com.mhw.journey.service.SessionService;
import com.mhw.journey.service.SpaceService;
import com.mhw.journey.model.CoupleSpace;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class MemoryController {
    private final MemoryService service;
    private final SessionService sessions;
    private final SpaceService spaces;

    public MemoryController(MemoryService service, SessionService sessions, SpaceService spaces) {
        this.service = service; this.sessions = sessions; this.spaces = spaces;
    }

    private CoupleSpace current(HttpServletRequest request) { return spaces.requireCurrentSpace(sessions.requireUserId(request)); }

    @GetMapping("/memories")
    public List<Memory> memories(HttpServletRequest request) {
        return service.findAll(current(request).getId());
    }

    @GetMapping("/memories/milk-tea")
    public List<Memory> milkTea(HttpServletRequest request) {
        return service.findMilkTea(current(request).getId());
    }

    @PostMapping("/memories")
    @ResponseStatus(HttpStatus.CREATED)
    public Memory create(@Valid @RequestBody Memory memory, HttpServletRequest request) {
        return service.create(current(request).getId(), memory);
    }

    @PutMapping("/memories/{id}")
    public Memory update(@PathVariable Long id, @Valid @RequestBody Memory memory, HttpServletRequest request) {
        return service.update(current(request).getId(), id, memory);
    }

    @DeleteMapping("/memories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, HttpServletRequest request) {
        service.delete(current(request).getId(), id);
    }

    @GetMapping("/summary")
    public DashboardSummary summary(HttpServletRequest request) {
        return service.getSummary(current(request).getId());
    }

    @GetMapping("/recommendations")
    public List<DestinationRecommendation> recommendations(HttpServletRequest request) {
        return service.recommend(current(request).getId());
    }
}
