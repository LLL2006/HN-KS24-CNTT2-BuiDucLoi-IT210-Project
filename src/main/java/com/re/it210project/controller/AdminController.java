package com.re.it210project.controller;

import com.re.it210project.model.dto.EquipmentRequest;
import com.re.it210project.model.entity.Equipment;
import com.re.it210project.model.entity.SessionUser;
import com.re.it210project.repository.EquipmentRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final EquipmentRepository equipmentRepository;

    private boolean isAdmin(SessionUser user) {
        return user != null &&
                user.getRole().name().equals("ADMIN");
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        SessionUser sessionUser =
                (SessionUser) session.getAttribute("sessionUser");

        if (!isAdmin(sessionUser)) {
            return "redirect:/access-denied";
        }

        model.addAttribute("sessionUser", sessionUser);
        model.addAttribute("active", "dashboard");

        return "pages/admin/dashboard";
    }

    @GetMapping("/equipments")
    public String equipments(HttpSession session, Model model) {
        SessionUser sessionUser = (SessionUser) session.getAttribute("sessionUser");
        if (sessionUser == null) return "redirect:/auth/login";

        model.addAttribute("sessionUser", sessionUser);
        model.addAttribute("active", "equipments");
        model.addAttribute("equipments", equipmentRepository.findAll());

        return "pages/admin/equipments";
    }

    @GetMapping("/equipments/create")
    public String createPage(HttpSession session, Model model) {

        SessionUser sessionUser =
                (SessionUser) session.getAttribute("sessionUser");

        if (!isAdmin(sessionUser)) {
            return "redirect:/access-denied";
        }

        model.addAttribute("sessionUser", sessionUser);

        model.addAttribute(
                "equipmentRequest",
                new EquipmentRequest()
        );

        return "pages/admin/equipment-form";
    }

    @PostMapping("/equipments/create")
    public String create(@Valid @ModelAttribute("equipmentRequest") EquipmentRequest request,
                         BindingResult result, HttpSession session, Model model, RedirectAttributes ra) {

        if (result.hasErrors()) {
            SessionUser sessionUser = (SessionUser) session.getAttribute("sessionUser");
            model.addAttribute("sessionUser", sessionUser);
            return "pages/admin/equipment-form";
        }

        Equipment equipment = Equipment.builder()
                .name(request.getName())
                .description(request.getDescription())
                .quantity(request.getQuantity())
                .active(true)
                .build();

        equipmentRepository.save(equipment);
        ra.addFlashAttribute("successMsg", "Thêm thiết bị thành công!");
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

        if (!isAdmin(sessionUser)) {
            return "redirect:/access-denied";
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

        if (!isAdmin(sessionUser)) {
            return "redirect:/access-denied";
        }

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

    @PostMapping("/equipments/delete/{id}")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes ra,
            HttpSession session
    ) {

        SessionUser sessionUser =
                (SessionUser) session.getAttribute("sessionUser");

        if (!isAdmin(sessionUser)) {
            return "redirect:/access-denied";
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

        equipmentRepository.delete(equipment);

        ra.addFlashAttribute(
                "successMsg",
                "Xóa thiết bị thành công!"
        );

        return "redirect:/admin/equipments";
    }
}