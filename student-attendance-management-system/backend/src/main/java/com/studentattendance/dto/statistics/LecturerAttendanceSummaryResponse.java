package com.studentattendance.dto.statistics;

import java.math.BigDecimal;
import java.util.List;

public record LecturerAttendanceSummaryResponse(
        BigDecimal lowAttendanceThreshold,
        int assignedModules,
        int completedSessions,
        int enrolledStudents,
        BigDecimal averageAttendancePercentage,
        int lowAttendanceStudents,
        List<LecturerModuleAttendanceSummary> modules) {
}
