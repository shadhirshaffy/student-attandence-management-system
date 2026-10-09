package com.studentattendance.dto.statistics;

import java.math.BigDecimal;
import java.util.List;

public record LecturerModuleAttendanceSummary(
        Long moduleId,
        String moduleCode,
        String moduleName,
        int enrolledStudents,
        int completedSessions,
        BigDecimal averageAttendancePercentage,
        int lowAttendanceStudents,
        List<StudentModuleAttendanceSummary> students) {
}
