package com.re.it210project.repository;

import com.re.it210project.model.entity.LabRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LabRoomRepository extends JpaRepository<LabRoom, Long> {
}