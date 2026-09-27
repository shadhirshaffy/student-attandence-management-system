package com.studentattendance.dto.session;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.studentattendance.entity.Session;
import com.studentattendance.entity.SessionStatus;

public class SessionResponse {

    private Long id;
    private Long moduleId;
    private String moduleCode;
    private String moduleName;
    private Long lecturerId;
    private String lecturerName;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String room;
    private SessionStatus status;
    private String attendanceToken;
    private LocalDateTime tokenExpiry;
    private boolean locked;
    private LocalDateTime createdAt;

    public static SessionResponse from(Session session) {
        SessionResponse response = new SessionResponse();
        response.id = session.getId();
        response.sessionDate = session.getSessionDate();
        response.startTime = session.getStartTime();
        response.endTime = session.getEndTime();
        response.room = session.getRoom();
        response.status = session.getStatus();
        response.attendanceToken = session.getAttendanceToken();
        response.tokenExpiry = session.getTokenExpiry();
        response.locked = session.isLocked();
        response.createdAt = session.getCreatedAt();

        if (session.getModule() != null) {
            response.moduleId = session.getModule().getId();
            response.moduleCode = session.getModule().getModuleCode();
            response.moduleName = session.getModule().getModuleName();
        }
        if (session.getLecturer() != null) {
            response.lecturerId = session.getLecturer().getId();
            if (session.getLecturer().getUser() != null) {
                response.lecturerName = session.getLecturer().getUser().getName();
            }
        }
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public Long getLecturerId() {
        return lecturerId;
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public String getRoom() {
        return room;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public String getAttendanceToken() {
        return attendanceToken;
    }

    public LocalDateTime getTokenExpiry() {
        return tokenExpiry;
    }

    public boolean isLocked() {
        return locked;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
