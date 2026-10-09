package com.studentattendance.dto.statistics;

import java.math.BigDecimal;
import java.util.List;

public record AdminAttendanceSummaryResponse(
        BigDecimal lowAttendanceThreshold,
        int totalModules,
        int totalStudents,
        int completedSessions,
        int attendanceRecords,
        BigDecimal averageAttendancePercentage,
        int lowAttendanceStudentModules,
        List<LecturerModuleAttendanceSummary> modules) {
}
