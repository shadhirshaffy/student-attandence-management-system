package com.studentattendance.repository;

import java.util.Optional;

import com.studentattendance.entity.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    @EntityGraph(attributePaths = {"user"})
    Optional<Student> findByStudentNumber(String studentNumber);

    @EntityGraph(attributePaths = {"user"})
    Optional<Student> findByUserId(Long userId);

    boolean existsByStudentNumber(String studentNumber);

    boolean existsByStudentNumberAndIdNot(String studentNumber, Long id);
}
