package com.re.it210project.service;

import com.re.it210project.model.entity.BorrowingRecord;

import java.util.List;

public interface BorrowingService {
    List<BorrowingRecord> findAllPending();
    void approveExport(Long borrowingRecordId);
    BorrowingRecord findById(Long id);
}