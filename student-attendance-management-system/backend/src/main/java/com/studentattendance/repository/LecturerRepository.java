package com.studentattendance.repository;

import java.util.Optional;

import com.studentattendance.entity.Lecturer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LecturerRepository extends JpaRepository<Lecturer, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<Lecturer> findByEmployeeNumber(String employeeNumber);

    @EntityGraph(attributePaths = {"user"})
    Optional<Lecturer> findByUserId(Long userId);

    boolean existsByEmployeeNumber(String employeeNumber);

    boolean existsByEmployeeNumberAndIdNot(String employeeNumber, Long id);
}
