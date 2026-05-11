package com.re.it210project.controller;

import com.re.it210project.model.enums.Role;
import com.re.it210project.model.entity.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(HttpSession session) {
        SessionUser user =
                (SessionUser) session.getAttribute("sessionUser");

        if (user == null) {
            return "redirect:/auth/login";
        }

        if (user.getRole() == Role.ADMIN) {
            return "redirect:/admin/dashboard";
        }

        if (user.getRole() == Role.LECTURER) {
            return "redirect:/lecturer/dashboard";
        }

        return "redirect:/student/dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied(HttpSession session, Model model) {
        SessionUser user = (SessionUser) session.getAttribute("sessionUser");
        model.addAttribute("sessionUser", user);
        return "error/403";
    }
}