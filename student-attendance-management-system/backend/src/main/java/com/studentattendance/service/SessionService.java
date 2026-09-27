package com.studentattendance.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.studentattendance.dto.session.SessionRequest;
import com.studentattendance.dto.session.SessionResponse;
import com.studentattendance.entity.Lecturer;
import com.studentattendance.entity.Module;
import com.studentattendance.entity.Session;
import com.studentattendance.entity.SessionStatus;
import com.studentattendance.exception.BadRequestException;
import com.studentattendance.exception.NotFoundException;
import com.studentattendance.repository.LecturerRepository;
import com.studentattendance.repository.ModuleRepository;
import com.studentattendance.repository.SessionRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final ModuleRepository moduleRepository;
    private final LecturerRepository lecturerRepository;

    public SessionService(
            SessionRepository sessionRepository,
            ModuleRepository moduleRepository,
            LecturerRepository lecturerRepository) {
        this.sessionRepository = sessionRepository;
        this.moduleRepository = moduleRepository;
        this.lecturerRepository = lecturerRepository;
    }

    @Transactional
    public SessionResponse createSession(Long lecturerUserId, SessionRequest request) {
        Lecturer lecturer = getLecturerByUserId(lecturerUserId);
        Module module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new NotFoundException("Module not found."));

        validateLecturerOwnsModule(lecturer, module);
        validateModuleActive(module);
        validateTimeRange(request);

        Session session = new Session();
        session.setModule(module);
        session.setLecturer(lecturer);
        session.setSessionDate(request.getSessionDate());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setRoom(request.getRoom());
        session.setStatus(SessionStatus.SCHEDULED);
        session.setLocked(false);

        return SessionResponse.from(sessionRepository.save(session));
    }

    public List<SessionResponse> listLecturerSessions(Long lecturerUserId) {
        Lecturer lecturer = getLecturerByUserId(lecturerUserId);
        return sessionRepository.findByLecturerId(lecturer.getId()).stream()
                .map(SessionResponse::from)
                .toList();
    }

    public List<SessionResponse> listModuleSessions(Long lecturerUserId, Long moduleId) {
        Lecturer lecturer = getLecturerByUserId(lecturerUserId);
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new NotFoundException("Module not found."));
        validateLecturerOwnsModule(lecturer, module);

        return sessionRepository.findByModuleIdAndLecturerId(moduleId, lecturer.getId()).stream()
                .map(SessionResponse::from)
                .toList();
    }

    public SessionResponse getSession(Long lecturerUserId, Long sessionId) {
        Lecturer lecturer = getLecturerByUserId(lecturerUserId);
        Session session = findOwnedSession(lecturer, sessionId);
        return SessionResponse.from(session);
    }

    @Transactional
    public SessionResponse activateSession(Long lecturerUserId, Long sessionId) {
        Lecturer lecturer = getLecturerByUserId(lecturerUserId);
        Session session = findOwnedSession(lecturer, sessionId);

        if (session.isLocked()) {
            throw new BadRequestException("Locked sessions cannot be activated.");
        }
        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new BadRequestException("Only scheduled sessions can be activated.");
        }

        session.setStatus(SessionStatus.ACTIVE);
        session.setAttendanceToken(UUID.randomUUID().toString());
        session.setTokenExpiry(LocalDateTime.of(session.getSessionDate(), session.getEndTime()));
        return SessionResponse.from(sessionRepository.save(session));
    }

    @Transactional
    public SessionResponse lockSession(Long lecturerUserId, Long sessionId) {
        Lecturer lecturer = getLecturerByUserId(lecturerUserId);
        Session session = findOwnedSession(lecturer, sessionId);

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BadRequestException("Only active sessions can be locked.");
        }

        session.setLocked(true);
        session.setStatus(SessionStatus.LOCKED);
        session.setTokenExpiry(LocalDateTime.now());
        return SessionResponse.from(sessionRepository.save(session));
    }

    @Transactional
    public SessionResponse completeSession(Long lecturerUserId, Long sessionId) {
        Lecturer lecturer = getLecturerByUserId(lecturerUserId);
        Session session = findOwnedSession(lecturer, sessionId);

        if (session.getStatus() != SessionStatus.ACTIVE && session.getStatus() != SessionStatus.LOCKED) {
            throw new BadRequestException("Only active or locked sessions can be completed.");
        }

        session.setLocked(true);
        session.setStatus(SessionStatus.COMPLETED);
        session.setTokenExpiry(LocalDateTime.now());
        return SessionResponse.from(sessionRepository.save(session));
    }

    private Lecturer getLecturerByUserId(Long lecturerUserId) {
        return lecturerRepository.findByUserId(lecturerUserId)
                .orElseThrow(() -> new NotFoundException("Lecturer profile not found."));
    }

    private Session findOwnedSession(Lecturer lecturer, Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found."));
        if (!session.getLecturer().getId().equals(lecturer.getId())) {
            throw new AccessDeniedException("You do not own this session.");
        }
        return session;
    }

    private void validateLecturerOwnsModule(Lecturer lecturer, Module module) {
        if (module.getLecturer() == null || !module.getLecturer().getId().equals(lecturer.getId())) {
            throw new AccessDeniedException("You are not assigned to this module.");
        }
    }

    private void validateModuleActive(Module module) {
        if (!module.isActive()) {
            throw new BadRequestException("Cannot create sessions for an inactive module.");
        }
    }

    private void validateTimeRange(SessionRequest request) {
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time.");
        }
    }
}
