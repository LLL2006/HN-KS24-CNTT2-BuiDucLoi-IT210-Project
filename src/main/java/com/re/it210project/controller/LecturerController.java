package com.re.it210project.controller;

import com.re.it210project.exception.BadRequestException;
import com.re.it210project.model.dto.EvaluationRequest;
import com.re.it210project.model.entity.MentoringSession;
import com.re.it210project.model.entity.SessionUser;
import com.re.it210project.model.enums.SessionStatus;
import com.re.it210project.repository.MentoringSessionRepository;
import com.re.it210project.service.EquipmentService;
import com.re.it210project.service.EvaluationService;
import com.re.it210project.service.MentoringService;
import jakarta.servlet.http.HttpSession;
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
    private final MentoringSessionRepository mentoringSessionRepository;

    @GetMapping("/sessions")
    public String pendingSessions(
            Model model,
            HttpSession session
    ) {

        SessionUser user =
                (SessionUser) session.getAttribute("sessionUser");

        model.addAttribute(
                "sessionUser",
                user
        );

        model.addAttribute(
                "sessions",
                mentoringService.findPendingSessionsForLecturer(user.getId())
        );

        return "pages/lecturer/sessions";
    }

    @GetMapping("/evaluations/{id}")
    public String viewEvaluationDetail(@PathVariable Long id, HttpSession session, Model model) {
        SessionUser user = (SessionUser) session.getAttribute("sessionUser");
        model.addAttribute("sessionUser", user);

        // Lấy đúng 1 đối tượng session duy nhất để hiển thị chi tiết [cite: 62]
        MentoringSession mentoringSession = mentoringService.findById(id);
        model.addAttribute("s", mentoringSession);

        return "pages/lecturer/evaluation-detail";
    }

    @PostMapping("/sessions/{id}/reject")
    public String rejectSession(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes ra
    ) {

        SessionUser user =
                (SessionUser) session.getAttribute("sessionUser");

        mentoringService.rejectSession(
                id,
                user.getId()
        );

        ra.addFlashAttribute(
                "success",
                "Đã từ chối ca tư vấn"
        );

        return "redirect:/lecturer/sessions";
    }

    @GetMapping("/sessions/{id}/evaluate")
    public String evaluatePage(
            @PathVariable Long id,
            HttpSession session,
            Model model
    ) {

        SessionUser user =
                (SessionUser) session.getAttribute("sessionUser");

        EvaluationRequest request = new EvaluationRequest();
        request.setSessionId(id);

        model.addAttribute("sessionUser", user);

        model.addAttribute(
                "evaluationRequest",
                request
        );

        model.addAttribute(
                "equipments",
                equipmentService.findActive()
        );

        return "pages/lecturer/evaluate";
    }

    @PostMapping("/evaluations")
    public String evaluate(
            @Valid @ModelAttribute("evaluationRequest") EvaluationRequest request,
            BindingResult result,
            HttpSession session,
            Model model,
            RedirectAttributes ra
    ) {
        SessionUser user = (SessionUser) session.getAttribute("sessionUser");
        model.addAttribute("sessionUser", user);

        if (result.hasErrors()) {
            model.addAttribute("equipments", equipmentService.findActive());
            return "pages/lecturer/evaluate";
        }

        try {
            evaluationService.completeEvaluation(request, user.getId());
            ra.addFlashAttribute("success", "Đã lưu đánh giá thành công!");
            return "redirect:/lecturer/sessions";

        } catch (BadRequestException e) {
            // KHÔNG DÙNG REDIRECT KHI LỖI LOGIC ĐỂ GIỮ DỮ LIỆU
            model.addAttribute("error", e.getMessage());
            model.addAttribute("equipments", equipmentService.findActive());
            // Giữ nguyên request để các ô input không bị trống
            model.addAttribute("evaluationRequest", request);
            return "pages/lecturer/evaluate";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            model.addAttribute("equipments", equipmentService.findActive());
            return "pages/lecturer/evaluate";
        }
    }

    @GetMapping("/evaluations")
    public String evaluations(HttpSession session, Model model) {
        SessionUser user =
                (SessionUser) session.getAttribute("sessionUser");

        model.addAttribute("sessionUser", user);

        // Tìm các ca có trạng thái CONFIRMED hoặc COMPLETED
        // Bạn nên tạo một hàm mới trong MentoringService để lấy cả 2 trạng thái này
        model.addAttribute("sessions",
                mentoringService.findSessionsForEvaluationPage(user.getId())
        );

        return "pages/lecturer/evaluations";
    }

    @GetMapping("/dashboard")
    public String dashboard(
            HttpSession session,
            Model model
    ) {

        SessionUser user =
                (SessionUser) session.getAttribute("sessionUser");

        model.addAttribute("sessionUser", user);

        model.addAttribute(
                "pendingCount",
                mentoringSessionRepository
                        .countByLecturerUserIdAndStatus(
                                user.getId(),
                                SessionStatus.PENDING
                        )
        );

        model.addAttribute(
                "confirmedCount",
                mentoringSessionRepository
                        .countByLecturerUserIdAndStatus(
                                user.getId(),
                                SessionStatus.CONFIRMED
                        )
        );

        model.addAttribute(
                "completedCount",
                mentoringSessionRepository
                        .countByLecturerUserIdAndStatus(
                                user.getId(),
                                SessionStatus.COMPLETED
                        )
        );

        return "pages/lecturer/dashboard";
    }

    @PostMapping("/sessions/{id}/accept")
    public String acceptSession(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        try {
            SessionUser user = (SessionUser) session.getAttribute("sessionUser");
            mentoringService.acceptSession(id, user.getId());
            ra.addFlashAttribute("success", "Đã tiếp nhận ca tư vấn");
        } catch (Exception e) {
            // Log lỗi ra console để debug
            System.err.println("Lỗi tiếp nhận: " + e.getMessage());
            ra.addFlashAttribute("error", e.getMessage()); // Sẽ hiện thông báo đỏ trên web
        }
        return "redirect:/lecturer/sessions";
    }
}