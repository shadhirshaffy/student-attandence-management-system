package com.studentattendance.dto.report;

import java.math.BigDecimal;

public record AttendanceReportRow(
        Long studentId,
        String studentNumber,
        String studentName,
        Long moduleId,
        String moduleCode,
        String moduleName,
        int sessionsHeld,
        int sessionsPresent,
        int sessionsAbsent,
        BigDecimal attendancePercentage) {
}
