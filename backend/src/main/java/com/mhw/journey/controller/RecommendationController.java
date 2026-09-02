package com.mhw.journey.controller;
import com.mhw.journey.dto.RecommendationResponse;
import com.mhw.journey.model.CoupleSpace;
import com.mhw.journey.service.RecommendationService;
import com.mhw.journey.service.SessionService;
import com.mhw.journey.service.SpaceService;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
@RestController @RequestMapping("/api/v1/recommendations")
public class RecommendationController {
    private final SessionService sessions;private final SpaceService spaces;private final RecommendationService recommendations;
    public RecommendationController(SessionService sessions,SpaceService spaces,RecommendationService recommendations){this.sessions=sessions;this.spaces=spaces;this.recommendations=recommendations;}
    @GetMapping("/today") public RecommendationResponse today(@RequestParam(required=false)String city,HttpServletRequest request){Long user=sessions.requireUserId(request);CoupleSpace space=spaces.requireCurrentSpace(user);return recommendations.today(space.getId(),city);}
    @GetMapping("/weekend") public RecommendationResponse weekend(@RequestParam(required=false)String city,HttpServletRequest request){Long user=sessions.requireUserId(request);CoupleSpace space=spaces.requireCurrentSpace(user);return recommendations.weekend(space.getId(),city);}
}
