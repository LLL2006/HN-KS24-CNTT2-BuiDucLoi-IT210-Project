package com.re.it210project.repository;

import com.re.it210project.model.entity.BorrowingRecord;
import com.re.it210project.model.entity.Equipment;
import com.re.it210project.model.enums.BorrowingStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {

    Optional<BorrowingRecord> findBySessionId(Long sessionId);

    List<BorrowingRecord> findByStatus(BorrowingStatus status);
    List<BorrowingRecord> findAllByOrderByCreatedAtDesc();
    @Query("""
    SELECT COUNT(br) 
    FROM BorrowingRecord br 
    WHERE br.status = 'EXPORTED'
""")
    Long countActiveBorrowings();

    @Query("""
    SELECT br FROM BorrowingRecord br 
    JOIN FETCH br.session s 
    JOIN FETCH s.student st 
    ORDER BY br.id DESC
""")
    List<BorrowingRecord> findTop5RecentBorrowings(Pageable pageable);


}