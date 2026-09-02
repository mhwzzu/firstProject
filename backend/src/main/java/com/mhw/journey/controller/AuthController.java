package com.mhw.journey.controller;

import com.mhw.journey.config.AuthInterceptor;
import com.mhw.journey.dto.CurrentUserResponse;
import com.mhw.journey.dto.EmailLoginRequest;
import com.mhw.journey.dto.RegisterRequest;
import com.mhw.journey.model.UserAccount;
import com.mhw.journey.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final UserAccountRepository users;
    private final PasswordEncoder encoder;

    public AuthController(UserAccountRepository users, PasswordEncoder encoder) { this.users = users; this.encoder = encoder; }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public CurrentUserResponse register(@Valid @RequestBody RegisterRequest request, HttpSession session) {
        String email = request.getEmail().trim().toLowerCase();
        if (users.findByEmail(email).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "该邮箱已注册，请直接登录");
        UserAccount user = new UserAccount();
        user.setEmail(email); user.setDisplayName(request.getDisplayName().trim()); user.setPasswordHash(encoder.encode(request.getPassword()));
        users.save(user); session.setAttribute(AuthInterceptor.SESSION_USER, user.getId());
        return response(user);
    }
    @PostMapping("/login")
    public CurrentUserResponse login(@Valid @RequestBody EmailLoginRequest request, HttpSession session) {
        UserAccount user = users.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "邮箱或密码不正确"));
        if (!encoder.matches(request.getPassword(), user.getPasswordHash())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "邮箱或密码不正确");
        session.setAttribute(AuthInterceptor.SESSION_USER, user.getId());
        return response(user);
    }

    @GetMapping("/me")
    public CurrentUserResponse me(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object id = session == null ? null : session.getAttribute(AuthInterceptor.SESSION_USER);
        if (!(id instanceof Long)) return new CurrentUserResponse(null, null, null, false);
        UserAccount user = users.findById((Long) id).orElse(null);
        return user == null ? new CurrentUserResponse(null, null, null, false) : response(user);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return Collections.singletonMap("message", "已退出登录");
    }

    private CurrentUserResponse response(UserAccount user) { return new CurrentUserResponse(user.getId(), user.getEmail(), user.getDisplayName(), true); }
}
