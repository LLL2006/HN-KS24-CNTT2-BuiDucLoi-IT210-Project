package com.re.it210project.model.entity;

import com.re.it210project.model.enums.BorrowingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "borrowing_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "session_id", unique = true)
    private MentoringSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private BorrowingStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime exportedAt;

    @OneToMany(mappedBy = "borrowingRecord",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<BorrowingDetail> details = new ArrayList<>();

    // tiền cọc
    private Double depositAmount;

    // tiền bị phạt
    private Double penaltyAmount;

    // hạn phải trả
    private LocalDateTime dueReturnTime;

    // thời gian trả thực tế
    private LocalDateTime actualReturnTime;
}