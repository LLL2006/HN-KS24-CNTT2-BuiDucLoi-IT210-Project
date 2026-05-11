package com.re.it210project.controller;

import com.re.it210project.model.dto.EquipmentRequest;
import com.re.it210project.model.entity.BorrowingRecord;
import com.re.it210project.model.entity.Equipment;
import com.re.it210project.model.entity.PaymentTransaction;
import com.re.it210project.model.entity.SessionUser;
import com.re.it210project.model.enums.BorrowingStatus;
import com.re.it210project.repository.*;
import com.re.it210project.service.BorrowingService;
import com.re.it210project.service.EquipmentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final EquipmentRepository equipmentRepository;
    private final BorrowingService borrowingService;
    private final MentoringSessionRepository mentoringSessionRepository;
    private final EquipmentService equipmentService;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final DepartmentRepository departmentRepository;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("sessionUser");

        model.addAttribute("totalEquipments", equipmentService.findAll().size());

        List<Object[]> statusCounts = mentoringSessionRepository.countSessionsByStatus();
        long pendingCount = 0;
        for (Object[] row : statusCounts) {
            if (row[0].toString().equals("PENDING")) pendingCount = (long) row[1];
        }

        model.addAttribute("pendingSessions", pendingCount);
        model.addAttribute("borrowedCount", borrowingRecordRepository.countActiveBorrowings());

        List<Object[]> topData = mentoringSessionRepository.getTop5Lecturers(PageRequest.of(0, 5));
        List<String> lecturerNames = new ArrayList<>();
        List<Long> sessionCounts = new ArrayList<>();

        for (Object[] row : topData) {
            lecturerNames.add((String) row[0]);
            sessionCounts.add((Long) row[1]);
        }

        model.addAttribute("recentBorrowings",
                borrowingRecordRepository.findTop5RecentBorrowings(PageRequest.of(0, 5)));
        model.addAttribute("lowStockEquipments",
                equipmentRepository.findLowStockEquipments());
        model.addAttribute("totalSessions", mentoringSessionRepository.count());
        model.addAttribute("lecturerNames", lecturerNames);
        model.addAttribute("sessionCounts", sessionCounts);
        model.addAttribute("sessionUser", sessionUser);

        return "pages/admin/dashboard";
    }

    @GetMapping("/borrowings")
    public String borrowings(
            HttpSession session,
            Model model
    ) {
        SessionUser sessionUser =
                (SessionUser) session.getAttribute("sessionUser");
        model.addAttribute("sessionUser", sessionUser);
        model.addAttribute(
                "borrowings",
                borrowingService.findAll()
        );

        return "pages/admin/borrowings";
    }

    @GetMapping("/borrowings/{id}")
    public String borrowingDetail(
            @PathVariable Long id,
            HttpSession session,
            Model model
    ) {
        SessionUser sessionUser =
                (SessionUser) session.getAttribute("sessionUser");
        model.addAttribute("sessionUser", sessionUser);
        model.addAttribute("active", "borrowings");
        model.addAttribute(
                "record",
                borrowingService.findById(id)
        );

        return "pages/admin/borrowing-detail";
    }

    @PostMapping("/borrowings/{id}/return")
    public String returnBorrowing(
            @PathVariable Long id,
            RedirectAttributes ra,
            HttpSession session
    ) {
        SessionUser sessionUser =
                (SessionUser) session.getAttribute("sessionUser");
        borrowingService.returnEquipment(id);
        ra.addFlashAttribute(
                "successMsg",
                "Đã xác nhận trả thiết bị"
        );

        return "redirect:/admin/borrowings";
    }

    @PostMapping("/borrowings/{id}/approve")
    public String approveBorrowing(
            @PathVariable Long id,
            RedirectAttributes ra,
            HttpSession session
    ) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("sessionUser");

        try {
            borrowingService.approveExport(id);
            ra.addFlashAttribute("successMsg", "Xuất kho thiết bị thành công");
            return "redirect:/admin/borrowings/" + id;
        } catch (com.re.it210project.exception.BadRequestException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/admin/borrowings/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/admin/borrowings/" + id;
        }
    }

    @PostMapping("/borrowings/{id}/reject")
    public String rejectBorrowing(
            @PathVariable Long id,
            RedirectAttributes ra,
            HttpSession session
    ) {
        SessionUser sessionUser =
                (SessionUser) session.getAttribute("sessionUser");
        borrowingService.rejectExport(id);
        ra.addFlashAttribute(
                "successMsg",
                "Đã từ chối phiếu mượn"
        );

        return "redirect:/admin/borrowings";
    }

    @GetMapping("/equipments")
    public String equipments(HttpSession session, Model model) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("sessionUser");

        model.addAttribute("sessionUser", sessionUser);
        model.addAttribute("active", "equipments");
        model.addAttribute("equipments", equipmentRepository.findAll());

        return "pages/admin/equipments";
    }

    @GetMapping("/equipments/create")
    public String createPage(HttpSession session, Model model) {

        SessionUser sessionUser =
                (SessionUser) session.getAttribute("sessionUser");
        model.addAttribute("sessionUser", sessionUser);
        model.addAttribute(
                "equipmentRequest",
                new EquipmentRequest()
        );

        return "pages/admin/equipment-form";
    }

    @PostMapping("/equipments/create")
    public String create(
            @Valid @ModelAttribute("equipmentRequest")
            EquipmentRequest request,
            BindingResult result,
            HttpSession session,
            Model model,
            RedirectAttributes ra
    ) {
        if (Boolean.TRUE.equals(request.getRequiresDeposit())
                && (request.getDepositAmount() == null
                || request.getDepositAmount() <= 0)) {
            result.rejectValue(
                    "depositAmount",
                    "error.depositAmount",
                    "Thiết bị yêu cầu đặt cọc phải có tiền cọc lớn hơn 0"
            );
        }

        if (result.hasErrors()) {
            SessionUser sessionUser =
                    (SessionUser) session.getAttribute(
                            "sessionUser"
                    );
            model.addAttribute(
                    "sessionUser",
                    sessionUser
            );

            return "pages/admin/equipment-form";
        }

        Equipment equipment = Equipment.builder()
                .name(request.getName())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .requiresDeposit(
                        request.getRequiresDeposit()
                )
                .depositAmount(
                        request.getDepositAmount()
                )
                .active(true)
                .build();

        equipmentRepository.save(equipment);

        ra.addFlashAttribute(
                "successMsg",
                "Thêm thiết bị thành công!"
        );

        return "redirect:/admin/equipments";
    }

    @GetMapping("/equipments/edit/{id}")
    public String editPage(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes ra
    ) {
        SessionUser sessionUser =
                (SessionUser) session.getAttribute("sessionUser");
        Equipment equipment = equipmentRepository
                .findById(id)
                .orElse(null);

        if (equipment == null) {
            ra.addFlashAttribute(
                    "errorMsg",
                    "Thiết bị không tồn tại!"
            );

            return "redirect:/admin/equipments";
        }

        EquipmentRequest request = new EquipmentRequest();

        request.setName(equipment.getName());
        request.setDescription(equipment.getDescription());
        request.setQuantity(equipment.getQuantity());

        model.addAttribute("sessionUser", sessionUser);
        model.addAttribute("equipmentId", id);
        model.addAttribute("equipmentRequest", request);

        return "pages/admin/equipment-form";
    }

    @PostMapping("/equipments/edit/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute EquipmentRequest request,
            BindingResult result,
            HttpSession session,
            Model model,
            RedirectAttributes ra
    ) {
        SessionUser sessionUser =
                (SessionUser) session.getAttribute("sessionUser");
        if (result.hasErrors()) {
            model.addAttribute("sessionUser", sessionUser);
            model.addAttribute("equipmentId", id);

            return "pages/admin/equipment-form";
        }

        Equipment equipment = equipmentRepository
                .findById(id)
                .orElse(null);
        if (equipment == null) {
            ra.addFlashAttribute(
                    "errorMsg",
                    "Thiết bị không tồn tại!"
            );

            return "redirect:/admin/equipments";
        }

        equipment.setName(request.getName());
        equipment.setDescription(request.getDescription());
        equipment.setQuantity(request.getQuantity());
        equipmentRepository.save(equipment);

        ra.addFlashAttribute(
                "successMsg",
                "Cập nhật thiết bị thành công!"
        );

        return "redirect:/admin/equipments";
    }

    @GetMapping("/equipments/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        Equipment equipment = equipmentRepository.findById(id).orElseThrow();
        equipment.setActive(false);
        equipmentRepository.save(equipment);
        ra.addFlashAttribute("successMsg", "Đã ngừng hoạt động thiết bị!");

        return "redirect:/admin/equipments";
    }

    @GetMapping("/payment/vnpay-return")
    public String paymentReturn(
            @RequestParam String txnRef
    ) {
        PaymentTransaction transaction =
                paymentTransactionRepository
                        .findByTransactionRef(txnRef)
                        .orElseThrow();
        BorrowingRecord borrowing =
                transaction.getBorrowingRecord();
        borrowing.setStatus(
                BorrowingStatus.EXPORTED
        );
        borrowing.setExportedAt(
                LocalDateTime.now()
        );
        transaction.setPaidAt(
                LocalDateTime.now()
        );

        borrowingRecordRepository.save(borrowing);
        paymentTransactionRepository.save(transaction);

        return "Thanh toán thành công";
    }

    @GetMapping("/departments")
    public String listDepartments(HttpSession session, Model model) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("sessionUser");
        model.addAttribute("sessionUser", sessionUser);
        model.addAttribute("active", "departments");
        model.addAttribute("departments", departmentRepository.findAll());

        return "pages/admin/departments";
    }
}