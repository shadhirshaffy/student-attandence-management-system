package com.studentattendance.dto.report;

import java.time.LocalDate;
import java.util.List;

public record AttendanceReportResponse(
        String title,
        LocalDate dateFrom,
        LocalDate dateTo,
        Long moduleId,
        Long studentId,
        Long sessionId,
        int rowCount,
        List<AttendanceReportRow> rows) {
}
