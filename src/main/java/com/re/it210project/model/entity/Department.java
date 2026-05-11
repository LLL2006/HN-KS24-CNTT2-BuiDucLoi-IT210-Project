package com.re.it210project.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String name;

    @Column(unique = true, length = 20)
    private String code;

    @OneToMany(mappedBy = "department")
    private List<UserProfile> userProfiles;

    @OneToMany(mappedBy = "department")
    private List<Lecturer> lecturers;

    @OneToMany(mappedBy = "department")
    private List<LabRoom> labRooms;
}