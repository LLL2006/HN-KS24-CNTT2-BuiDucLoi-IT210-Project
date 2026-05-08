package com.re.it210project.service;

import com.re.it210project.model.dto.MentoringBookingRequest;
import com.re.it210project.model.entity.MentoringSession;

import java.util.List;

public interface MentoringService {

    MentoringSession book(MentoringBookingRequest request, Long studentId);

    void cancel(Long sessionId, Long studentId);

    List<MentoringSession> findStudentSessions(Long studentId);

    List<MentoringSession> findPendingSessionsForLecturer(Long lecturerId);
}