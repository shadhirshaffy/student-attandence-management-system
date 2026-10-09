package com.studentattendance.controller;

import com.studentattendance.dto.statistics.AdminAttendanceSummaryResponse;
import com.studentattendance.dto.statistics.LecturerAttendanceSummaryResponse;
import com.studentattendance.dto.statistics.LecturerModuleAttendanceSummary;
import com.studentattendance.dto.statistics.StudentAttendanceSummaryResponse;
import com.studentattendance.security.AuthenticatedUser;
import com.studentattendance.service.AttendanceStatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AttendanceStatisticsController {

    private final AttendanceStatisticsService attendanceStatisticsService;

    public AttendanceStatisticsController(AttendanceStatisticsService attendanceStatisticsService) {
        this.attendanceStatisticsService = attendanceStatisticsService;
    }

    @GetMapping("/students/me/attendance-summary")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentAttendanceSummaryResponse> studentSummary(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(attendanceStatisticsService.getStudentSummary(authenticatedUser.getUser().getId()));
    }

    @GetMapping("/lecturers/me/attendance-summary")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<LecturerAttendanceSummaryResponse> lecturerSummary(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(attendanceStatisticsService.getLecturerSummary(authenticatedUser.getUser().getId()));
    }

    @GetMapping("/lecturers/me/modules/{moduleId}/attendance-summary")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<LecturerModuleAttendanceSummary> lecturerModuleSummary(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long moduleId) {
        return ResponseEntity.ok(attendanceStatisticsService.getLecturerModuleSummary(
                authenticatedUser.getUser().getId(),
                moduleId));
    }

    @GetMapping("/admin/attendance-summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminAttendanceSummaryResponse> adminSummary() {
        return ResponseEntity.ok(attendanceStatisticsService.getAdminSummary());
    }
}
