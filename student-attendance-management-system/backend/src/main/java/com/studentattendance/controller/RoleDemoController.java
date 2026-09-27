package com.studentattendance.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class RoleDemoController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> adminOnly() {
        return ResponseEntity.ok(Map.of("message", "Admin access granted"));
    }

    @GetMapping("/lecturer")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<Map<String, String>> lecturerOnly() {
        return ResponseEntity.ok(Map.of("message", "Lecturer access granted"));
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Map<String, String>> studentOnly() {
        return ResponseEntity.ok(Map.of("message", "Student access granted"));
    }
}
