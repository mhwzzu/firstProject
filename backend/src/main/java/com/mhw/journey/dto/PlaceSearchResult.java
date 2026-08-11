package com.mhw.journey.dto;

public class PlaceSearchResult {
    private String id;
    private String name;
    private String province;
    private String city;
    private String district;
    private String address;
    private Double latitude;
    private Double longitude;

    public PlaceSearchResult(String id, String name, String province, String city, String district,
                             String address, Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.province = province;
        this.city = city;
        this.district = district;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
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
}
