package com.mhw.journey.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "space_memberships", uniqueConstraints = @UniqueConstraint(columnNames = {"spaceId", "userId"}))
public class SpaceMembership {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private Long spaceId;
    @Column(nullable = false) private Long userId;
    @Column(nullable = false, length = 20) private String role;
    @Column(nullable = false) private LocalDateTime joinedAt;
    @PrePersist void joined() { if (joinedAt == null) joinedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Long getSpaceId() { return spaceId; }
    public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
}
