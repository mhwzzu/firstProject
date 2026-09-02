package com.mhw.journey.dto;
import java.util.List;
public class SpaceResponse {
    private Long id; private String name; private String defaultCity; private List<Member> members;
    public SpaceResponse(Long id, String name, String defaultCity, List<Member> members) { this.id=id; this.name=name; this.defaultCity=defaultCity; this.members=members; }
    public Long getId(){return id;} public String getName(){return name;} public String getDefaultCity(){return defaultCity;} public List<Member> getMembers(){return members;}
    public static class Member { private Long id; private String displayName; private String role; public Member(Long id,String displayName,String role){this.id=id;this.displayName=displayName;this.role=role;} public Long getId(){return id;} public String getDisplayName(){return displayName;} public String getRole(){return role;} }
}
