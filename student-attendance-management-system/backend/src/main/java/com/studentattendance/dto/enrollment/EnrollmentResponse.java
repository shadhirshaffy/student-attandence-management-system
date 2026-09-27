package com.studentattendance.dto.enrollment;

import java.time.LocalDateTime;

import com.studentattendance.entity.Enrollment;

public class EnrollmentResponse {

    private Long id;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private String studentNumber;
    private Long moduleId;
    private String moduleCode;
    private String moduleName;
    private LocalDateTime enrolledAt;

    public static EnrollmentResponse from(Enrollment enrollment) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.id = enrollment.getId();
        response.enrolledAt = enrollment.getEnrolledAt();
        response.studentId = enrollment.getStudent().getId();
        response.studentNumber = enrollment.getStudent().getStudentNumber();
        if (enrollment.getStudent().getUser() != null) {
            response.studentName = enrollment.getStudent().getUser().getName();
            response.studentEmail = enrollment.getStudent().getUser().getEmail();
        }
        response.moduleId = enrollment.getModule().getId();
        response.moduleCode = enrollment.getModule().getModuleCode();
        response.moduleName = enrollment.getModule().getModuleName();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public String getStudentNumber() {
        return studentNumber;
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

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }
}
