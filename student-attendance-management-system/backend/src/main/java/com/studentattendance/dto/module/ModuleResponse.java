package com.studentattendance.dto.module;

import com.studentattendance.entity.Lecturer;
import com.studentattendance.entity.Module;

public class ModuleResponse {

    private Long id;
    private String moduleCode;
    private String moduleName;
    private String description;
    private boolean active;
    private Long lecturerId;
    private String lecturerName;
    private String lecturerEmail;
    private String employeeNumber;

    public static ModuleResponse from(Module module) {
        ModuleResponse response = new ModuleResponse();
        response.id = module.getId();
        response.moduleCode = module.getModuleCode();
        response.moduleName = module.getModuleName();
        response.description = module.getDescription();
        response.active = module.isActive();

        Lecturer lecturer = module.getLecturer();
        if (lecturer != null) {
            response.lecturerId = lecturer.getId();
            response.employeeNumber = lecturer.getEmployeeNumber();
            if (lecturer.getUser() != null) {
                response.lecturerName = lecturer.getUser().getName();
                response.lecturerEmail = lecturer.getUser().getEmail();
            }
        }
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public Long getLecturerId() {
        return lecturerId;
    }

    public String getLecturerName() {
        return lecturerName;
    }

    public String getLecturerEmail() {
        return lecturerEmail;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }
}
