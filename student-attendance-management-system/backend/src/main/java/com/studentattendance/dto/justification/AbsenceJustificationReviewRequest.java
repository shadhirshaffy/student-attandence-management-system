package com.studentattendance.dto.justification;

import com.studentattendance.entity.JustificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AbsenceJustificationReviewRequest {

    @NotNull
    private JustificationStatus status;

    @Size(max = 1000)
    private String reviewNote;

    public JustificationStatus getStatus() {
        return status;
    }

    public void setStatus(JustificationStatus status) {
        this.status = status;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }
}
