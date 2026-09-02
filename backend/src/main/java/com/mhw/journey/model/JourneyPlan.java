package com.mhw.journey.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "journey_plans")
public class JourneyPlan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long spaceId;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, length = 80) private String city;
    private Long candidateId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer budget;
    @Column(nullable = false, length = 20) private String status = "PLANNED";
    @Column(length = 2000) private String note;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void created() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Long getSpaceId() { return spaceId; }
    public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public Long getCandidateId() { return candidateId; }
    public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getBudget() { return budget; }
    public void setBudget(Integer budget) { this.budget = budget; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
