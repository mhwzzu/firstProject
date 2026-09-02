package com.mhw.journey.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_feedback", uniqueConstraints = @UniqueConstraint(columnNames = {"candidateId", "userId"}))
public class CandidateFeedback {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long candidateId;
    @Column(nullable = false) private Long userId;
    @Column(nullable = false, length = 24) private String action;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void created() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
