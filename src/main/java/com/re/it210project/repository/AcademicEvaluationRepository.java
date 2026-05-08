package com.re.it210project.repository;

import com.re.it210project.model.entity.AcademicEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademicEvaluationRepository extends JpaRepository<AcademicEvaluation, Long> {

    Optional<AcademicEvaluation> findBySessionId(Long sessionId);
}