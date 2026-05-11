package com.re.it210project.service.impl;

import com.re.it210project.exception.BadRequestException;
import com.re.it210project.exception.NotFoundException;
import com.re.it210project.model.dto.MentoringBookingRequest;
import com.re.it210project.model.entity.*;
import com.re.it210project.model.enums.BorrowingStatus;
import com.re.it210project.model.enums.SessionStatus;
import com.re.it210project.repository.BorrowingRecordRepository;
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
    private final BorrowingRecordRepository borrowingRecordRepository;

    @Override
    @Transactional
    public MentoringSession book(MentoringBookingRequest request, Long studentId) {
        LocalDateTime startTime = LocalDateTime.of(
                request.getSessionDate(),
                request.getSessionTime()
        );

        LocalDateTime endTime = startTime.plusHours(1);

        if (startTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Không thể đặt lịch ở thời điểm đã qua. Vui lòng chọn thời gian khác");
        }

        boolean isConflict = mentoringSessionRepository.existsConflictingSession(
                request.getLecturerId(),
                -1L,
                startTime,
                endTime
        );

        if (isConflict) {
            throw new BadRequestException("Giảng viên đã có lịch hoặc đang trong ca tư vấn khác ở khung giờ này");
        }

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thông tin sinh viên"));

        Lecturer lecturer = lecturerRepository.findById(request.getLecturerId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy thông tin giảng viên"));

        MentoringSession session = MentoringSession.builder()
                .student(student)
                .lecturer(lecturer)
                .topic(request.getTopic())
                .startTime(startTime)
                .endTime(endTime)
                .status(SessionStatus.PENDING)
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
    public List<MentoringSession> findPendingSessionsForLecturer(Long userId) {
        return mentoringSessionRepository.findByLecturerUserIdAndStatusInOrderByStartTimeDesc(
                userId,
                List.of(SessionStatus.PENDING)
        );
    }

    @Override
    @Transactional
    public void acceptSession(
            Long sessionId,
            Long lecturerId
    ) {
        MentoringSession session =
                mentoringSessionRepository
                        .findById(sessionId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Không tìm thấy lịch"
                                )
                        );

        if (session.getStatus() != SessionStatus.PENDING
                && session.getStatus() != SessionStatus.PENDING_PAYMENT) {

            throw new BadRequestException(
                    "Lịch này không ở trạng thái có thể tiếp nhận"
            );
        }

        boolean conflict = mentoringSessionRepository.existsConflictingSession(
                lecturerId,
                sessionId,
                session.getStartTime(),
                session.getEndTime()
        );

        if (conflict) {
            throw new BadRequestException(
                    "Bạn đã có ca tư vấn trùng thời gian"
            );
        }

        Lecturer lecturer =
                lecturerRepository
                        .findById(lecturerId)
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "Không tìm thấy giảng viên"
                                )
                        );

        session.setLecturer(lecturer);
        session.setStatus(
                SessionStatus.CONFIRMED
        );

        mentoringSessionRepository.save(session);
    }

    public List<MentoringSession> findSessionsForEvaluationPage(Long lecturerId) {
        return mentoringSessionRepository.findByLecturerUserIdAndStatusInOrderByStartTimeDesc(
                lecturerId,
                List.of(SessionStatus.CONFIRMED, SessionStatus.COMPLETED, SessionStatus.CANCELLED)
        );
    }

    @Override
    @Transactional
    public void rejectSession(Long sessionId, Long lecturerId) {
        MentoringSession session = mentoringSessionRepository
                .findById(sessionId)
                .orElseThrow(() ->
                        new NotFoundException("Không tìm thấy ca tư vấn"));

        if (!session.getLecturer().getUserId().equals(lecturerId)) {
            throw new BadRequestException(
                    "Bạn không có quyền xử lý ca này"
            );
        }

        if (session.getStatus() != SessionStatus.PENDING) {
            throw new BadRequestException(
                    "Ca tư vấn đã được xử lý"
            );
        }

        session.setStatus(SessionStatus.CANCELLED);
        mentoringSessionRepository.save(session);
    }

    @Override
    @Transactional
    public void cancelSession(Long sessionId, SessionUser currentUser) {
        MentoringSession session = mentoringSessionRepository.findById(sessionId)
                .orElseThrow(() ->
                        new NotFoundException("Không tìm thấy lịch tư vấn"));

        if (!session.getStudent().getId().equals(currentUser.getId())) {
            throw new BadRequestException(
                    "Bạn không thể hủy lịch của người khác"
            );
        }

        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new BadRequestException(
                    "Lịch đã bị hủy"
            );
        }

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new BadRequestException(
                    "Không thể hủy lịch đã hoàn thành"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(session.getStartTime().minusHours(24))) {
            throw new BadRequestException(
                    "Chỉ được hủy trước 24 giờ"
            );
        }

        session.setStatus(SessionStatus.CANCELLED);
        mentoringSessionRepository.save(session);
        BorrowingRecord borrowing = borrowingRecordRepository
                .findBySessionId(sessionId)
                .orElse(null);

        if (borrowing != null &&
                borrowing.getStatus() == BorrowingStatus.PENDING_ALLOCATION) {
            borrowing.setStatus(BorrowingStatus.REJECTED);
            borrowingRecordRepository.save(borrowing);
        }
    }

    @Override
    public MentoringSession findById(Long id) {
        return mentoringSessionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy ca tư vấn này"));
    }
}