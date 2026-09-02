package com.mhw.journey.service;

import com.mhw.journey.dto.CreateSpaceRequest;
import com.mhw.journey.dto.SpaceResponse;
import com.mhw.journey.model.*;
import com.mhw.journey.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class SpaceService {
    private final CoupleSpaceRepository spaces; private final SpaceMembershipRepository memberships;
    private final UserAccountRepository users; private final PreferenceProfileRepository preferences;
    private final InviteRepository invites;
    public SpaceService(CoupleSpaceRepository spaces, SpaceMembershipRepository memberships, UserAccountRepository users, PreferenceProfileRepository preferences, InviteRepository invites) {
        this.spaces=spaces; this.memberships=memberships; this.users=users; this.preferences=preferences; this.invites=invites;
    }
    public CoupleSpace create(Long userId, CreateSpaceRequest request) {
        if (memberships.findFirstByUserId(userId).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "每位成员在首发版本中只能加入一个双人空间");
        CoupleSpace space=new CoupleSpace(); space.setName(request.getName().trim()); space.setDefaultCity(request.getCity().trim()); space.setCreatedByUserId(userId); spaces.save(space);
        SpaceMembership membership=new SpaceMembership(); membership.setSpaceId(space.getId()); membership.setUserId(userId); membership.setRole("OWNER"); memberships.save(membership);
        PreferenceProfile profile=new PreferenceProfile(); profile.setSpaceId(space.getId()); profile.setUserId(userId); profile.setCity(space.getDefaultCity()); preferences.save(profile);
        return space;
    }
    public CoupleSpace requireCurrentSpace(Long userId) {
        SpaceMembership membership=memberships.findFirstByUserId(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "请先创建或加入一个双人空间"));
        return spaces.findById(membership.getSpaceId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "双人空间不存在"));
    }
    public void requireMember(Long userId, Long spaceId) {
        if (!memberships.findBySpaceIdAndUserId(spaceId, userId).isPresent()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "你无权访问这个双人空间");
    }
    public SpaceResponse response(CoupleSpace space) {
        List<SpaceResponse.Member> members=new ArrayList<>();
        for (SpaceMembership membership : memberships.findBySpaceIdOrderByJoinedAtAsc(space.getId())) {
            UserAccount user=users.findById(membership.getUserId()).orElse(null);
            if (user != null) members.add(new SpaceResponse.Member(user.getId(), user.getDisplayName(), membership.getRole()));
        }
        return new SpaceResponse(space.getId(), space.getName(), space.getDefaultCity(), members);
    }
    public String createInvite(Long userId, CoupleSpace space) {
        SpaceMembership membership=memberships.findBySpaceIdAndUserId(space.getId(), userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "无权创建邀请"));
        if (!"OWNER".equals(membership.getRole())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "只有创建者可以邀请另一位成员");
        if (memberships.findBySpaceIdOrderByJoinedAtAsc(space.getId()).size() >= 2) throw new ResponseStatusException(HttpStatus.CONFLICT, "这个双人空间已经有两位成员");
        byte[] bytes=new byte[24]; new SecureRandom().nextBytes(bytes); String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Invite invite=new Invite(); invite.setSpaceId(space.getId()); invite.setTokenHash(hash(token)); invite.setExpiresAt(LocalDateTime.now().plusHours(72)); invites.save(invite); return token;
    }
    public CoupleSpace join(Long userId, String token) {
        if (memberships.findFirstByUserId(userId).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "你已经加入一个双人空间");
        Invite invite=invites.findByTokenHash(hash(token)).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "邀请链接无效"));
        if (invite.getUsedAt()!=null || invite.getExpiresAt().isBefore(LocalDateTime.now())) throw new ResponseStatusException(HttpStatus.GONE, "邀请链接已失效");
        if (memberships.findBySpaceIdOrderByJoinedAtAsc(invite.getSpaceId()).size() >= 2) throw new ResponseStatusException(HttpStatus.CONFLICT, "这个双人空间已经满员");
        SpaceMembership membership=new SpaceMembership(); membership.setSpaceId(invite.getSpaceId()); membership.setUserId(userId); membership.setRole("MEMBER"); memberships.save(membership); invite.setUsedAt(LocalDateTime.now()); invites.save(invite);
        CoupleSpace space=spaces.findById(invite.getSpaceId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "双人空间不存在"));
        PreferenceProfile profile=new PreferenceProfile(); profile.setSpaceId(space.getId()); profile.setUserId(userId); profile.setCity(space.getDefaultCity()); preferences.save(profile); return space;
    }
    private String hash(String value) { try { byte[] digest=MessageDigest.getInstance("SHA-256").digest(value.getBytes("UTF-8")); StringBuilder b=new StringBuilder(); for(byte x:digest)b.append(String.format("%02x", x)); return b.toString(); } catch(Exception e){ throw new IllegalStateException(e); } }
}
