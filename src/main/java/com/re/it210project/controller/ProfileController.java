package com.re.it210project.controller;

import com.re.it210project.model.dto.ProfileUpdateRequest;
import com.re.it210project.model.entity.User;
import com.re.it210project.model.entity.UserProfile;
import com.re.it210project.repository.UserProfileRepository;
import com.re.it210project.repository.UserRepository;
import com.re.it210project.model.entity.SessionUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @GetMapping
    public String profile(
            HttpSession session,
            Model model
    ) {

        SessionUser sessionUser =
                (SessionUser) session.getAttribute("sessionUser");

        User user = userRepository.findById(sessionUser.getId())
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy user"));

        UserProfile profile = userProfileRepository
                .findByUserId(sessionUser.getId())
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy profile"));

        model.addAttribute("sessionUser", sessionUser);
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);

        return "pages/profile/view";
    }

    @GetMapping("/edit")
    public String editPage(
            HttpSession session,
            Model model
    ) {

        SessionUser sessionUser =
                (SessionUser) session.getAttribute("sessionUser");

        UserProfile profile = userProfileRepository
                .findByUserId(sessionUser.getId())
                .orElseThrow();

        ProfileUpdateRequest request =
                new ProfileUpdateRequest();

        request.setFullName(profile.getFullName());
        request.setPhone(profile.getPhone());
        request.setAddress(profile.getAddress());

        model.addAttribute("sessionUser", sessionUser);
        model.addAttribute("profileRequest", request);

        return "pages/profile/edit";
    }

    @PostMapping("/edit")
    public String updateProfile(
            @Valid
            @ModelAttribute("profileRequest")
            ProfileUpdateRequest request,
            BindingResult result,
            HttpSession session,
            RedirectAttributes ra
    ) {

        SessionUser sessionUser =
                (SessionUser) session.getAttribute("sessionUser");

        if (result.hasErrors()) {
            return "pages/profile/edit";
        }

        UserProfile profile = userProfileRepository
                .findByUserId(sessionUser.getId())
                .orElseThrow();

        profile.setFullName(request.getFullName());
        profile.setPhone(request.getPhone());
        profile.setAddress(request.getAddress());

        userProfileRepository.save(profile);

        sessionUser.setFullName(request.getFullName());

        session.setAttribute("sessionUser", sessionUser);

        ra.addFlashAttribute(
                "successMsg",
                "Cập nhật hồ sơ thành công!"
        );

        return "redirect:/profile";
    }
}