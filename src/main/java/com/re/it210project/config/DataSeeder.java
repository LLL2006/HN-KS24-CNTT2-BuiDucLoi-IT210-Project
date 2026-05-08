package com.re.it210project.config;

import com.re.it210project.model.entity.Department;
import com.re.it210project.model.entity.Equipment;
import com.re.it210project.model.entity.Lecturer;
import com.re.it210project.model.entity.User;
import com.re.it210project.model.entity.UserProfile;
import com.re.it210project.model.enums.Role;
import com.re.it210project.repository.DepartmentRepository;
import com.re.it210project.repository.EquipmentRepository;
import com.re.it210project.repository.LecturerRepository;
import com.re.it210project.repository.UserProfileRepository;
import com.re.it210project.repository.UserRepository;
import com.re.it210project.security.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final LecturerRepository lecturerRepository;
    private final EquipmentRepository equipmentRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedDepartments();
        seedAdmin();
        seedLecturers();
        seedEquipments();
    }

    private void seedDepartments() {
        if (departmentRepository.count() > 0) {
            return;
        }

        List<Department> departments = List.of(
                Department.builder().name("Công nghệ thông tin").build(),
                Department.builder().name("Kỹ thuật phần mềm").build(),
                Department.builder().name("Hệ thống thông tin").build(),
                Department.builder().name("Mạng máy tính").build()
        );

        departmentRepository.saveAll(departments);
    }

    private void seedAdmin() {
        if (userRepository.existsByUsername("admin")) {
            return;
        }

        User admin = User.builder()
                .username("admin")
                .email("admin@example.com")
                .passwordHash(PasswordUtil.hash("Admin@123"))
                .role(Role.ADMIN)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        User savedAdmin = userRepository.save(admin);

        UserProfile profile = UserProfile.builder()
                .user(savedAdmin)
                .fullName("Quản trị viên")
                .build();

        userProfileRepository.save(profile);
    }

    private void seedLecturers() {

        if (userRepository.existsByUsername("lecturer1")) {
            return;
        }

        Department department = departmentRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow();

        createLecturer(
                "lecturer1",
                "lecturer1@example.com",
                "Giảng viên 1",
                "Java Web, Spring Boot",
                department
        );

        createLecturer(
                "lecturer2",
                "lecturer2@example.com",
                "Giảng viên 2",
                "Database, System Design",
                department
        );
    }

    private void createLecturer(
            String username,
            String email,
            String fullName,
            String specialization,
            Department department
    ) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(PasswordUtil.hash("Lecturer@123"))
                .role(Role.LECTURER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(savedUser)
                .fullName(fullName)
                .department(department)
                .build();

        userProfileRepository.save(profile);

        Lecturer lecturer = Lecturer.builder()
                .user(savedUser)
                .department(department)
                .specialization(specialization)
                .build();

        lecturerRepository.save(lecturer);
    }

    private void seedEquipments() {
        if (equipmentRepository.count() > 0) {
            return;
        }

        List<Equipment> equipments = List.of(
                Equipment.builder()
                        .name("Kit Arduino Uno")
                        .description("Bộ kit thực hành Arduino cơ bản")
                        .quantity(20)
                        .active(true)
                        .build(),
                Equipment.builder()
                        .name("Raspberry Pi")
                        .description("Thiết bị thực hành IoT")
                        .quantity(10)
                        .active(true)
                        .build(),
                Equipment.builder()
                        .name("Tài liệu Java Web")
                        .description("Tài liệu thực hành Java Web")
                        .quantity(50)
                        .active(true)
                        .build()
        );

        equipmentRepository.saveAll(equipments);
    }
}