package com.mhw.journey.model;

import javax.persistence.*;

@Entity
@Table(name = "places", uniqueConstraints = @UniqueConstraint(columnNames = {"source", "externalId"}))
public class Place {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 30) private String source;
    @Column(nullable = false, length = 160) private String externalId;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, length = 80) private String city;
    private String district;
    @Column(length = 300) private String address;
    private Double latitude;
    private Double longitude;
    @Column(length = 80) private String category;
    public Long getId() { return id; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
