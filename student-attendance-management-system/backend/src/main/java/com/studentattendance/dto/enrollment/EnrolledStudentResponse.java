package com.studentattendance.dto.enrollment;

import java.time.LocalDateTime;

import com.studentattendance.entity.Enrollment;

public record EnrolledStudentResponse(
        Long enrollmentId,
        Long studentId,
        Long userId,
        String name,
        String email,
        String studentNumber,
        LocalDateTime enrolledAt) {

    public static EnrolledStudentResponse from(Enrollment enrollment) {
        return new EnrolledStudentResponse(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getUser().getId(),
                enrollment.getStudent().getUser().getName(),
                enrollment.getStudent().getUser().getEmail(),
                enrollment.getStudent().getStudentNumber(),
                enrollment.getEnrolledAt());
    }
}
