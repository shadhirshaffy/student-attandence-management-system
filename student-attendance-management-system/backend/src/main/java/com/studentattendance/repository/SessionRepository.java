package com.studentattendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.studentattendance.entity.Module;
import com.studentattendance.entity.Session;
import com.studentattendance.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {

    List<Session> findByModule(Module module);

    List<Session> findByModuleId(Long moduleId);

    List<Session> findByLecturerId(Long lecturerId);

    List<Session> findByModuleIdAndSessionDate(Long moduleId, LocalDate sessionDate);

    List<Session> findByStatus(SessionStatus status);

    Optional<Session> findByAttendanceToken(String attendanceToken);
}
