package com.studentattendance.service;

import java.util.List;

import com.studentattendance.dto.module.ModuleRequest;
import com.studentattendance.dto.module.ModuleResponse;
import com.studentattendance.entity.Lecturer;
import com.studentattendance.entity.Module;
import com.studentattendance.exception.ConflictException;
import com.studentattendance.exception.NotFoundException;
import com.studentattendance.repository.LecturerRepository;
import com.studentattendance.repository.EnrollmentRepository;
import com.studentattendance.repository.SessionRepository;
import com.studentattendance.repository.ModuleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final LecturerRepository lecturerRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SessionRepository sessionRepository;

    public ModuleService(ModuleRepository moduleRepository, LecturerRepository lecturerRepository,
            EnrollmentRepository enrollmentRepository,
            SessionRepository sessionRepository) {
        this.moduleRepository = moduleRepository;
        this.lecturerRepository = lecturerRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.sessionRepository = sessionRepository;
    }

    public List<ModuleResponse> listModules() {
        return moduleRepository.findAll().stream()
                .map(ModuleResponse::from)
                .toList();
    }

    public ModuleResponse getModule(Long id) {
        return ModuleResponse.from(findModule(id));
    }

    @Transactional
    public ModuleResponse createModule(ModuleRequest request) {
        String moduleCode = request.getModuleCode().trim().toUpperCase();
        if (moduleRepository.existsByModuleCode(moduleCode)) {
            throw new ConflictException("Module code already exists.");
        }

        Module module = new Module();
        applyRequest(module, request);
        module.setModuleCode(moduleCode);
        module.setActive(request.getActive() == null || request.getActive());
        return ModuleResponse.from(moduleRepository.save(module));
    }

    @Transactional
    public ModuleResponse updateModule(Long id, ModuleRequest request) {
        Module module = findModule(id);
        String moduleCode = request.getModuleCode().trim().toUpperCase();
        if (moduleRepository.existsByModuleCodeAndIdNot(moduleCode, id)) {
            throw new ConflictException("Module code already exists.");
        }

        applyRequest(module, request);
        module.setModuleCode(moduleCode);
        if (request.getActive() != null) {
            module.setActive(request.getActive());
        }
        return ModuleResponse.from(moduleRepository.save(module));
    }

    @Transactional
    public ModuleResponse setActive(Long id, boolean active) {
        Module module = findModule(id);
        module.setActive(active);
        return ModuleResponse.from(moduleRepository.save(module));
    }

    @Transactional
    public void deleteModule(Long id) {
        Module module = findModule(id);
        if (enrollmentRepository.existsByModuleId(id) || sessionRepository.existsByModuleId(id)) {
            throw new ConflictException("Cannot delete a module with enrollments or sessions. Deactivate it instead.");
        }
        moduleRepository.delete(module);
        // Flush inside the transaction so foreign keys also protect concurrent references.
        moduleRepository.flush();
    }

    public List<ModuleResponse> listLecturerModulesByUserId(Long userId) {
        Lecturer lecturer = lecturerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Lecturer profile not found."));
        return moduleRepository.findByLecturerId(lecturer.getId()).stream()
                .map(ModuleResponse::from)
                .toList();
    }

    private void applyRequest(Module module, ModuleRequest request) {
        Lecturer lecturer = lecturerRepository.findById(request.getLecturerId())
                .orElseThrow(() -> new NotFoundException("Lecturer not found."));
        module.setModuleName(request.getModuleName().trim());
        module.setDescription(request.getDescription());
        module.setLecturer(lecturer);
    }

    private Module findModule(Long id) {
        return moduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Module not found."));
    }
}
