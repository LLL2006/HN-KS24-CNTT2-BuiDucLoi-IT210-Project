package com.re.it210project.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "academic_evaluations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademicEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "session_id", unique = true)
    private MentoringSession session;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(length = 100)
    private String abilityLevel;

    private LocalDateTime createdAt;
}