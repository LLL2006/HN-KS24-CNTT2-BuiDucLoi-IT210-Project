package com.re.it210project.service.impl;

import com.re.it210project.exception.BadRequestException;
import com.re.it210project.exception.NotFoundException;
import com.re.it210project.model.dto.MentoringBookingRequest;
import com.re.it210project.model.entity.Lecturer;
import com.re.it210project.model.entity.MentoringSession;
import com.re.it210project.model.entity.User;
import com.re.it210project.model.enums.SessionStatus;
import com.re.it210project.repository.LecturerRepository;
import com.re.it210project.repository.MentoringSessionRepository;
import com.re.it210project.repository.UserRepository;
import com.re.it210project.service.MentoringService;
import com.re.it210project.util.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MentoringServiceImpl implements MentoringService {

    private final MentoringSessionRepository mentoringSessionRepository;
    private final UserRepository userRepository;
    private final LecturerRepository lecturerRepository;

    @Override
    @Transactional
    public MentoringSession book(MentoringBookingRequest request, Long studentId) {

        LocalDateTime startTime = LocalDateTime.of(
                request.getSessionDate(),
                request.getSessionTime()
        );

        // 1. Chặn đặt lịch quá khứ (Yêu cầu CORE-05)
        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Không thể đặt lịch trong quá khứ");
        }

        boolean duplicated = mentoringSessionRepository.existsByLecturerUserIdAndStartTimeBetweenAndStatusNot(
                request.getLecturerId(),
                startTime.minusMinutes(59),
                startTime.plusMinutes(59),
                SessionStatus.CANCELLED
        );

        if (duplicated) {
            throw new BadRequestException("Giảng viên đã có lịch hoặc đang trong ca tư vấn khác ở khung giờ này.");
        }

        // 3. Tìm kiếm thực thể
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sinh viên"));

        Lecturer lecturer = lecturerRepository.findById(request.getLecturerId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy giảng viên"));

        // 4. Khởi tạo phiên tư vấn (Transaction an toàn)
        MentoringSession session = MentoringSession.builder()
                .student(student)
                .lecturer(lecturer)
                .startTime(startTime)
                .endTime(startTime.plusHours(1)) // Mặc định mỗi ca 1 tiếng
                .status(SessionStatus.PENDING)
                .topic(request.getTopic()) // Đừng quên lưu Topic sinh viên nhập nhé
                .createdAt(LocalDateTime.now())
                .build();

        return mentoringSessionRepository.save(session);
    }

    @Override
    @Transactional
    public void cancel(Long sessionId, Long studentId) {

        MentoringSession session = mentoringSessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new NotFoundException("Không tìm thấy lịch tư vấn"));

        if (!session.getStudent().getId().equals(studentId)) {
            throw new BadRequestException(
                    "Bạn không có quyền thao tác lịch này"
            );
        }

        if (!DateTimeUtil.isBeforeHours(session.getStartTime(), 24)) {
            throw new BadRequestException(
                    "Chỉ được hủy lịch trước ít nhất 24 giờ"
            );
        }

        session.setStatus(SessionStatus.CANCELLED);

        mentoringSessionRepository.save(session);
    }

    public List<MentoringSession> findStudentSessions(Long studentId) {
        return mentoringSessionRepository.findByStudentId(studentId);
    }

    @Override
    public List<MentoringSession> findPendingSessionsForLecturer(Long lecturerId) {
        return mentoringSessionRepository.findActiveSessionsByLecturer(
                lecturerId,
                SessionStatus.PENDING
        );
    }
}