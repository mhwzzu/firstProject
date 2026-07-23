package com.mhw.journey.controller;

import com.mhw.journey.dto.DashboardSummary;
import com.mhw.journey.dto.DestinationRecommendation;
import com.mhw.journey.model.Memory;
import com.mhw.journey.service.MemoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173"})
public class MemoryController {
    private final MemoryService service;

    public MemoryController(MemoryService service) {
        this.service = service;
    }

    @GetMapping("/memories")
    public List<Memory> memories() {
        return service.findAll();
    }

    @PostMapping("/memories")
    @ResponseStatus(HttpStatus.CREATED)
    public Memory create(@Valid @RequestBody Memory memory) {
        return service.create(memory);
    }

    @PutMapping("/memories/{id}")
    public Memory update(@PathVariable Long id, @Valid @RequestBody Memory memory) {
        return service.update(id, memory);
    }

    @DeleteMapping("/memories/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/summary")
    public DashboardSummary summary() {
        return service.getSummary();
    }

    @GetMapping("/recommendations")
    public List<DestinationRecommendation> recommendations() {
        return service.recommend();
    }
}

