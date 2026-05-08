package com.re.it210project.repository;

import com.re.it210project.model.entity.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LecturerRepository extends JpaRepository<Lecturer, Long> {

    List<Lecturer> findByDepartmentId(Long departmentId);

    @Query("""
        SELECT l
        FROM Lecturer l
        JOIN FETCH l.user
        JOIN FETCH l.department
    """)
    List<Lecturer> findAllWithUserAndDepartment();

    @Query("SELECT l FROM Lecturer l JOIN FETCH l.user u LEFT JOIN FETCH u.profile JOIN FETCH l.department")
    List<Lecturer> findAllWithProfile();
}