package com.re.it210project.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("sessionUser") != null);
        String uri = request.getRequestURI();

        // 1. Cho phép tài nguyên tĩnh VÀ favicon
        if (uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/") || uri.equals("/favicon.ico")) {
            return true;
        }

        // 2. Cho phép trang Login/Register
        if (uri.equals("/auth/login") || uri.equals("/auth/register")) {
            if (isLoggedIn) {
                response.sendRedirect("/");
                return false;
            }
            return true;
        }

        // 3. Nếu chưa đăng nhập thì mới chặn
        if (!isLoggedIn) {
            // Kiểm tra xem có phải đang gửi dữ liệu lên không, nếu POST thì cho qua để Controller xử lý
            if ("POST".equalsIgnoreCase(request.getMethod())) {
                return true;
            }
            response.sendRedirect("/auth/login");
            return false;
        }

        return true;
    }
}