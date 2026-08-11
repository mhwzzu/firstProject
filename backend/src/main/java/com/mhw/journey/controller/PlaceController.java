package com.mhw.journey.controller;

import com.mhw.journey.dto.PlaceSearchResult;
import com.mhw.journey.service.PlaceSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/places")
public class PlaceController {
    private final PlaceSearchService service;

    public PlaceController(PlaceSearchService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public List<PlaceSearchResult> search(@RequestParam String keywords,
                                          @RequestParam(required = false) String city) {
        return service.search(keywords, city);
    }
}
