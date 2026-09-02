package com.mhw.journey.service;

import com.mhw.journey.config.AuthInterceptor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Service
public class SessionService {
    public Long requireUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute(AuthInterceptor.SESSION_USER);
        if (!(value instanceof Long)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先登录");
        return (Long) value;
    }
}
