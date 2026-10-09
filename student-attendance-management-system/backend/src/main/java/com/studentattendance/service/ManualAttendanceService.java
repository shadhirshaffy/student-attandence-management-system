package com.studentattendance.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.studentattendance.dto.attendance.ManualAttendanceItemRequest;
import com.studentattendance.dto.attendance.ManualAttendanceRequest;
import com.studentattendance.dto.attendance.ManualAttendanceResponse;
import com.studentattendance.dto.attendance.ManualAttendanceStudentResponse;
import com.studentattendance.entity.AttendanceRecord;
import com.studentattendance.entity.Enrollment;
import com.studentattendance.entity.Lecturer;
import com.studentattendance.entity.MarkingMethod;
import com.studentattendance.entity.Session;
import com.studentattendance.entity.SessionStatus;
import com.studentattendance.entity.Student;
import com.studentattendance.exception.BadRequestException;
import com.studentattendance.exception.NotFoundException;
import com.studentattendance.repository.AttendanceRecordRepository;
import com.studentattendance.repository.EnrollmentRepository;
import com.studentattendance.repository.LecturerRepository;
import com.studentattendance.repository.SessionRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class ManualAttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LecturerRepository lecturerRepository;
    private final SessionRepository sessionRepository;

    public ManualAttendanceService(
            AttendanceRecordRepository attendanceRecordRepository,
            EnrollmentRepository enrollmentRepository,
            LecturerRepository lecturerRepository,
            SessionRepository sessionRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lecturerRepository = lecturerRepository;
        this.sessionRepository = sessionRepository;
    }

    public ManualAttendanceResponse getManualAttendance(Long lecturerUserId, Long sessionId) {
        Lecturer lecturer = getLecturerByUserId(lecturerUserId);
        Session session = findOwnedSession(lecturer, sessionId);
        return buildResponse(session);
    }

    @Transactional
    public ManualAttendanceResponse saveManualAttendance(
            Long lecturerUserId,
            Long sessionId,
            ManualAttendanceRequest request) {
        Lecturer lecturer = getLecturerByUserId(lecturerUserId);
        Session session = sessionRepository.findForUpdateById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found."));
        if (!session.getLecturer().getId().equals(lecturer.getId())) {
            throw new AccessDeniedException("You do not own this session.");
        }
        validateEditable(session);

        List<Enrollment> enrollments = enrollmentRepository.findByModuleId(session.getModule().getId());
        Set<Long> enrolledStudentIds = new HashSet<>();
        for (Enrollment enrollment : enrollments) {
            enrolledStudentIds.add(enrollment.getStudent().getId());
        }

        Map<Long, ManualAttendanceItemRequest> requestedByStudentId = new HashMap<>();
        for (ManualAttendanceItemRequest item : request.getRecords()) {
            if (!enrolledStudentIds.contains(item.getStudentId())) {
                throw new BadRequestException("Student is not enrolled in this session's module.");
            }
            if (requestedByStudentId.put(item.getStudentId(), item) != null) {
                throw new BadRequestException("Duplicate student in attendance request.");
            }
        }

        Map<Long, AttendanceRecord> existingByStudentId = new HashMap<>();
        for (AttendanceRecord record : attendanceRecordRepository.findBySessionId(sessionId)) {
            existingByStudentId.put(record.getStudent().getId(), record);
        }

        LocalDateTime now = LocalDateTime.now();
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            ManualAttendanceItemRequest item = requestedByStudentId.get(student.getId());
            if (item == null) {
                continue;
            }

            AttendanceRecord record = existingByStudentId.get(student.getId());
            if (record == null) {
                record = new AttendanceRecord();
                record.setSession(session);
                record.setStudent(student);
            }
            record.setStatus(item.getStatus());
            record.setMarkingMethod(MarkingMethod.MANUAL);
            record.setMarkedAt(now);
            attendanceRecordRepository.save(record);
        }

        return buildResponse(session);
    }

    private ManualAttendanceResponse buildResponse(Session session) {
        List<Enrollment> enrollments = enrollmentRepository.findByModuleId(session.getModule().getId());
        Map<Long, AttendanceRecord> recordsByStudentId = new HashMap<>();
        for (AttendanceRecord record : attendanceRecordRepository.findBySessionId(session.getId())) {
            recordsByStudentId.put(record.getStudent().getId(), record);
        }

        List<ManualAttendanceStudentResponse> students = enrollments.stream()
                .map(enrollment -> ManualAttendanceStudentResponse.from(
                        enrollment.getStudent(),
                        recordsByStudentId.get(enrollment.getStudent().getId())))
                .toList();

        return ManualAttendanceResponse.from(session, students);
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

    private void validateEditable(Session session) {
        if (session.isLocked()
                || session.getStatus() == SessionStatus.LOCKED
                || session.getStatus() == SessionStatus.COMPLETED) {
            throw new BadRequestException("Locked or completed sessions cannot be edited.");
        }
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BadRequestException("Manual attendance can only be edited for active sessions.");
        }
    }
}
