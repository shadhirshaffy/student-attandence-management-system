package com.studentattendance.controller;

import com.studentattendance.dto.attendance.ManualAttendanceRequest;
import com.studentattendance.dto.attendance.ManualAttendanceResponse;
import com.studentattendance.security.AuthenticatedUser;
import com.studentattendance.service.ManualAttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sessions/{sessionId}/attendance/manual")
public class ManualAttendanceController {

    private final ManualAttendanceService manualAttendanceService;

    public ManualAttendanceController(ManualAttendanceService manualAttendanceService) {
        this.manualAttendanceService = manualAttendanceService;
    }

    @GetMapping
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ManualAttendanceResponse> getManualAttendance(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(manualAttendanceService.getManualAttendance(
                authenticatedUser.getUser().getId(),
                sessionId));
    }

    @PutMapping
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<ManualAttendanceResponse> saveManualAttendance(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long sessionId,
            @Valid @RequestBody ManualAttendanceRequest request) {
        return ResponseEntity.ok(manualAttendanceService.saveManualAttendance(
                authenticatedUser.getUser().getId(),
                sessionId,
                request));
    }
}
