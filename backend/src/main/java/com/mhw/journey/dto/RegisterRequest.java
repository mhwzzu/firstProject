package com.mhw.journey.dto;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
public class RegisterRequest {
    @Email @NotBlank private String email;
    @NotBlank @Size(max = 80) private String displayName;
    @NotBlank @Size(min = 8, max = 72) private String password;
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
