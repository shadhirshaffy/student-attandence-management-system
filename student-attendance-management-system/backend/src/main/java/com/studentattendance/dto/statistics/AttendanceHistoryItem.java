package com.studentattendance.dto.statistics;

import java.time.LocalDate;
import java.time.LocalTime;

public record AttendanceHistoryItem(
        Long sessionId,
        Long moduleId,
        String moduleCode,
        String moduleName,
        LocalDate sessionDate,
        LocalTime startTime,
        LocalTime endTime,
        String room,
        String status,
        String markingMethod,
        String markedAt,
        Long justificationId,
        String justificationStatus,
        String justificationReason,
        String justificationReviewNote) {
}
