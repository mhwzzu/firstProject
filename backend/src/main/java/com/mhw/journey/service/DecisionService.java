package com.mhw.journey.service;

import com.mhw.journey.dto.CreatePlanRequest;
import com.mhw.journey.dto.WishResponse;
import com.mhw.journey.model.*;
import com.mhw.journey.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class DecisionService {
    private final RecommendationCandidateRepository candidates;private final RecommendationRunRepository runs;private final CandidateFeedbackRepository feedback;private final JourneyPlanRepository plans;private final MemoryRepository memories;private final SpaceMembershipRepository memberships;private final PlaceRepository places;
    private static final Set<String> ACTIONS=new HashSet<>(Arrays.asList("WANT","SKIP","DISLIKE","VISITED"));
    public DecisionService(RecommendationCandidateRepository candidates,RecommendationRunRepository runs,CandidateFeedbackRepository feedback,JourneyPlanRepository plans,MemoryRepository memories,SpaceMembershipRepository memberships,PlaceRepository places){this.candidates=candidates;this.runs=runs;this.feedback=feedback;this.plans=plans;this.memories=memories;this.memberships=memberships;this.places=places;}
    public CandidateFeedback feedback(Long userId,Long spaceId,Long candidateId,String action){if(!ACTIONS.contains(action))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"不支持的反馈动作"); verify(spaceId,candidateId);CandidateFeedback item=feedback.findByCandidateIdAndUserId(candidateId,userId).orElseGet(CandidateFeedback::new);item.setCandidateId(candidateId);item.setUserId(userId);item.setAction(action);return feedback.save(item);}
    public JourneyPlan plan(Long spaceId,CreatePlanRequest request){RecommendationCandidate candidate=verify(spaceId,request.getCandidateId());JourneyPlan plan=new JourneyPlan();plan.setSpaceId(spaceId);plan.setCandidateId(candidate.getId());plan.setTitle(candidate.getTitle());plan.setCity(candidate.getCity());plan.setStartDate(request.getStartDate());plan.setEndDate(request.getEndDate());plan.setBudget(request.getBudget());plan.setNote(request.getNote()==null?"":request.getNote().trim());return plans.save(plan);}
    public List<JourneyPlan> plans(Long spaceId){return plans.findBySpaceIdOrderByCreatedAtDesc(spaceId);}
    public List<WishResponse> wishes(Long userId,Long spaceId){
        List<Long> memberIds=new ArrayList<>();for(SpaceMembership membership:memberships.findBySpaceIdOrderByJoinedAtAsc(spaceId))memberIds.add(membership.getUserId());
        Map<Long,List<CandidateFeedback>> grouped=new LinkedHashMap<>();
        for(CandidateFeedback item:feedback.findByUserIdInAndActionOrderByCreatedAtDesc(memberIds,"WANT"))grouped.computeIfAbsent(item.getCandidateId(),key->new ArrayList<>()).add(item);
        List<WishResponse> output=new ArrayList<>();
        for(Map.Entry<Long,List<CandidateFeedback>> entry:grouped.entrySet()){
            Optional<RecommendationCandidate> candidateValue=candidates.findById(entry.getKey());if(!candidateValue.isPresent())continue;
            RecommendationCandidate candidate=candidateValue.get();Optional<RecommendationRun> run=runs.findById(candidate.getRunId());if(!run.isPresent()||!spaceId.equals(run.get().getSpaceId()))continue;
            Place place=candidate.getPlaceId()==null?null:places.findById(candidate.getPlaceId()).orElse(null);boolean mine=false;LocalDateTime savedAt=null;
            for(CandidateFeedback item:entry.getValue()){if(userId.equals(item.getUserId()))mine=true;if(savedAt==null||item.getCreatedAt().isAfter(savedAt))savedAt=item.getCreatedAt();}
            output.add(new WishResponse(candidate.getId(),candidate.getTitle(),candidate.getCity(),candidate.getCategory(),candidate.getScore(),candidate.getSourceUrl(),
                    place==null?null:place.getDistrict(),place==null?null:place.getAddress(),place==null?null:place.getLatitude(),place==null?null:place.getLongitude(),mine,entry.getValue().size()>1,entry.getValue().size(),savedAt));
        }
        return output;
    }
    public JourneyPlan complete(Long spaceId,Long planId){JourneyPlan plan=plans.findById(planId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"计划不存在"));if(!spaceId.equals(plan.getSpaceId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"无权修改该计划");if("COMPLETED".equals(plan.getStatus()))return plan;plan.setStatus("COMPLETED");plans.save(plan);
        Memory memory=new Memory();memory.setSpaceId(spaceId);memory.setPlanId(plan.getId());memory.setTitle(plan.getTitle());memory.setCity(plan.getCity());memory.setType(MemoryType.TRIP);memory.setVisitedAt(plan.getEndDate()!=null?plan.getEndDate():LocalDate.now());memory.setRating(5);memory.setNote(plan.getNote());memory.setTags("共同计划");memory.setEmoji("✨");memory.setCategory("TRIP");memories.save(memory);return plan;}
    private RecommendationCandidate verify(Long spaceId,Long candidateId){RecommendationCandidate candidate=candidates.findById(candidateId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"推荐候选不存在"));RecommendationRun run=runs.findById(candidate.getRunId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"推荐快照不存在"));if(!spaceId.equals(run.getSpaceId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"无权访问该候选");return candidate;}
}
