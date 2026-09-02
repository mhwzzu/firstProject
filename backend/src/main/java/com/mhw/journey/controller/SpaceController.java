package com.mhw.journey.controller;

import com.mhw.journey.dto.*;
import com.mhw.journey.model.CoupleSpace;
import com.mhw.journey.service.SessionService;
import com.mhw.journey.service.SpaceService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/spaces")
public class SpaceController {
    private final SessionService sessions; private final SpaceService spaces;
    public SpaceController(SessionService sessions,SpaceService spaces){this.sessions=sessions;this.spaces=spaces;}
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public SpaceResponse create(@Valid @RequestBody CreateSpaceRequest request,HttpServletRequest http){CoupleSpace space=spaces.create(sessions.requireUserId(http),request);return spaces.response(space);}
    @GetMapping("/current") public SpaceResponse current(HttpServletRequest http){return spaces.response(spaces.requireCurrentSpace(sessions.requireUserId(http)));}
    @PostMapping("/current/invites") public InviteResponse invite(HttpServletRequest http){Long user=sessions.requireUserId(http);CoupleSpace space=spaces.requireCurrentSpace(user);String token=spaces.createInvite(user,space);return new InviteResponse(token,java.time.LocalDateTime.now().plusHours(72));}
    @PostMapping("/join") public SpaceResponse join(@Valid @RequestBody JoinSpaceRequest request,HttpServletRequest http){return spaces.response(spaces.join(sessions.requireUserId(http),request.getToken()));}
}
