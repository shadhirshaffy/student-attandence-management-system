package com.studentattendance.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.studentattendance.dto.attendance.QrAttendanceRequest;
import com.studentattendance.dto.attendance.QrAttendanceResponse;
import com.studentattendance.dto.session.SessionQrResponse;
import com.studentattendance.entity.AttendanceRecord;
import com.studentattendance.entity.AttendanceStatus;
import com.studentattendance.entity.Lecturer;
import com.studentattendance.entity.MarkingMethod;
import com.studentattendance.entity.Session;
import com.studentattendance.entity.SessionStatus;
import com.studentattendance.entity.Student;
import com.studentattendance.exception.BadRequestException;
import com.studentattendance.exception.ConflictException;
import com.studentattendance.exception.NotFoundException;
import com.studentattendance.repository.AttendanceRecordRepository;
import com.studentattendance.repository.EnrollmentRepository;
import com.studentattendance.repository.LecturerRepository;
import com.studentattendance.repository.SessionRepository;
import com.studentattendance.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class QrAttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LecturerRepository lecturerRepository;
    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final String frontendBaseUrl;

    public QrAttendanceService(
            AttendanceRecordRepository attendanceRecordRepository,
            EnrollmentRepository enrollmentRepository,
            LecturerRepository lecturerRepository,
            SessionRepository sessionRepository,
            StudentRepository studentRepository,
            @Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lecturerRepository = lecturerRepository;
        this.sessionRepository = sessionRepository;
        this.studentRepository = studentRepository;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public SessionQrResponse getSessionQr(Long lecturerUserId, Long sessionId) {
        Lecturer lecturer = lecturerRepository.findByUserId(lecturerUserId)
                .orElseThrow(() -> new NotFoundException("Lecturer profile not found."));
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found."));
        if (!session.getLecturer().getId().equals(lecturer.getId())) {
            throw new AccessDeniedException("You do not own this session.");
        }
        validateQrDisplayable(session);
        return SessionQrResponse.from(session, buildAttendanceUrl(session.getAttendanceToken()));
    }

    @Transactional
    public QrAttendanceResponse markAttendance(Long studentUserId, QrAttendanceRequest request) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new NotFoundException("Student profile not found."));
        Session session = sessionRepository.findForUpdateByToken(request.getToken().trim())
                .orElseThrow(() -> new BadRequestException("Invalid attendance QR code."));

        validateQrUsable(session, request.getToken().trim());

        if (!enrollmentRepository.existsByStudentIdAndModuleId(student.getId(), session.getModule().getId())) {
            throw new AccessDeniedException("You are not enrolled in this module.");
        }
        if (attendanceRecordRepository.existsByStudentIdAndSessionId(student.getId(), session.getId())) {
            throw new ConflictException("Attendance has already been recorded.");
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setSession(session);
        record.setStudent(student);
        record.setStatus(AttendanceStatus.PRESENT);
        record.setMarkingMethod(MarkingMethod.QR);
        record.setMarkedAt(LocalDateTime.now());
        return QrAttendanceResponse.from(attendanceRecordRepository.save(record));
    }

    private void validateQrDisplayable(Session session) {
        validateQrSessionState(session);
        if (session.getAttendanceToken() == null || session.getTokenExpiry() == null) {
            throw new BadRequestException("QR attendance is not enabled for this session.");
        }
        if (session.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Attendance QR code has expired.");
        }
    }

    private void validateQrUsable(Session session, String token) {
        validateQrSessionState(session);
        if (!token.equals(session.getAttendanceToken())) {
            throw new BadRequestException("Invalid attendance QR code.");
        }
        if (session.getTokenExpiry() == null || !session.getTokenExpiry().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Attendance QR code has expired.");
        }
    }

    private void validateQrSessionState(Session session) {
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new BadRequestException("Attendance QR code is not active.");
        }
        if (session.isLocked() || session.getStatus() == SessionStatus.LOCKED || session.getStatus() == SessionStatus.COMPLETED) {
            throw new BadRequestException("Attendance QR code is no longer available.");
        }
    }

    private String buildAttendanceUrl(String token) {
        String separator = frontendBaseUrl.endsWith("/") ? "" : "/";
        return frontendBaseUrl + separator + "student/scan?token="
                + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }
}
