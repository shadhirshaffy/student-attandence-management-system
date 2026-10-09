package com.studentattendance.dto.statistics;

import java.math.BigDecimal;

public record ModuleAttendanceSummary(
        Long moduleId,
        String moduleCode,
        String moduleName,
        int eligibleCompletedSessions,
        int presentSessions,
        int absentSessions,
        BigDecimal attendancePercentage,
        boolean lowAttendance) {
}
