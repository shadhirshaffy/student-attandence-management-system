package com.studentattendance.dto.attendance;

import java.time.LocalDateTime;

import com.studentattendance.entity.AttendanceRecord;
import com.studentattendance.entity.AttendanceStatus;
import com.studentattendance.entity.MarkingMethod;
import com.studentattendance.entity.Student;

public record ManualAttendanceStudentResponse(
        Long studentId,
        String studentNumber,
        String studentName,
        String email,
        Long attendanceRecordId,
        AttendanceStatus status,
        MarkingMethod markingMethod,
        LocalDateTime markedAt,
        String markedByName,
        String manualChangeReason) {

    public static ManualAttendanceStudentResponse from(Student student, AttendanceRecord record) {
        return new ManualAttendanceStudentResponse(
                student.getId(),
                student.getStudentNumber(),
                student.getUser().getName(),
                student.getUser().getEmail(),
                record == null ? null : record.getId(),
                record == null ? null : record.getStatus(),
                record == null ? null : record.getMarkingMethod(),
                record == null ? null : record.getMarkedAt(),
                record == null || record.getMarkedBy() == null ? null : record.getMarkedBy().getName(),
                record == null ? null : record.getManualChangeReason());
    }
}
