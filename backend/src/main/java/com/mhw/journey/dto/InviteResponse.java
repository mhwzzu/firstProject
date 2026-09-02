package com.mhw.journey.dto;
import java.time.LocalDateTime;
public class InviteResponse { private String token; private LocalDateTime expiresAt; public InviteResponse(String token,LocalDateTime expiresAt){this.token=token;this.expiresAt=expiresAt;} public String getToken(){return token;} public LocalDateTime getExpiresAt(){return expiresAt;} }
