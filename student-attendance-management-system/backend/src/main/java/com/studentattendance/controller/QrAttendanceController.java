package com.studentattendance.controller;

import com.studentattendance.dto.attendance.QrAttendanceRequest;
import com.studentattendance.dto.attendance.QrAttendanceResponse;
import com.studentattendance.dto.session.SessionQrResponse;
import com.studentattendance.security.AuthenticatedUser;
import com.studentattendance.service.QrAttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class QrAttendanceController {

    private final QrAttendanceService qrAttendanceService;

    public QrAttendanceController(QrAttendanceService qrAttendanceService) {
        this.qrAttendanceService = qrAttendanceService;
    }

    @GetMapping("/sessions/{sessionId}/qr")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<SessionQrResponse> getSessionQr(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(qrAttendanceService.getSessionQr(
                authenticatedUser.getUser().getId(),
                sessionId));
    }

    @PostMapping("/attendance/qr")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<QrAttendanceResponse> markQrAttendance(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody QrAttendanceRequest request) {
        return ResponseEntity.ok(qrAttendanceService.markAttendance(
                authenticatedUser.getUser().getId(),
                request));
    }
}
