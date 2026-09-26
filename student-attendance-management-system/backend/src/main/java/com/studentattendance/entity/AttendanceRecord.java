package com.studentattendance.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
        name = "attendance_records",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_attendance_records_session_student",
                columnNames = {"session_id", "student_id"}),
        indexes = {
                @Index(name = "idx_attendance_records_session_id", columnList = "session_id"),
                @Index(name = "idx_attendance_records_student_id", columnList = "student_id")
        })
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "marking_method", nullable = false, length = 20)
    private MarkingMethod markingMethod;

    @Column(name = "marked_at", nullable = false)
    private LocalDateTime markedAt;

    @PrePersist
    void onCreate() {
        if (markedAt == null) {
            markedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public MarkingMethod getMarkingMethod() {
        return markingMethod;
    }

    public void setMarkingMethod(MarkingMethod markingMethod) {
        this.markingMethod = markingMethod;
    }

    public LocalDateTime getMarkedAt() {
        return markedAt;
    }

    public void setMarkedAt(LocalDateTime markedAt) {
        this.markedAt = markedAt;
    }
}
