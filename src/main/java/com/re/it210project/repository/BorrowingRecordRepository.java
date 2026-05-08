package com.re.it210project.repository;

import com.re.it210project.model.entity.BorrowingRecord;
import com.re.it210project.model.enums.BorrowingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {

    Optional<BorrowingRecord> findBySessionId(Long sessionId);

    List<BorrowingRecord> findByStatus(BorrowingStatus status);
}