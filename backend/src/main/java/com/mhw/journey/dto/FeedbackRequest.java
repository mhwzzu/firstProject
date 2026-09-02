package com.mhw.journey.dto;
import javax.validation.constraints.NotBlank;
public class FeedbackRequest { @NotBlank private String action; public String getAction(){return action;} public void setAction(String action){this.action=action;} }
