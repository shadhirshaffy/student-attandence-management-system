package com.studentattendance.dto.enrollment;

import jakarta.validation.constraints.NotNull;

public class EnrollmentRequest {

    @NotNull
    private Long studentId;

    @NotNull
    private Long moduleId;

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }
}
