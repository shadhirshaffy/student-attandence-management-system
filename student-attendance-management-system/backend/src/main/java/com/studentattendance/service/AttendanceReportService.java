package com.studentattendance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.studentattendance.dto.report.AttendanceReportResponse;
import com.studentattendance.dto.report.AttendanceReportRow;
import com.studentattendance.entity.AttendanceRecord;
import com.studentattendance.entity.AttendanceStatus;
import com.studentattendance.entity.Enrollment;
import com.studentattendance.entity.Lecturer;
import com.studentattendance.entity.Module;
import com.studentattendance.entity.Role;
import com.studentattendance.entity.Session;
import com.studentattendance.entity.SessionStatus;
import com.studentattendance.entity.Student;
import com.studentattendance.exception.BadRequestException;
import com.studentattendance.exception.NotFoundException;
import com.studentattendance.repository.AttendanceRecordRepository;
import com.studentattendance.repository.EnrollmentRepository;
import com.studentattendance.repository.LecturerRepository;
import com.studentattendance.repository.ModuleRepository;
import com.studentattendance.repository.SessionRepository;
import com.studentattendance.repository.StudentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class AttendanceReportService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LecturerRepository lecturerRepository;
    private final ModuleRepository moduleRepository;
    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;

    public AttendanceReportService(
            AttendanceRecordRepository attendanceRecordRepository,
            EnrollmentRepository enrollmentRepository,
            LecturerRepository lecturerRepository,
            ModuleRepository moduleRepository,
            SessionRepository sessionRepository,
            StudentRepository studentRepository) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lecturerRepository = lecturerRepository;
        this.moduleRepository = moduleRepository;
        this.sessionRepository = sessionRepository;
        this.studentRepository = studentRepository;
    }

    public AttendanceReportResponse generateReport(
            Role role,
            Long userId,
            Long moduleId,
            Long studentId,
            Long sessionId,
            LocalDate dateFrom,
            LocalDate dateTo) {
        if (dateFrom != null && dateTo != null && dateFrom.isAfter(dateTo)) {
            throw new BadRequestException("Start date must be before end date.");
        }

        Lecturer lecturer = role == Role.LECTURER
                ? lecturerRepository.findByUserId(userId).orElseThrow(() -> new NotFoundException("Lecturer profile not found."))
                : null;

        List<Module> modules = resolveModules(role, lecturer, moduleId, sessionId);
        Student selectedStudent = studentId == null ? null : studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found."));

        List<AttendanceReportRow> rows = new ArrayList<>();
        for (Module module : modules) {
            List<Session> sessions = completedSessions(module, sessionId, dateFrom, dateTo);
            List<Enrollment> enrollments = enrollmentRepository.findByModuleId(module.getId());
            for (Enrollment enrollment : enrollments) {
                if (selectedStudent != null && !enrollment.getStudent().getId().equals(selectedStudent.getId())) {
                    continue;
                }
                rows.add(buildRow(enrollment.getStudent(), module, sessions));
            }
        }

        rows.sort(Comparator
                .comparing((AttendanceReportRow row) -> row.moduleCode())
                .thenComparing(row -> row.studentNumber()));

        return new AttendanceReportResponse(
                "Attendance Report",
                dateFrom,
                dateTo,
                moduleId,
                studentId,
                sessionId,
                rows.size(),
                rows);
    }

    public byte[] exportCsv(AttendanceReportResponse report) {
        StringBuilder csv = new StringBuilder();
        csv.append("Student Number,Student Name,Module Code,Module Name,Sessions Held,Sessions Present,Sessions Absent,Attendance Percentage\n");
        for (AttendanceReportRow row : report.rows()) {
            csv.append(csv(row.studentNumber())).append(',')
                    .append(csv(row.studentName())).append(',')
                    .append(csv(row.moduleCode())).append(',')
                    .append(csv(row.moduleName())).append(',')
                    .append(row.sessionsHeld()).append(',')
                    .append(row.sessionsPresent()).append(',')
                    .append(row.sessionsAbsent()).append(',')
                    .append(row.attendancePercentage()).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportPdf(AttendanceReportResponse report) {
        List<String> lines = new ArrayList<>();
        lines.add(report.title());
        lines.add("Rows: " + report.rowCount());
        if (report.dateFrom() != null || report.dateTo() != null) {
            lines.add("Date range: " + value(report.dateFrom()) + " to " + value(report.dateTo()));
        }
        lines.add(" ");
        lines.add("Student | Module | Held | Present | Absent | %");
        for (AttendanceReportRow row : report.rows()) {
            lines.add(row.studentNumber() + " " + row.studentName()
                    + " | " + row.moduleCode()
                    + " | " + row.sessionsHeld()
                    + " | " + row.sessionsPresent()
                    + " | " + row.sessionsAbsent()
                    + " | " + row.attendancePercentage());
        }
        return SimplePdfWriter.write(lines);
    }

    public String filename(String extension, Long moduleId, Long studentId, Long sessionId) {
        String scope = "attendance-report";
        if (sessionId != null) {
            scope += "-session-" + sessionId;
        } else if (moduleId != null) {
            scope += "-module-" + moduleId;
        } else if (studentId != null) {
            scope += "-student-" + studentId;
        }
        return scope + "." + extension;
    }

    private List<Module> resolveModules(Role role, Lecturer lecturer, Long moduleId, Long sessionId) {
        if (sessionId != null) {
            Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new NotFoundException("Session not found."));
            validateModuleAccess(role, lecturer, session.getModule());
            if (moduleId != null && !moduleId.equals(session.getModule().getId())) {
                throw new BadRequestException("Session does not belong to the selected module.");
            }
            return List.of(session.getModule());
        }

        if (moduleId != null) {
            Module module = moduleRepository.findById(moduleId)
                    .orElseThrow(() -> new NotFoundException("Module not found."));
            validateModuleAccess(role, lecturer, module);
            return List.of(module);
        }

        if (role == Role.LECTURER) {
            return moduleRepository.findByLecturerId(lecturer.getId());
        }
        return moduleRepository.findAll();
    }

    private void validateModuleAccess(Role role, Lecturer lecturer, Module module) {
        if (role == Role.ADMIN) {
            return;
        }
        if (lecturer == null || module.getLecturer() == null || !module.getLecturer().getId().equals(lecturer.getId())) {
            throw new AccessDeniedException("You are not assigned to this module.");
        }
    }

    private List<Session> completedSessions(Module module, Long sessionId, LocalDate dateFrom, LocalDate dateTo) {
        List<Session> sessions;
        if (sessionId != null) {
            Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new NotFoundException("Session not found."));
            sessions = session.getStatus() == SessionStatus.COMPLETED ? List.of(session) : List.of();
        } else {
            sessions = sessionRepository.findByModuleIdAndStatus(module.getId(), SessionStatus.COMPLETED);
        }

        return sessions.stream()
                .filter(session -> dateFrom == null || !session.getSessionDate().isBefore(dateFrom))
                .filter(session -> dateTo == null || !session.getSessionDate().isAfter(dateTo))
                .sorted(Comparator
                        .comparing((Session session) -> session.getSessionDate())
                        .thenComparing(session -> session.getStartTime()))
                .toList();
    }

    private AttendanceReportRow buildRow(Student student, Module module, List<Session> sessions) {
        Map<Long, AttendanceRecord> records = new HashMap<>();
        for (AttendanceRecord record : attendanceRecordRepository.findByStudentId(student.getId())) {
            records.put(record.getSession().getId(), record);
        }

        int present = 0;
        for (Session session : sessions) {
            AttendanceRecord record = records.get(session.getId());
            if (record != null && record.getStatus() == AttendanceStatus.PRESENT) {
                present++;
            }
        }

        int held = sessions.size();
        int absent = Math.max(held - present, 0);
        return new AttendanceReportRow(
                student.getId(),
                student.getStudentNumber(),
                student.getUser().getName(),
                module.getId(),
                module.getModuleCode(),
                module.getModuleName(),
                held,
                present,
                absent,
                percentage(present, held));
    }

    private BigDecimal percentage(int present, int held) {
        if (held == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(present)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(held), 2, RoundingMode.HALF_UP);
    }

    private String csv(String value) {
        String safe = value == null ? "" : value;
        if (!safe.stripLeading().isEmpty() && "=+@-".indexOf(safe.stripLeading().charAt(0)) >= 0
                || safe.startsWith("\t") || safe.startsWith("\r") || safe.startsWith("\n")) {
            safe = "'" + safe;
        }
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private String value(Object value) {
        return value == null ? "" : value.toString();
    }
}
