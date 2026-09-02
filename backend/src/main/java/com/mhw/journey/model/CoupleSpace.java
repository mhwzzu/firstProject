package com.mhw.journey.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "couple_spaces")
public class CoupleSpace {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 80)
    private String defaultCity;
    @Column(nullable = false)
    private Long createdByUserId;
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist void created() { if (createdAt == null) createdAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDefaultCity() { return defaultCity; }
    public void setDefaultCity(String defaultCity) { this.defaultCity = defaultCity; }
    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
