package com.mhw.journey.dto;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
public class CreateSpaceRequest {
    @NotBlank @Size(max=100) private String name;
    @NotBlank @Size(max=80) private String city;
    public String getName() { return name; }
    public void setName(String name) { this.name=name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city=city; }
}
