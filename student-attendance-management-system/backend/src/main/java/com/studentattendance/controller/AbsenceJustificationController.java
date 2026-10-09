package com.studentattendance.controller;

import java.util.List;

import com.studentattendance.dto.justification.AbsenceJustificationRequest;
import com.studentattendance.dto.justification.AbsenceJustificationResponse;
import com.studentattendance.dto.justification.AbsenceJustificationReviewRequest;
import com.studentattendance.security.AuthenticatedUser;
import com.studentattendance.service.AbsenceJustificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AbsenceJustificationController {

    private final AbsenceJustificationService absenceJustificationService;

    public AbsenceJustificationController(AbsenceJustificationService absenceJustificationService) {
        this.absenceJustificationService = absenceJustificationService;
    }

    @GetMapping("/students/me/absence-justifications")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<AbsenceJustificationResponse>> listMine(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(absenceJustificationService.listMine(authenticatedUser.getUser().getId()));
    }

    @PostMapping("/students/me/absence-justifications")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<AbsenceJustificationResponse> submit(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody AbsenceJustificationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(absenceJustificationService.submit(authenticatedUser.getUser().getId(), request));
    }

    @GetMapping("/lecturers/me/absence-justifications")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<List<AbsenceJustificationResponse>> listForLecturer(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(absenceJustificationService.listForLecturer(authenticatedUser.getUser().getId()));
    }

    @PatchMapping("/lecturers/me/absence-justifications/{id}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<AbsenceJustificationResponse> review(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id,
            @Valid @RequestBody AbsenceJustificationReviewRequest request) {
        return ResponseEntity.ok(absenceJustificationService.review(authenticatedUser.getUser().getId(), id, request));
    }
}
