package com.studentattendance.dto.attendance;

import jakarta.validation.constraints.NotBlank;

public class QrAttendanceRequest {

    @NotBlank
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
