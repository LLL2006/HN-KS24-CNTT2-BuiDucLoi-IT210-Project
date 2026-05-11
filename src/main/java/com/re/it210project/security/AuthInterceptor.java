package com.re.it210project.security;

import com.re.it210project.model.entity.SessionUser;
import com.re.it210project.model.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        SessionUser user = (session != null) ? (SessionUser) session.getAttribute("sessionUser") : null;
        String uri = request.getRequestURI();

        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/") || uri.equals("/favicon.ico")) {
            return true;
        }

        if (uri.startsWith("/auth/")) {
            if (user != null) {
                response.sendRedirect("/");
                return false;
            }
            return true;
        }

        if (user == null) {
            response.sendRedirect("/auth/login");
            return false;
        }

        if (uri.startsWith("/admin/") && user.getRole() != Role.ADMIN) {
            response.sendRedirect("/access-denied");
            return false;
        }

        if (uri.startsWith("/lecturer/") && user.getRole() != Role.LECTURER) {
            response.sendRedirect("/access-denied");
            return false;
        }

        if (uri.startsWith("/student/") && user.getRole() != Role.STUDENT) {
            response.sendRedirect("/access-denied");
            return false;
        }

        return true;
    }
}