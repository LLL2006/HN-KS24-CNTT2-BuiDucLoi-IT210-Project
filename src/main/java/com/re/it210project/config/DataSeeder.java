package com.re.it210project.config;

import com.re.it210project.model.entity.*;
import com.re.it210project.model.enums.Role;
import com.re.it210project.repository.*;
import com.re.it210project.security.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final LecturerRepository lecturerRepository;
    private final EquipmentRepository equipmentRepository;
    private final LabRoomRepository labRoomRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (departmentRepository.count() > 0) return;

        seedAdmin();
        seedDataByDepartment();
        seedEquipments();
    }

    private void seedAdmin() {
        if (userRepository.existsByUsername("admin")) return;

        User admin = User.builder()
                .username("admin")
                .email("admin@smartlab.com")
                .passwordHash(PasswordUtil.hash("Admin@123"))
                .role(Role.ADMIN)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        User savedAdmin = userRepository.save(admin);
        userProfileRepository.save(UserProfile.builder()
                .user(savedAdmin)
                .fullName("Quản trị viên hệ thống")
                .build());
    }

    private void seedDataByDepartment() {
        String[][] deptData = {
                {"Công nghệ thông tin", "CNTT", "Phòng Lab AI & Big Data", "A2-301", "Nguyễn Văn CNTT", "AI/ML Specialist"},
                {"Kỹ thuật phần mềm", "KTPM", "Phòng Lab Software Engineering", "A2-302", "Trần Văn Phần Mềm", "Clean Code & Testing"},
                {"Hệ thống thông tin", "HTTT", "Phòng Lab Database Systems", "B1-205", "Lê Thị Hệ Thống", "Data Architect"},
                {"Mạng máy tính", "MMT", "Phòng Lab Network & Security", "B1-206", "Phạm Văn Mạng", "Cyber Security"}
        };

        for (int i = 0; i < deptData.length; i++) {
            Department dept = Department.builder()
                    .name(deptData[i][0])
                    .code(deptData[i][1])
                    .build();
            dept = departmentRepository.save(dept);

            LabRoom lab = LabRoom.builder()
                    .name(deptData[i][2])
                    .roomNumber(deptData[i][3])
                    .department(dept)
                    .build();
            labRoomRepository.save(lab);

            String username = "lecturer_" + deptData[i][1].toLowerCase();
            createLecturer(
                    username,
                    username + "@smartlab.com",
                    deptData[i][4],
                    deptData[i][5],
                    dept
            );
        }
    }

    private void createLecturer(String username, String email, String fullName, String spec, Department dept) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(PasswordUtil.hash("Lecturer@123"))
                .role(Role.LECTURER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        userProfileRepository.save(UserProfile.builder()
                .user(savedUser)
                .fullName(fullName)
                .department(dept)
                .build());

        lecturerRepository.save(Lecturer.builder()
                .user(savedUser)
                .department(dept)
                .specialization(spec)
                .build());
    }

    private void seedEquipments() {
        if (equipmentRepository.count() > 0) return;

        List<Equipment> equipments = List.of(
                Equipment.builder().name("Kit Arduino Uno").quantity(20).active(true).requiresDeposit(false).depositAmount(0.0).build(),
                Equipment.builder().name("Macbook Pro M3").quantity(5).active(true).requiresDeposit(true).depositAmount(7000000.0).build(),
                Equipment.builder().name("DJI Mini Drone").quantity(2).active(true).requiresDeposit(true).depositAmount(5000000.0).build()
        );
        equipmentRepository.saveAll(equipments);
    }
}