package com.mhw.journey.controller;
import com.mhw.journey.dto.CreatePlanRequest;
import com.mhw.journey.dto.FeedbackRequest;
import com.mhw.journey.model.*;
import com.mhw.journey.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
@RestController @RequestMapping("/api/v1")
public class DecisionController {
    private final SessionService sessions;private final SpaceService spaces;private final DecisionService decisions;
    public DecisionController(SessionService sessions,SpaceService spaces,DecisionService decisions){this.sessions=sessions;this.spaces=spaces;this.decisions=decisions;}
    @PostMapping("/candidates/{id}/feedback") public CandidateFeedback feedback(@PathVariable Long id,@Valid @RequestBody FeedbackRequest body,HttpServletRequest request){Long user=sessions.requireUserId(request);CoupleSpace space=spaces.requireCurrentSpace(user);return decisions.feedback(user,space.getId(),id,body.getAction().trim().toUpperCase());}
    @PostMapping("/plans") @ResponseStatus(HttpStatus.CREATED) public JourneyPlan create(@Valid @RequestBody CreatePlanRequest body,HttpServletRequest request){Long user=sessions.requireUserId(request);CoupleSpace space=spaces.requireCurrentSpace(user);return decisions.plan(space.getId(),body);}
    @GetMapping("/plans") public List<JourneyPlan> list(HttpServletRequest request){Long user=sessions.requireUserId(request);CoupleSpace space=spaces.requireCurrentSpace(user);return decisions.plans(space.getId());}
    @PostMapping("/plans/{id}/complete") public JourneyPlan complete(@PathVariable Long id,HttpServletRequest request){Long user=sessions.requireUserId(request);CoupleSpace space=spaces.requireCurrentSpace(user);return decisions.complete(space.getId(),id);}
}
