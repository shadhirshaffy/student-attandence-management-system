package com.studentattendance.dto.statistics;

import java.math.BigDecimal;
import java.util.List;

public record StudentAttendanceSummaryResponse(
        BigDecimal lowAttendanceThreshold,
        int enrolledModules,
        int lowAttendanceModules,
        int eligibleCompletedSessions,
        int presentSessions,
        int absentSessions,
        BigDecimal overallAttendancePercentage,
        boolean overallLowAttendance,
        List<ModuleAttendanceSummary> modules,
        List<AttendanceHistoryItem> history) {
}
