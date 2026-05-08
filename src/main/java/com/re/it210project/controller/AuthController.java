package com.re.it210project.controller;

import com.re.it210project.model.dto.AuthRequest;
import com.re.it210project.model.dto.RegisterRequest;
import com.re.it210project.repository.DepartmentRepository;
import com.re.it210project.model.entity.SessionUser;
import com.re.it210project.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final DepartmentRepository departmentRepository;

    @GetMapping("/login")
    public String loginPage(Model model) {
        if (!model.containsAttribute("authRequest")) {
            model.addAttribute("authRequest", new AuthRequest());
        }
        return "pages/auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("authRequest") AuthRequest request,
                        BindingResult result, HttpSession session, Model model) {


        if (result.hasErrors()) return "pages/auth/login";

        try {
            SessionUser sessionUser = authService.login(request);
            session.setAttribute("sessionUser", sessionUser);
            return "redirect:/";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "pages/auth/login";
        }
    }

    @GetMapping("/register")
    public String registerPage(Model model) {

        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }

        model.addAttribute("departments", departmentRepository.findAll());

        return "pages/auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid RegisterRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes ra) {

        if (result.hasErrors()) {
            model.addAttribute("departments", departmentRepository.findAll());
            return "pages/auth/register";
        }

        try {

            authService.register(request);

            ra.addFlashAttribute(
                    "success",
                    "Đăng ký thành công! Hãy đăng nhập.");

            return "redirect:/auth/login";

        } catch (RuntimeException e) {

            model.addAttribute("error", e.getMessage());
            model.addAttribute("departments", departmentRepository.findAll());

            return "pages/auth/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/auth/login?logout";
    }
}