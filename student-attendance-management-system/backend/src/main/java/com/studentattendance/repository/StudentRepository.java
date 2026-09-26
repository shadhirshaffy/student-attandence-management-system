package com.studentattendance.repository;

import java.util.Optional;

import com.studentattendance.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByStudentNumber(String studentNumber);

    boolean existsByStudentNumber(String studentNumber);
}
