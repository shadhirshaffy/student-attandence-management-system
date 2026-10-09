package com.studentattendance.repository;

import java.util.List;
import java.util.Optional;

import com.studentattendance.entity.AbsenceJustification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbsenceJustificationRepository extends JpaRepository<AbsenceJustification, Long> {

    @EntityGraph(attributePaths = {"session", "session.module", "student", "student.user", "reviewedBy"})
    List<AbsenceJustification> findByStudentId(Long studentId);

    Optional<AbsenceJustification> findByStudentIdAndSessionId(Long studentId, Long sessionId);

    @EntityGraph(attributePaths = {"session", "session.module", "student", "student.user", "reviewedBy"})
    List<AbsenceJustification> findBySessionModuleLecturerId(Long lecturerId);
}
