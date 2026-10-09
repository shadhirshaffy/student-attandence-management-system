package com.studentattendance.dto.attendance;

import java.util.List;

import com.studentattendance.entity.Session;
import com.studentattendance.entity.SessionStatus;

public record ManualAttendanceResponse(
        Long sessionId,
        Long moduleId,
        String moduleCode,
        String moduleName,
        SessionStatus sessionStatus,
        boolean editable,
        List<ManualAttendanceStudentResponse> students) {

    public static ManualAttendanceResponse from(Session session, List<ManualAttendanceStudentResponse> students) {
        return new ManualAttendanceResponse(
                session.getId(),
                session.getModule().getId(),
                session.getModule().getModuleCode(),
                session.getModule().getModuleName(),
                session.getStatus(),
                session.getStatus() == SessionStatus.ACTIVE && !session.isLocked(),
                students);
    }
}
