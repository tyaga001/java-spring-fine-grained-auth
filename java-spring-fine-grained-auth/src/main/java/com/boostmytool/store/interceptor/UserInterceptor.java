package com.boostmytool.store.interceptor;

import com.boostmytool.store.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        var header = request.getHeader("Authorization");
        String userKey = header == null ? null : header.replaceFirst("Bearer ", "");
        if (userKey != null && !userKey.isEmpty()) {
            request.setAttribute("user", userService.login(userKey));
        }
        return true;
    }
}
