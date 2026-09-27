package com.studentattendance.dto.auth;

import com.studentattendance.entity.Role;
import com.studentattendance.entity.User;

public class CurrentUserResponse {

    private Long userId;
    private String name;
    private String email;
    private Role role;

    public CurrentUserResponse(Long userId, String name, String email, Role role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public static CurrentUserResponse from(User user) {
        return new CurrentUserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    public Long getUserId() {
        return userId;
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
}
