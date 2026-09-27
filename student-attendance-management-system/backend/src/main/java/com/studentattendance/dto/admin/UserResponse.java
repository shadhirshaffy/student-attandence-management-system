package com.studentattendance.dto.admin;

import java.time.LocalDateTime;

import com.studentattendance.entity.Role;
import com.studentattendance.entity.User;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;
    private boolean active;
    private Long profileId;
    private String studentNumber;
    private String employeeNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();
        response.id = user.getId();
        response.name = user.getName();
        response.email = user.getEmail();
        response.role = user.getRole();
        response.active = user.isActive();
        response.createdAt = user.getCreatedAt();
        response.updatedAt = user.getUpdatedAt();

        if (user.getStudent() != null) {
            response.profileId = user.getStudent().getId();
            response.studentNumber = user.getStudent().getStudentNumber();
        }
        if (user.getLecturer() != null) {
            response.profileId = user.getLecturer().getId();
            response.employeeNumber = user.getLecturer().getEmployeeNumber();
        }
        if (user.getAdmin() != null) {
            response.profileId = user.getAdmin().getId();
        }

        return response;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public Long getProfileId() {
        return profileId;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
