package com.re.it210project.service;

import com.re.it210project.model.dto.AcademicHistoryResponse;

import java.util.List;

public interface AcademicHistoryService {

    List<AcademicHistoryResponse> getStudentHistory(Long studentId);
}