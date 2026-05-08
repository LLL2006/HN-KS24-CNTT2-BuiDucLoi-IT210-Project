package com.re.it210project.repository;

import com.re.it210project.model.entity.MentoringSession;
import com.re.it210project.model.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MentoringSessionRepository extends JpaRepository<MentoringSession, Long> {

    List<MentoringSession> findByStudentIdOrderByStartTimeDesc(Long studentId);

    boolean existsByLecturerUserIdAndStartTimeAndStatusNot(
            Long lecturerId,
            LocalDateTime startTime,
            SessionStatus status
    );

    boolean existsByLecturerUserIdAndStartTimeBetweenAndStatusNot(
            Long lecturerId,
            LocalDateTime start,
            LocalDateTime end,
            SessionStatus status
    );

    @Query("""
        SELECT m
        FROM MentoringSession m
        JOIN FETCH m.lecturer l
        JOIN FETCH l.user
        WHERE m.student.id = :studentId
        ORDER BY m.startTime DESC
    """)
    List<MentoringSession> findFullHistoryByStudentId(@Param("studentId") Long studentId);

    @Query("""
        SELECT m
        FROM MentoringSession m
        JOIN FETCH m.student
        WHERE m.lecturer.user.id = :lecturerId
        AND m.status = :status
        ORDER BY m.startTime ASC
    """)
    List<MentoringSession> findActiveSessionsByLecturer(
            @Param("lecturerId") Long lecturerId,
            @Param("status") SessionStatus status
    );

    @Query("""
        SELECT m.lecturer.user.username, COUNT(m)
        FROM MentoringSession m
        WHERE m.status = 'COMPLETED'
        GROUP BY m.lecturer.id
        ORDER BY COUNT(m) DESC
    """)
    List<Object[]> countCompletedSessionsByLecturer();

    @Query("""
    SELECT ms
    FROM MentoringSession ms
    JOIN FETCH ms.lecturer l
    JOIN FETCH l.user u
    WHERE ms.student.id = :studentId
    ORDER BY ms.startTime DESC
""")
    List<MentoringSession> findByStudentId(Long studentId);
}