package com.studentattendance.controller;

import java.util.List;

import com.studentattendance.dto.module.ModuleRequest;
import com.studentattendance.dto.module.ModuleResponse;
import com.studentattendance.security.AuthenticatedUser;
import com.studentattendance.service.ModuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping("/modules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ModuleResponse>> listModules() {
        return ResponseEntity.ok(moduleService.listModules());
    }

    @PostMapping("/modules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleResponse> createModule(@Valid @RequestBody ModuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(moduleService.createModule(request));
    }

    @GetMapping("/modules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleResponse> getModule(@PathVariable Long id) {
        return ResponseEntity.ok(moduleService.getModule(id));
    }

    @PutMapping("/modules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleResponse> updateModule(
            @PathVariable Long id,
            @Valid @RequestBody ModuleRequest request) {
        return ResponseEntity.ok(moduleService.updateModule(id, request));
    }

    @PatchMapping("/modules/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ModuleResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, Boolean> request) {
        return ResponseEntity.ok(moduleService.setActive(id, Boolean.TRUE.equals(request.get("active"))));
    }

    @GetMapping("/lecturers/me/modules")
    @PreAuthorize("hasRole('LECTURER')")
    public ResponseEntity<List<ModuleResponse>> myLecturerModules(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        return ResponseEntity.ok(moduleService.listLecturerModulesByUserId(authenticatedUser.getUser().getId()));
    }
}
