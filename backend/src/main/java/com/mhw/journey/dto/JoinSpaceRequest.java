package com.mhw.journey.dto;
import javax.validation.constraints.NotBlank;
public class JoinSpaceRequest { @NotBlank private String token; public String getToken(){return token;} public void setToken(String token){this.token=token;} }
