package com.re.it210project.controller;

import com.re.it210project.exception.BadRequestException;
import com.re.it210project.model.dto.MentoringBookingRequest;
import com.re.it210project.model.entity.Lecturer;
import com.re.it210project.repository.DepartmentRepository;
import com.re.it210project.repository.LecturerRepository;
import com.re.it210project.model.entity.SessionUser;
import com.re.it210project.service.AcademicHistoryService;
import com.re.it210project.service.MentoringService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final MentoringService mentoringService;
    private final AcademicHistoryService academicHistoryService;
    private final DepartmentRepository departmentRepository;
    private final LecturerRepository lecturerRepository;

    // Helper lấy sessionUser để sidebar không bị lỗi null
    private SessionUser getSessionUser(HttpSession session) {
        return (SessionUser) session.getAttribute("sessionUser");
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        SessionUser user = getSessionUser(session);
        if (user == null) return "redirect:/auth/login";
        model.addAttribute("sessionUser", user);
        return "pages/student/dashboard";
    }

    @GetMapping("/sessions")
    public String sessions(HttpSession session, Model model) {

        SessionUser user =
                (SessionUser) session.getAttribute("sessionUser");

        if (user == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("sessionUser", user);

        model.addAttribute(
                "sessions",
                mentoringService.findStudentSessions(user.getId())
        );

        return "pages/student/sessions";
    }

    @GetMapping("/sessions/book")
    public String bookingPage(HttpSession session, Model model) {

        SessionUser user = (SessionUser) session.getAttribute("sessionUser");
        if (user == null) return "redirect:/auth/login";

        model.addAttribute("sessionUser", user);
        model.addAttribute("bookingRequest", new MentoringBookingRequest());
        model.addAttribute("departments", departmentRepository.findAll());

        model.addAttribute("lecturers",
                lecturerRepository.findAllWithUserAndDepartment());

        return "pages/student/book-session";
    }

    @PostMapping("/sessions/book")
    public String book(
            @Valid @ModelAttribute("bookingRequest") MentoringBookingRequest request,
            BindingResult result,
            HttpSession session,
            Model model,
            RedirectAttributes ra
    ) {

        SessionUser user = getSessionUser(session);

        if (result.hasErrors()) {

            model.addAttribute("sessionUser", user);
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("lecturers",
                    lecturerRepository.findAllWithUserAndDepartment());

            return "pages/student/book-session";
        }

        try {

            mentoringService.book(request, user.getId());

            ra.addFlashAttribute(
                    "successMsg",
                    "Đặt lịch thành công!"
            );

            return "redirect:/student/sessions";

        } catch (BadRequestException e) {

            model.addAttribute("sessionUser", user);
            model.addAttribute("departments", departmentRepository.findAll());
            model.addAttribute("lecturers",
                    lecturerRepository.findAllWithUserAndDepartment());

            model.addAttribute("errorMsg", e.getMessage());

            return "pages/student/book-session";
        }
    }

    @PostMapping("/sessions/{id}/cancel")
    public String cancelSession(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes ra
    ) {

        SessionUser sessionUser =
                (SessionUser) session.getAttribute("sessionUser");

        if (sessionUser == null) {
            return "redirect:/auth/login";
        }

        mentoringService.cancelSession(id, sessionUser);

        ra.addFlashAttribute(
                "successMsg",
                "Đã hủy lịch tư vấn!"
        );

        return "redirect:/student/sessions";
    }

    @GetMapping("/history")
    public String history(HttpSession session, Model model) {
        SessionUser user = getSessionUser(session);
        if (user == null) return "redirect:/auth/login";

        model.addAttribute("sessionUser", user);
        model.addAttribute("active", "history");
        model.addAttribute("histories", academicHistoryService.getStudentHistory(user.getId()));
        return "pages/student/history";
    }

    // Thêm hàm này vào cuối file StudentController.java của bạn
    @GetMapping("/history/{id}")
    public String evaluationDetail(
            @PathVariable("id") Long sessionId,
            HttpSession session,
            Model model
    ) {
        SessionUser user = getSessionUser(session);
        if (user == null) return "redirect:/auth/login";

        // Lấy thông tin chi tiết đánh giá từ Service
        // Bạn có thể dùng EvaluationService hoặc AcademicHistoryService tùy cấu trúc logic
        model.addAttribute("sessionUser", user);
        model.addAttribute("evaluation", academicHistoryService.getEvaluationDetail(sessionId));

        return "pages/student/evaluation-detail";
    }

    @GetMapping("/history/{id}/borrowing")
    public String borrowingDetail(
            @PathVariable("id") Long sessionId,
            HttpSession session,
            Model model
    ) {
        SessionUser user = getSessionUser(session);
        if (user == null) return "redirect:/auth/login";

        model.addAttribute("sessionUser", user);
        // Lấy thông tin đánh giá để có quan hệ sang BorrowingRecord
        model.addAttribute("evaluation", academicHistoryService.getEvaluationDetail(sessionId));

        return "pages/student/borrowing-detail";
    }
}