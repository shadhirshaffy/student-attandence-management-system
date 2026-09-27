package com.studentattendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.studentattendance.entity.Module;
import com.studentattendance.entity.Session;
import com.studentattendance.entity.SessionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, Long> {

    @Override
    @EntityGraph(attributePaths = {"module", "lecturer", "lecturer.user"})
    Optional<Session> findById(Long id);

    List<Session> findByModule(Module module);

    @EntityGraph(attributePaths = {"module", "lecturer", "lecturer.user"})
    List<Session> findByModuleId(Long moduleId);

    @EntityGraph(attributePaths = {"module", "lecturer", "lecturer.user"})
    List<Session> findByLecturerId(Long lecturerId);

    @EntityGraph(attributePaths = {"module", "lecturer", "lecturer.user"})
    List<Session> findByModuleIdAndLecturerId(Long moduleId, Long lecturerId);

    List<Session> findByModuleIdAndSessionDate(Long moduleId, LocalDate sessionDate);

    List<Session> findByStatus(SessionStatus status);

    Optional<Session> findByAttendanceToken(String attendanceToken);
}
