package com.studentattendance.service;

import java.util.List;

import com.studentattendance.dto.enrollment.EnrolledStudentResponse;
import com.studentattendance.dto.enrollment.EnrollmentRequest;
import com.studentattendance.dto.enrollment.EnrollmentResponse;
import com.studentattendance.dto.module.ModuleResponse;
import com.studentattendance.entity.Enrollment;
import com.studentattendance.entity.Module;
import com.studentattendance.entity.Student;
import com.studentattendance.exception.BadRequestException;
import com.studentattendance.exception.ConflictException;
import com.studentattendance.exception.NotFoundException;
import com.studentattendance.repository.EnrollmentRepository;
import com.studentattendance.repository.ModuleRepository;
import com.studentattendance.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final ModuleRepository moduleRepository;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            StudentRepository studentRepository,
            ModuleRepository moduleRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.moduleRepository = moduleRepository;
    }

    @Transactional
    public EnrollmentResponse enroll(EnrollmentRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new NotFoundException("Student not found."));
        Module module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new NotFoundException("Module not found."));

        if (!module.isActive()) {
            throw new BadRequestException("Cannot enroll students into an inactive module.");
        }
        if (enrollmentRepository.existsByStudentIdAndModuleId(student.getId(), module.getId())) {
            throw new ConflictException("Student is already enrolled in this module.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setModule(module);
        return EnrollmentResponse.from(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public EnrollmentResponse enrollSelf(Long userId, Long moduleId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile not found."));
        EnrollmentRequest request = new EnrollmentRequest();
        request.setStudentId(student.getId());
        request.setModuleId(moduleId);
        return enroll(request);
    }

    public List<ModuleResponse> listAvailableModules(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile not found."));
        java.util.Set<Long> enrolledIds = enrollmentRepository.findByStudentId(student.getId()).stream()
                .map(enrollment -> enrollment.getModule().getId())
                .collect(java.util.stream.Collectors.toSet());
        return moduleRepository.findByActiveTrue().stream()
                .filter(module -> !enrolledIds.contains(module.getId()))
                .map(ModuleResponse::from).toList();
    }

    @Transactional
    public void removeEnrollment(Long id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new NotFoundException("Enrollment not found.");
        }
        enrollmentRepository.deleteById(id);
    }

    public List<EnrolledStudentResponse> listStudentsInModule(Long moduleId) {
        moduleRepository.findById(moduleId).orElseThrow(() -> new NotFoundException("Module not found."));
        return enrollmentRepository.findByModuleId(moduleId).stream()
                .map(EnrolledStudentResponse::from)
                .toList();
    }

    public List<ModuleResponse> listModulesForStudent(Long studentId) {
        studentRepository.findById(studentId).orElseThrow(() -> new NotFoundException("Student not found."));
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(enrollment -> ModuleResponse.from(enrollment.getModule()))
                .toList();
    }

    public List<ModuleResponse> listModulesForStudentUser(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Student profile not found."));
        return listModulesForStudent(student.getId());
    }
}
