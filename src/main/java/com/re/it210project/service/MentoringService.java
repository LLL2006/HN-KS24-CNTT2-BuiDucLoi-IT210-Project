package com.re.it210project.service;

import com.re.it210project.model.dto.MentoringBookingRequest;
import com.re.it210project.model.entity.MentoringSession;
import com.re.it210project.model.entity.SessionUser;

import java.util.List;

public interface MentoringService {
    MentoringSession book(MentoringBookingRequest request, Long studentId);
    void cancel(Long sessionId, Long studentId);
    List<MentoringSession> findStudentSessions(Long studentId);
    List<MentoringSession> findPendingSessionsForLecturer(Long lecturerId);
    void acceptSession(Long sessionId, Long lecturerId);
    void rejectSession(Long sessionId, Long lecturerId);
    List<MentoringSession> findSessionsForEvaluationPage(Long lecturerId);
    void cancelSession(Long sessionId, SessionUser currentUser);
    MentoringSession findById(Long id);
}