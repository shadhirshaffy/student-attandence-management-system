package com.studentattendance.controller;

import java.util.List;

import com.studentattendance.dto.session.SessionRequest;
import com.studentattendance.dto.session.SessionResponse;
import com.studentattendance.security.AuthenticatedUser;
import com.studentattendance.service.SessionService;
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
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/sessions")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<SessionResponse> createSession(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @Valid @RequestBody SessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.createSession(authenticatedUser.getUser().getId(), request));
    }

    @GetMapping("/lecturers/me/sessions")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<List<SessionResponse>> mySessions(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(sessionService.listLecturerSessions(authenticatedUser.getUser().getId()));
    }

    @GetMapping("/modules/{moduleId}/sessions")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<List<SessionResponse>> moduleSessions(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long moduleId) {
        return ResponseEntity.ok(sessionService.listModuleSessions(authenticatedUser.getUser().getId(), moduleId));
    }

    @GetMapping("/sessions/{id}")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<SessionResponse> getSession(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(sessionService.getSession(authenticatedUser.getUser().getId(), id));
    }

    @PatchMapping("/sessions/{id}/activate")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<SessionResponse> activateSession(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(sessionService.activateSession(authenticatedUser.getUser().getId(), id));
    }

    @PatchMapping("/sessions/{id}/lock")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<SessionResponse> lockSession(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(sessionService.lockSession(authenticatedUser.getUser().getId(), id));
    }

    @PatchMapping("/sessions/{id}/complete")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<SessionResponse> completeSession(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(sessionService.completeSession(authenticatedUser.getUser().getId(), id));
    }
}
