package com.studentattendance.repository;

import java.util.List;
import java.util.Optional;

import com.studentattendance.entity.AttendanceRecord;
import com.studentattendance.entity.Session;
import com.studentattendance.entity.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByStudent(Student student);

    @EntityGraph(attributePaths = {"session", "session.module", "student", "student.user"})
    List<AttendanceRecord> findByStudentId(Long studentId);

    List<AttendanceRecord> findBySession(Session session);

    @EntityGraph(attributePaths = {"student", "student.user"})
    List<AttendanceRecord> findBySessionId(Long sessionId);

    Optional<AttendanceRecord> findByStudentAndSession(Student student, Session session);

    Optional<AttendanceRecord> findByStudentIdAndSessionId(Long studentId, Long sessionId);

    boolean existsByStudentIdAndSessionId(Long studentId, Long sessionId);
}
