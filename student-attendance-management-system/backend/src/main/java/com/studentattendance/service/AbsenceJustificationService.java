package com.studentattendance.service;

import java.time.LocalDateTime;
import java.util.List;

import com.studentattendance.dto.justification.AbsenceJustificationRequest;
import com.studentattendance.dto.justification.AbsenceJustificationResponse;
import com.studentattendance.dto.justification.AbsenceJustificationReviewRequest;
import com.studentattendance.entity.AbsenceJustification;
import com.studentattendance.entity.AttendanceRecord;
import com.studentattendance.entity.AttendanceStatus;
import com.studentattendance.entity.JustificationStatus;
import com.studentattendance.entity.Lecturer;
import com.studentattendance.entity.Session;
import com.studentattendance.entity.SessionStatus;
import com.studentattendance.entity.Student;
import com.studentattendance.exception.BadRequestException;
import com.studentattendance.exception.ConflictException;
import com.studentattendance.exception.NotFoundException;
import com.studentattendance.repository.AbsenceJustificationRepository;
import com.studentattendance.repository.AttendanceRecordRepository;
import com.studentattendance.repository.EnrollmentRepository;
import com.studentattendance.repository.LecturerRepository;
import com.studentattendance.repository.SessionRepository;
import com.studentattendance.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AbsenceJustificationService {

    private final AbsenceJustificationRepository absenceJustificationRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LecturerRepository lecturerRepository;
    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;

    public AbsenceJustificationService(
            AbsenceJustificationRepository absenceJustificationRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            EnrollmentRepository enrollmentRepository,
            LecturerRepository lecturerRepository,
            SessionRepository sessionRepository,
            StudentRepository studentRepository) {
        this.absenceJustificationRepository = absenceJustificationRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lecturerRepository = lecturerRepository;
        this.sessionRepository = sessionRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public AbsenceJustificationResponse submit(Long studentUserId, AbsenceJustificationRequest request) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new NotFoundException("Student profile not found."));
        Session session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new NotFoundException("Session not found."));

        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new BadRequestException("Justifications can only be submitted for completed sessions.");
        }
        boolean enrolled = enrollmentRepository.findByModuleId(session.getModule().getId()).stream()
                .anyMatch(enrollment -> enrollment.getStudent().getId().equals(student.getId()));
        if (!enrolled) {
            throw new AccessDeniedException("You are not enrolled in this session's module.");
        }
        AttendanceRecord record = attendanceRecordRepository
                .findByStudentIdAndSessionId(student.getId(), session.getId())
                .orElse(null);
        if (record != null && record.getStatus() == AttendanceStatus.PRESENT) {
            throw new BadRequestException("Present attendance records do not need absence justification.");
        }
        if (absenceJustificationRepository.findByStudentIdAndSessionId(student.getId(), session.getId()).isPresent()) {
            throw new ConflictException("A justification has already been submitted for this session.");
        }

        AbsenceJustification justification = new AbsenceJustification();
        justification.setStudent(student);
        justification.setSession(session);
        justification.setReason(request.getReason().trim());
        justification.setStatus(JustificationStatus.PENDING);
        return AbsenceJustificationResponse.from(absenceJustificationRepository.save(justification));
    }

    public List<AbsenceJustificationResponse> listMine(Long studentUserId) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new NotFoundException("Student profile not found."));
        return absenceJustificationRepository.findByStudentId(student.getId()).stream()
                .map(AbsenceJustificationResponse::from)
                .toList();
    }

    public List<AbsenceJustificationResponse> listForLecturer(Long lecturerUserId) {
        Lecturer lecturer = lecturerRepository.findByUserId(lecturerUserId)
                .orElseThrow(() -> new NotFoundException("Lecturer profile not found."));
        return absenceJustificationRepository.findBySessionModuleLecturerId(lecturer.getId()).stream()
                .map(AbsenceJustificationResponse::from)
                .toList();
    }

    @Transactional
    public AbsenceJustificationResponse review(
            Long lecturerUserId,
            Long justificationId,
            AbsenceJustificationReviewRequest request) {
        Lecturer lecturer = lecturerRepository.findByUserId(lecturerUserId)
                .orElseThrow(() -> new NotFoundException("Lecturer profile not found."));
        AbsenceJustification justification = absenceJustificationRepository.findById(justificationId)
                .orElseThrow(() -> new NotFoundException("Justification not found."));
        if (!justification.getSession().getModule().getLecturer().getId().equals(lecturer.getId())) {
            throw new AccessDeniedException("You do not own this justification's module.");
        }
        if (request.getStatus() == JustificationStatus.PENDING) {
            throw new BadRequestException("Review status must be APPROVED or REJECTED.");
        }
        justification.setStatus(request.getStatus());
        justification.setReviewNote(request.getReviewNote() == null ? null : request.getReviewNote().trim());
        justification.setReviewedAt(LocalDateTime.now());
        justification.setReviewedBy(lecturer.getUser());
        return AbsenceJustificationResponse.from(absenceJustificationRepository.save(justification));
    }
}
