package com.mhw.journey.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_accounts", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 160)
    private String email;
    @Column(nullable = false, length = 80)
    private String displayName;
    @Column(nullable = false, length = 100)
    private String passwordHash;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist void created() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email == null ? null : email.trim().toLowerCase(); }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
