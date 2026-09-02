package com.mhw.journey.dto;
public class CurrentUserResponse {
    private Long id; private String email; private String displayName; private boolean authenticated;
    public CurrentUserResponse() { }
    public CurrentUserResponse(Long id, String email, String displayName, boolean authenticated) { this.id=id; this.email=email; this.displayName=displayName; this.authenticated=authenticated; }
    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public boolean isAuthenticated() { return authenticated; }
}
