package com.studentattendance.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.studentattendance.entity.Module;
import com.studentattendance.entity.Session;
import com.studentattendance.entity.SessionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import jakarta.persistence.LockModeType;

public interface SessionRepository extends JpaRepository<Session, Long> {

    // Serialize attendance writes and state changes on the same session row.
    // Avoid fetch joins here: PostgreSQL cannot lock the nullable side of an outer join.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Session s where s.id = :id")
    Optional<Session> findForUpdateById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Session s where s.attendanceToken = :token")
    Optional<Session> findForUpdateByToken(String token);

    @Override
    @EntityGraph(attributePaths = {"module", "module.lecturer", "module.lecturer.user", "lecturer", "lecturer.user"})
    Optional<Session> findById(Long id);

    List<Session> findByModule(Module module);

    @EntityGraph(attributePaths = {"module", "module.lecturer", "module.lecturer.user", "lecturer", "lecturer.user"})
    List<Session> findByModuleId(Long moduleId);

    @EntityGraph(attributePaths = {"module", "module.lecturer", "module.lecturer.user", "lecturer", "lecturer.user"})
    List<Session> findByLecturerId(Long lecturerId);

    @EntityGraph(attributePaths = {"module", "module.lecturer", "module.lecturer.user", "lecturer", "lecturer.user"})
    List<Session> findByModuleIdAndLecturerId(Long moduleId, Long lecturerId);

    List<Session> findByModuleIdAndSessionDate(Long moduleId, LocalDate sessionDate);

    @EntityGraph(attributePaths = {"module", "module.lecturer", "module.lecturer.user", "lecturer", "lecturer.user"})
    List<Session> findByModuleIdAndStatus(Long moduleId, SessionStatus status);

    List<Session> findByStatus(SessionStatus status);

    @EntityGraph(attributePaths = {"module", "module.lecturer", "module.lecturer.user", "lecturer", "lecturer.user"})
    Optional<Session> findByAttendanceToken(String attendanceToken);
}
