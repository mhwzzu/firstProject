package com.mhw.journey.dto;

import java.time.LocalDateTime;

public class WishResponse {
    private final Long candidateId; private final String title; private final String city; private final String category;
    private final Integer score; private final String sourceUrl; private final String district; private final String address;
    private final Double latitude; private final Double longitude; private final boolean mine; private final boolean shared;
    private final int heartCount; private final LocalDateTime savedAt;
    public WishResponse(Long candidateId,String title,String city,String category,Integer score,String sourceUrl,String district,
                        String address,Double latitude,Double longitude,boolean mine,boolean shared,int heartCount,LocalDateTime savedAt){
        this.candidateId=candidateId;this.title=title;this.city=city;this.category=category;this.score=score;this.sourceUrl=sourceUrl;
        this.district=district;this.address=address;this.latitude=latitude;this.longitude=longitude;this.mine=mine;
        this.shared=shared;this.heartCount=heartCount;this.savedAt=savedAt;
    }
    public Long getCandidateId(){return candidateId;} public String getTitle(){return title;} public String getCity(){return city;}
    public String getCategory(){return category;} public Integer getScore(){return score;} public String getSourceUrl(){return sourceUrl;}
    public String getDistrict(){return district;} public String getAddress(){return address;} public Double getLatitude(){return latitude;}
    public Double getLongitude(){return longitude;} public boolean isMine(){return mine;} public boolean isShared(){return shared;}
    public int getHeartCount(){return heartCount;} public LocalDateTime getSavedAt(){return savedAt;}
}
