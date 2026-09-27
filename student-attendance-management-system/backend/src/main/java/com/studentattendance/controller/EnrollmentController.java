package com.studentattendance.controller;

import java.util.List;

import com.studentattendance.dto.enrollment.EnrolledStudentResponse;
import com.studentattendance.dto.enrollment.EnrollmentRequest;
import com.studentattendance.dto.enrollment.EnrollmentResponse;
import com.studentattendance.dto.module.ModuleResponse;
import com.studentattendance.security.AuthenticatedUser;
import com.studentattendance.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/enrollments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentResponse> enroll(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enroll(request));
    }

    @DeleteMapping("/enrollments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeEnrollment(@PathVariable Long id) {
        enrollmentService.removeEnrollment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/modules/{id}/students")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrolledStudentResponse>> listStudentsInModule(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.listStudentsInModule(id));
    }

    @GetMapping("/students/{id}/modules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ModuleResponse>> listStudentModules(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.listModulesForStudent(id));
    }

    @GetMapping("/students/me/modules")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ModuleResponse>> myStudentModules(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(enrollmentService.listModulesForStudentUser(authenticatedUser.getUser().getId()));
    }
}
