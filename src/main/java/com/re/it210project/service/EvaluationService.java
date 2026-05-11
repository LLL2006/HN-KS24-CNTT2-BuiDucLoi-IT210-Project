package com.re.it210project.service;

import com.re.it210project.model.dto.EvaluationRequest;

public interface EvaluationService {
    void completeEvaluation(EvaluationRequest request, Long lecturerId);
}