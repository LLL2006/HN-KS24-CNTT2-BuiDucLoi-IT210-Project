package com.re.it210project.service;

import com.re.it210project.model.dto.AcademicHistoryResponse;
import com.re.it210project.model.entity.AcademicEvaluation;

import java.util.List;

public interface AcademicHistoryService {
    List<AcademicHistoryResponse> getStudentHistory(Long studentId);
    AcademicEvaluation getEvaluationDetail(Long sessionId);
}