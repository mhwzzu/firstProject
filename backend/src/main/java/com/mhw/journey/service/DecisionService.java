package com.mhw.journey.service;

import com.mhw.journey.dto.CreatePlanRequest;
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

@Service
public class DecisionService {
    private final RecommendationCandidateRepository candidates;private final RecommendationRunRepository runs;private final CandidateFeedbackRepository feedback;private final JourneyPlanRepository plans;private final MemoryRepository memories;
    private static final Set<String> ACTIONS=new HashSet<>(Arrays.asList("WANT","SKIP","VOTE","VISITED"));
    public DecisionService(RecommendationCandidateRepository candidates,RecommendationRunRepository runs,CandidateFeedbackRepository feedback,JourneyPlanRepository plans,MemoryRepository memories){this.candidates=candidates;this.runs=runs;this.feedback=feedback;this.plans=plans;this.memories=memories;}
    public CandidateFeedback feedback(Long userId,Long spaceId,Long candidateId,String action){if(!ACTIONS.contains(action))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"不支持的反馈动作"); verify(spaceId,candidateId);CandidateFeedback item=feedback.findByCandidateIdAndUserId(candidateId,userId).orElseGet(CandidateFeedback::new);item.setCandidateId(candidateId);item.setUserId(userId);item.setAction(action);return feedback.save(item);}
    public JourneyPlan plan(Long spaceId,CreatePlanRequest request){RecommendationCandidate candidate=verify(spaceId,request.getCandidateId());JourneyPlan plan=new JourneyPlan();plan.setSpaceId(spaceId);plan.setCandidateId(candidate.getId());plan.setTitle(candidate.getTitle());plan.setCity(candidate.getCity());plan.setStartDate(request.getStartDate());plan.setEndDate(request.getEndDate());plan.setBudget(request.getBudget());plan.setNote(request.getNote()==null?"":request.getNote().trim());return plans.save(plan);}
    public List<JourneyPlan> plans(Long spaceId){return plans.findBySpaceIdOrderByCreatedAtDesc(spaceId);}
    public JourneyPlan complete(Long spaceId,Long planId){JourneyPlan plan=plans.findById(planId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"计划不存在"));if(!spaceId.equals(plan.getSpaceId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"无权修改该计划");if("COMPLETED".equals(plan.getStatus()))return plan;plan.setStatus("COMPLETED");plans.save(plan);
        Memory memory=new Memory();memory.setSpaceId(spaceId);memory.setPlanId(plan.getId());memory.setTitle(plan.getTitle());memory.setCity(plan.getCity());memory.setType(MemoryType.TRIP);memory.setVisitedAt(plan.getEndDate()!=null?plan.getEndDate():LocalDate.now());memory.setRating(5);memory.setNote(plan.getNote());memory.setTags("共同计划");memory.setEmoji("✨");memory.setCategory("TRIP");memories.save(memory);return plan;}
    private RecommendationCandidate verify(Long spaceId,Long candidateId){RecommendationCandidate candidate=candidates.findById(candidateId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"推荐候选不存在"));RecommendationRun run=runs.findById(candidate.getRunId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"推荐快照不存在"));if(!spaceId.equals(run.getSpaceId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"无权访问该候选");return candidate;}
}
