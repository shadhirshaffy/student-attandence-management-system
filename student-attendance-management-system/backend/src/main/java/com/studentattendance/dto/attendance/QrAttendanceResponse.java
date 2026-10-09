package com.studentattendance.dto.attendance;

import java.time.LocalDateTime;

import com.studentattendance.entity.AttendanceRecord;

public record QrAttendanceResponse(
        Long recordId,
        Long sessionId,
        Long moduleId,
        String moduleCode,
        String moduleName,
        String status,
        String markingMethod,
        LocalDateTime markedAt) {

    public static QrAttendanceResponse from(AttendanceRecord record) {
        return new QrAttendanceResponse(
                record.getId(),
                record.getSession().getId(),
                record.getSession().getModule().getId(),
                record.getSession().getModule().getModuleCode(),
                record.getSession().getModule().getModuleName(),
                record.getStatus().name(),
                record.getMarkingMethod().name(),
                record.getMarkedAt());
    }
}
