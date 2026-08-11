package com.mhw.journey.model;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "memories")
public class Memory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemoryType type;

    @NotBlank
    @Column(nullable = false)
    private String city;

    private String address;

    @NotNull
    @Column(nullable = false)
    private LocalDate visitedAt;

    @Min(1)
    @Max(5)
    private Integer rating;

    @Column(length = 1000)
    private String note;

    private String tags;
    private String emoji;
    private String category;
    private String specialty;
    private String placeId;
    private String province;
    private String district;
    private Double latitude;
    private Double longitude;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public MemoryType getType() { return type; }
    public void setType(MemoryType type) { this.type = type; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDate getVisitedAt() { return visitedAt; }
    public void setVisitedAt(LocalDate visitedAt) { this.visitedAt = visitedAt; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public String getPlaceId() { return placeId; }
    public void setPlaceId(String placeId) { this.placeId = placeId; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
