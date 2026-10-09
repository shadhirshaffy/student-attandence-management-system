package com.studentattendance.controller;

import java.time.LocalDate;

import com.studentattendance.dto.report.AttendanceReportResponse;
import com.studentattendance.entity.Role;
import com.studentattendance.security.AuthenticatedUser;
import com.studentattendance.service.AttendanceReportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports/attendance")
public class AttendanceReportController {

    private final AttendanceReportService attendanceReportService;

    public AttendanceReportController(AttendanceReportService attendanceReportService) {
        this.attendanceReportService = attendanceReportService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','LECTURER')")
    public ResponseEntity<AttendanceReportResponse> report(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        return ResponseEntity.ok(generate(authenticatedUser, moduleId, studentId, sessionId, dateFrom, dateTo));
    }

    @GetMapping("/csv")
    @PreAuthorize("hasAnyRole('ADMIN','LECTURER')")
    public ResponseEntity<byte[]> csv(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        AttendanceReportResponse report = generate(authenticatedUser, moduleId, studentId, sessionId, dateFrom, dateTo);
        return download(
                attendanceReportService.exportCsv(report),
                "text/csv",
                attendanceReportService.filename("csv", moduleId, studentId, sessionId));
    }

    @GetMapping("/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','LECTURER')")
    public ResponseEntity<byte[]> pdf(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        AttendanceReportResponse report = generate(authenticatedUser, moduleId, studentId, sessionId, dateFrom, dateTo);
        return download(
                attendanceReportService.exportPdf(report),
                MediaType.APPLICATION_PDF_VALUE,
                attendanceReportService.filename("pdf", moduleId, studentId, sessionId));
    }

    private AttendanceReportResponse generate(
            AuthenticatedUser authenticatedUser,
            Long moduleId,
            Long studentId,
            Long sessionId,
            LocalDate dateFrom,
            LocalDate dateTo) {
        Role role = authenticatedUser.getUser().getRole();
        return attendanceReportService.generateReport(
                role,
                authenticatedUser.getUser().getId(),
                moduleId,
                studentId,
                sessionId,
                dateFrom,
                dateTo);
    }

    private ResponseEntity<byte[]> download(byte[] body, String contentType, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString())
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(body);
    }
}
