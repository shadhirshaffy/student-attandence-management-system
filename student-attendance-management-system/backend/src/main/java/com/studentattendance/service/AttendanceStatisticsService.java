package com.studentattendance.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.studentattendance.dto.statistics.AdminAttendanceSummaryResponse;
import com.studentattendance.dto.statistics.AttendanceHistoryItem;
import com.studentattendance.dto.statistics.LecturerAttendanceSummaryResponse;
import com.studentattendance.dto.statistics.LecturerModuleAttendanceSummary;
import com.studentattendance.dto.statistics.ModuleAttendanceSummary;
import com.studentattendance.dto.statistics.StudentAttendanceSummaryResponse;
import com.studentattendance.dto.statistics.StudentModuleAttendanceSummary;
import com.studentattendance.entity.AttendanceRecord;
import com.studentattendance.entity.AttendanceStatus;
import com.studentattendance.entity.Enrollment;
import com.studentattendance.entity.Lecturer;
import com.studentattendance.entity.Module;
import com.studentattendance.entity.Session;
import com.studentattendance.entity.SessionStatus;
import com.studentattendance.entity.Student;
import com.studentattendance.exception.NotFoundException;
import com.studentattendance.repository.AttendanceRecordRepository;
import com.studentattendance.repository.EnrollmentRepository;
import com.studentattendance.repository.LecturerRepository;
import com.studentattendance.repository.ModuleRepository;
import com.studentattendance.repository.SessionRepository;
import com.studentattendance.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AttendanceStatisticsService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LecturerRepository lecturerRepository;
    private final ModuleRepository moduleRepository;
    private final SessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final BigDecimal lowAttendanceThreshold;

    public AttendanceStatisticsService(
            AttendanceRecordRepository attendanceRecordRepository,
            EnrollmentRepository enrollmentRepository,
            LecturerRepository lecturerRepository,
            ModuleRepository moduleRepository,
            SessionRepository sessionRepository,
            StudentRepository studentRepository,
            @Value("${app.attendance.low-threshold:80}") BigDecimal lowAttendanceThreshold) {
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lecturerRepository = lecturerRepository;
        this.moduleRepository = moduleRepository;
        this.sessionRepository = sessionRepository;
        this.studentRepository = studentRepository;
        this.lowAttendanceThreshold = lowAttendanceThreshold;
    }

    public StudentAttendanceSummaryResponse getStudentSummary(Long studentUserId) {
        Student student = studentRepository.findByUserId(studentUserId)
                .orElseThrow(() -> new NotFoundException("Student profile not found."));
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());

        List<ModuleAttendanceSummary> modules = enrollments.stream()
                .map(enrollment -> summarizeStudentModule(student, enrollment.getModule()))
                .toList();

        int eligible = modules.stream().mapToInt(module -> module.eligibleCompletedSessions()).sum();
        int present = modules.stream().mapToInt(module -> module.presentSessions()).sum();
        int absent = Math.max(eligible - present, 0);
        BigDecimal overallPercentage = percentage(present, eligible);
        List<AttendanceHistoryItem> history = buildStudentHistory(student, enrollments);

        return new StudentAttendanceSummaryResponse(
                lowAttendanceThreshold,
                enrollments.size(),
                (int) modules.stream().filter(module -> module.lowAttendance()).count(),
                eligible,
                present,
                absent,
                overallPercentage,
                isLow(overallPercentage, eligible),
                modules,
                history);
    }

    public LecturerAttendanceSummaryResponse getLecturerSummary(Long lecturerUserId) {
        Lecturer lecturer = lecturerRepository.findByUserId(lecturerUserId)
                .orElseThrow(() -> new NotFoundException("Lecturer profile not found."));
        List<Module> modules = moduleRepository.findByLecturerId(lecturer.getId());
        List<LecturerModuleAttendanceSummary> moduleSummaries = modules.stream()
                .map(module -> summarizeModuleForLecturer(module))
                .toList();

        int completedSessions = moduleSummaries.stream().mapToInt(module -> module.completedSessions()).sum();
        int enrolledStudents = moduleSummaries.stream().mapToInt(module -> module.enrolledStudents()).sum();
        int lowStudents = moduleSummaries.stream().mapToInt(module -> module.lowAttendanceStudents()).sum();
        BigDecimal average = averageModulePercentage(moduleSummaries);

        return new LecturerAttendanceSummaryResponse(
                lowAttendanceThreshold,
                modules.size(),
                completedSessions,
                enrolledStudents,
                average,
                lowStudents,
                moduleSummaries);
    }

    public LecturerModuleAttendanceSummary getLecturerModuleSummary(Long lecturerUserId, Long moduleId) {
        Lecturer lecturer = lecturerRepository.findByUserId(lecturerUserId)
                .orElseThrow(() -> new NotFoundException("Lecturer profile not found."));
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new NotFoundException("Module not found."));
        if (!module.getLecturer().getId().equals(lecturer.getId())) {
            throw new org.springframework.security.access.AccessDeniedException("You are not assigned to this module.");
        }
        return summarizeModuleForLecturer(module);
    }

    public AdminAttendanceSummaryResponse getAdminSummary() {
        List<Module> modules = moduleRepository.findAll();
        List<LecturerModuleAttendanceSummary> moduleSummaries = modules.stream()
                .map(module -> summarizeModuleForLecturer(module))
                .toList();

        return new AdminAttendanceSummaryResponse(
                lowAttendanceThreshold,
                modules.size(),
                (int) studentRepository.count(),
                moduleSummaries.stream().mapToInt(module -> module.completedSessions()).sum(),
                (int) attendanceRecordRepository.count(),
                averageModulePercentage(moduleSummaries),
                moduleSummaries.stream().mapToInt(module -> module.lowAttendanceStudents()).sum(),
                moduleSummaries);
    }

    private LecturerModuleAttendanceSummary summarizeModuleForLecturer(Module module) {
        List<Enrollment> enrollments = enrollmentRepository.findByModuleId(module.getId());
        List<Session> completedSessions = completedSessions(module);
        List<StudentModuleAttendanceSummary> students = enrollments.stream()
                .map(enrollment -> summarizeStudentForModule(enrollment.getStudent(), module, completedSessions))
                .toList();

        return new LecturerModuleAttendanceSummary(
                module.getId(),
                module.getModuleCode(),
                module.getModuleName(),
                enrollments.size(),
                completedSessions.size(),
                averageStudentPercentage(students),
                (int) students.stream().filter(student -> student.lowAttendance()).count(),
                students);
    }

    private ModuleAttendanceSummary summarizeStudentModule(Student student, Module module) {
        List<Session> completedSessions = completedSessions(module);
        StudentModuleAttendanceSummary summary = summarizeStudentForModule(student, module, completedSessions);
        return new ModuleAttendanceSummary(
                module.getId(),
                module.getModuleCode(),
                module.getModuleName(),
                summary.eligibleCompletedSessions(),
                summary.presentSessions(),
                summary.absentSessions(),
                summary.attendancePercentage(),
                summary.lowAttendance());
    }

    private StudentModuleAttendanceSummary summarizeStudentForModule(
            Student student,
            Module module,
            List<Session> completedSessions) {
        Map<Long, AttendanceRecord> recordsBySessionId = recordsBySessionId(student.getId());
        int present = 0;
        for (Session session : completedSessions) {
            AttendanceRecord record = recordsBySessionId.get(session.getId());
            if (record != null && record.getStatus() == AttendanceStatus.PRESENT) {
                present++;
            }
        }

        int eligible = completedSessions.size();
        int absent = Math.max(eligible - present, 0);
        BigDecimal attendancePercentage = percentage(present, eligible);
        return new StudentModuleAttendanceSummary(
                student.getId(),
                student.getStudentNumber(),
                student.getUser().getName(),
                student.getUser().getEmail(),
                eligible,
                present,
                absent,
                attendancePercentage,
                isLow(attendancePercentage, eligible));
    }

    private List<AttendanceHistoryItem> buildStudentHistory(Student student, List<Enrollment> enrollments) {
        Map<Long, AttendanceRecord> recordsBySessionId = recordsBySessionId(student.getId());
        return enrollments.stream()
                .flatMap(enrollment -> completedSessions(enrollment.getModule()).stream())
                .sorted(Comparator
                        .comparing((Session session) -> session.getSessionDate())
                        .reversed()
                        .thenComparing(session -> session.getStartTime()))
                .map(session -> {
                    AttendanceRecord record = recordsBySessionId.get(session.getId());
                    String status = record != null && record.getStatus() == AttendanceStatus.PRESENT ? "PRESENT" : "ABSENT";
                    return new AttendanceHistoryItem(
                            session.getId(),
                            session.getModule().getId(),
                            session.getModule().getModuleCode(),
                            session.getModule().getModuleName(),
                            session.getSessionDate(),
                            session.getStartTime(),
                            session.getEndTime(),
                            session.getRoom(),
                            status,
                            record == null ? null : record.getMarkingMethod().name(),
                            record == null || record.getMarkedAt() == null ? null : record.getMarkedAt().toString());
                })
                .toList();
    }

    private List<Session> completedSessions(Module module) {
        return sessionRepository.findByModuleIdAndStatus(module.getId(), SessionStatus.COMPLETED);
    }

    private Map<Long, AttendanceRecord> recordsBySessionId(Long studentId) {
        Map<Long, AttendanceRecord> records = new HashMap<>();
        for (AttendanceRecord record : attendanceRecordRepository.findByStudentId(studentId)) {
            records.put(record.getSession().getId(), record);
        }
        return records;
    }

    private BigDecimal percentage(int present, int eligible) {
        if (eligible == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(present)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(eligible), 2, RoundingMode.HALF_UP);
    }

    private boolean isLow(BigDecimal attendancePercentage, int eligible) {
        return eligible > 0 && attendancePercentage.compareTo(lowAttendanceThreshold) < 0;
    }

    private BigDecimal averageStudentPercentage(List<StudentModuleAttendanceSummary> students) {
        List<StudentModuleAttendanceSummary> eligibleStudents = students.stream()
                .filter(student -> student.eligibleCompletedSessions() > 0)
                .toList();
        if (eligibleStudents.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal total = eligibleStudents.stream()
                .map(student -> student.attendancePercentage())
                .reduce(BigDecimal.ZERO, (runningTotal, percentage) -> runningTotal.add(percentage));
        return total.divide(BigDecimal.valueOf(eligibleStudents.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal averageModulePercentage(List<LecturerModuleAttendanceSummary> modules) {
        List<LecturerModuleAttendanceSummary> eligibleModules = modules.stream()
                .filter(module -> module.completedSessions() > 0 && module.enrolledStudents() > 0)
                .toList();
        if (eligibleModules.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal total = eligibleModules.stream()
                .map(module -> module.averageAttendancePercentage())
                .reduce(BigDecimal.ZERO, (runningTotal, percentage) -> runningTotal.add(percentage));
        return total.divide(BigDecimal.valueOf(eligibleModules.size()), 2, RoundingMode.HALF_UP);
    }
}
