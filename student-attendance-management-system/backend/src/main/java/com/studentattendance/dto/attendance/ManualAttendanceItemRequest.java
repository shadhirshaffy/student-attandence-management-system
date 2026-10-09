package com.studentattendance.dto.attendance;

import com.studentattendance.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public class ManualAttendanceItemRequest {

    @NotNull
    private Long studentId;

    @NotNull
    private AttendanceStatus status;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }
}
