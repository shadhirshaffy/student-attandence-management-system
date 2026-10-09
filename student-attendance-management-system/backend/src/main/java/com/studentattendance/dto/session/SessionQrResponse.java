package com.studentattendance.dto.session;

import java.time.LocalDateTime;

import com.studentattendance.entity.Session;
import com.studentattendance.entity.SessionStatus;

public record SessionQrResponse(
        Long sessionId,
        Long moduleId,
        String moduleCode,
        String moduleName,
        String room,
        SessionStatus status,
        boolean locked,
        String token,
        LocalDateTime tokenExpiry,
        String attendanceUrl) {

    public static SessionQrResponse from(Session session, String attendanceUrl) {
        return new SessionQrResponse(
                session.getId(),
                session.getModule().getId(),
                session.getModule().getModuleCode(),
                session.getModule().getModuleName(),
                session.getRoom(),
                session.getStatus(),
                session.isLocked(),
                session.getAttendanceToken(),
                session.getTokenExpiry(),
                attendanceUrl);
    }
}
