package com.mhw.journey.controller;

import com.mhw.journey.config.AuthInterceptor;
import com.mhw.journey.dto.LoginRequest;
import com.mhw.journey.dto.LoginResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final String configuredUsername;
    private final String configuredPassword;

    public AuthController(
            @Value("${app.auth.username}") String configuredUsername,
            @Value("${app.auth.password}") String configuredPassword) {
        this.configuredUsername = configuredUsername;
        this.configuredPassword = configuredPassword;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        if (!configuredUsername.equals(request.getUsername()) || !configuredPassword.equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "账号或密码不正确");
        }
        session.setAttribute(AuthInterceptor.SESSION_USER, configuredUsername);
        return new LoginResponse(configuredUsername);
    }

    @GetMapping("/me")
    public LoginResponse me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return new LoginResponse(null);
        }
        Object username = session.getAttribute(AuthInterceptor.SESSION_USER);
        return username == null ? new LoginResponse(null) : new LoginResponse(username.toString());
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {
        session.invalidate();
        return Collections.singletonMap("message", "已退出登录");
    }
}
