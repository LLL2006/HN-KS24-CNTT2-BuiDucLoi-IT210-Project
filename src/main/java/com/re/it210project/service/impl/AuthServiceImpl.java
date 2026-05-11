package com.re.it210project.service.impl;

import com.re.it210project.exception.BadRequestException;
import com.re.it210project.exception.NotFoundException;
import com.re.it210project.model.dto.AuthRequest;
import com.re.it210project.model.dto.RegisterRequest;
import com.re.it210project.model.entity.*;
import com.re.it210project.model.enums.Role;
import com.re.it210project.repository.*;
import com.re.it210project.security.PasswordUtil;
import com.re.it210project.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public SessionUser login(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Tên đăng nhập hoặc mật khẩu không đúng"));

        if (user.getActive() != null && !user.getActive()) {
            throw new BadRequestException("Tài khoản đã bị khóa");
        }

        boolean isMatch = PasswordUtil.matches(request.getPassword(), user.getPasswordHash());
        if (!isMatch) {
            throw new BadRequestException("Tên đăng nhập hoặc mật khẩu không đúng");
        }

        UserProfile profile = userProfileRepository.findById(user.getId()).orElse(null);

        return SessionUser.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(profile != null ? profile.getFullName() : user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Tên đăng nhập đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã tồn tại");
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy khoa/ngành"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(PasswordUtil.hash(request.getPassword()))
                .role(Role.STUDENT)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(savedUser)
                .fullName(request.getFullName())
                .department(department)
                .build();

        userProfileRepository.save(profile);
    }
}