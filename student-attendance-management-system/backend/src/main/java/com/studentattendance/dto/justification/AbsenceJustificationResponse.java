package com.studentattendance.dto.justification;

import java.time.LocalDateTime;

import com.studentattendance.entity.AbsenceJustification;
import com.studentattendance.entity.JustificationStatus;

public record AbsenceJustificationResponse(
        Long id,
        Long sessionId,
        Long moduleId,
        String moduleCode,
        String moduleName,
        Long studentId,
        String studentNumber,
        String studentName,
        String reason,
        JustificationStatus status,
        String reviewNote,
        LocalDateTime submittedAt,
        LocalDateTime reviewedAt,
        String reviewedByName) {

    public static AbsenceJustificationResponse from(AbsenceJustification justification) {
        return new AbsenceJustificationResponse(
                justification.getId(),
                justification.getSession().getId(),
                justification.getSession().getModule().getId(),
                justification.getSession().getModule().getModuleCode(),
                justification.getSession().getModule().getModuleName(),
                justification.getStudent().getId(),
                justification.getStudent().getStudentNumber(),
                justification.getStudent().getUser().getName(),
                justification.getReason(),
                justification.getStatus(),
                justification.getReviewNote(),
                justification.getSubmittedAt(),
                justification.getReviewedAt(),
                justification.getReviewedBy() == null ? null : justification.getReviewedBy().getName());
    }
}
