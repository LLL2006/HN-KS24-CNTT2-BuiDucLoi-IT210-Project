package com.re.it210project.repository;

import com.re.it210project.model.entity.BorrowingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BorrowingDetailRepository extends JpaRepository<BorrowingDetail, Long> {

    @Query("SELECT d FROM BorrowingDetail d JOIN FETCH d.equipment WHERE d.borrowingRecord.id = :recordId")
    List<BorrowingDetail> findByBorrowingRecordIdWithEquipment(@Param("recordId") Long recordId);

    @Query("SELECT d.equipment.name, SUM(d.quantity) FROM BorrowingDetail d " +
            "GROUP BY d.equipment.id " +
            "ORDER BY SUM(d.quantity) DESC")
    List<Object[]> countMostBorrowedEquipments();

    @Query("SELECT SUM(d.quantity) FROM BorrowingDetail d " +
            "WHERE d.borrowingRecord.status = 'EXPORTED'")
    Long countTotalEquipmentsInUse();
}