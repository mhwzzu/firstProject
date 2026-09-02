package com.mhw.journey.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "space_invites")
public class Invite {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long spaceId;
    @Column(nullable = false, unique = true, length = 100) private String tokenHash;
    @Column(nullable = false) private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void created() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Long getSpaceId() { return spaceId; }
    public void setSpaceId(Long spaceId) { this.spaceId = spaceId; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public LocalDateTime getUsedAt() { return usedAt; }
    public void setUsedAt(LocalDateTime usedAt) { this.usedAt = usedAt; }
}
