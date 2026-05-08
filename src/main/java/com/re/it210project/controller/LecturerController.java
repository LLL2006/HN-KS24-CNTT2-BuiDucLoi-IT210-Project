package com.re.it210project.controller;

import com.re.it210project.model.dto.EvaluationRequest;
import com.re.it210project.model.entity.SessionUser;
import com.re.it210project.service.EquipmentService;
import com.re.it210project.service.EvaluationService;
import com.re.it210project.service.MentoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/lecturer")
@RequiredArgsConstructor
public class LecturerController {

    private final MentoringService mentoringService;
    private final EvaluationService evaluationService;
    private final EquipmentService equipmentService;

    @GetMapping("/sessions")
    public String pendingSessions(
            Model model,
            @RequestAttribute("user") SessionUser user
    ) {

        model.addAttribute(
                "sessions",
                mentoringService.findPendingSessionsForLecturer(user.getId())
        );

        return "pages/lecturer/sessions";
    }

    @GetMapping("/sessions/{id}/evaluate")
    public String evaluatePage(
            @PathVariable Long id,
            Model model
    ) {

        EvaluationRequest request = new EvaluationRequest();
        request.setSessionId(id);

        model.addAttribute("evaluationRequest", request);

        model.addAttribute(
                "equipments",
                equipmentService.findActive()
        );

        return "pages/lecturer/evaluate";
    }

    @PostMapping("/evaluations")
    public String evaluate(
            @Valid @ModelAttribute EvaluationRequest request,
            BindingResult result,
            @RequestAttribute("user") SessionUser user,
            Model model,
            RedirectAttributes ra
    ) {

        if (result.hasErrors()) {

            model.addAttribute(
                    "equipments",
                    equipmentService.findActive()
            );

            return "pages/lecturer/evaluate";
        }

        evaluationService.completeEvaluation(
                request,
                user.getId()
        );

        ra.addFlashAttribute(
                "success",
                "Đã lưu đánh giá thành công!"
        );

        return "redirect:/lecturer/sessions";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "pages/lecturer/dashboard";
    }
}