package com.studentattendance.dto.statistics;

import java.math.BigDecimal;

public record StudentModuleAttendanceSummary(
        Long studentId,
        String studentNumber,
        String studentName,
        String email,
        int eligibleCompletedSessions,
        int presentSessions,
        int absentSessions,
        BigDecimal attendancePercentage,
        boolean lowAttendance) {
}
