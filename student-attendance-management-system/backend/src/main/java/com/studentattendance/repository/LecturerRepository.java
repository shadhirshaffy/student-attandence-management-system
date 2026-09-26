package com.studentattendance.repository;

import java.util.Optional;

import com.studentattendance.entity.Lecturer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LecturerRepository extends JpaRepository<Lecturer, Long> {

    Optional<Lecturer> findByEmployeeNumber(String employeeNumber);

    boolean existsByEmployeeNumber(String employeeNumber);
}
