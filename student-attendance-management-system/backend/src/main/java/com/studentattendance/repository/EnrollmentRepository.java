package com.studentattendance.repository;

import java.util.List;
import java.util.Optional;

import com.studentattendance.entity.Enrollment;
import com.studentattendance.entity.Module;
import com.studentattendance.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudent(Student student);

    List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByModule(Module module);

    List<Enrollment> findByModuleId(Long moduleId);

    Optional<Enrollment> findByStudentAndModule(Student student, Module module);

    Optional<Enrollment> findByStudentIdAndModuleId(Long studentId, Long moduleId);

    boolean existsByStudentIdAndModuleId(Long studentId, Long moduleId);
}
