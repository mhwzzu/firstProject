package com.mhw.journey.controller;
import com.mhw.journey.dto.PreferenceRequest;
import com.mhw.journey.model.CoupleSpace;
import com.mhw.journey.model.PreferenceProfile;
import com.mhw.journey.service.PreferenceService;
import com.mhw.journey.service.SessionService;
import com.mhw.journey.service.SpaceService;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
@RestController @RequestMapping("/api/v1/preferences")
public class PreferenceController {
    private final SessionService sessions;private final SpaceService spaces;private final PreferenceService preferences;
    public PreferenceController(SessionService sessions,SpaceService spaces,PreferenceService preferences){this.sessions=sessions;this.spaces=spaces;this.preferences=preferences;}
    @GetMapping public PreferenceProfile get(HttpServletRequest request){Long user=sessions.requireUserId(request);CoupleSpace space=spaces.requireCurrentSpace(user);return preferences.get(space.getId(),user);}
    @PutMapping public PreferenceProfile update(@Valid @RequestBody PreferenceRequest body,HttpServletRequest request){Long user=sessions.requireUserId(request);CoupleSpace space=spaces.requireCurrentSpace(user);return preferences.save(space.getId(),user,body);}
}
