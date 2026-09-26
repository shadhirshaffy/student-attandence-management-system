package com.studentattendance.repository;

import java.util.List;
import java.util.Optional;

import com.studentattendance.entity.Lecturer;
import com.studentattendance.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    Optional<Module> findByModuleCode(String moduleCode);

    boolean existsByModuleCode(String moduleCode);

    List<Module> findByLecturer(Lecturer lecturer);

    List<Module> findByLecturerId(Long lecturerId);
}
