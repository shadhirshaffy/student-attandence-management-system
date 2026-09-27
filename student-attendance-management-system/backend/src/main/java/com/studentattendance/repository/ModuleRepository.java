package com.studentattendance.repository;

import java.util.List;
import java.util.Optional;

import com.studentattendance.entity.Lecturer;
import com.studentattendance.entity.Module;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModuleRepository extends JpaRepository<Module, Long> {

    @Override
    @EntityGraph(attributePaths = {"lecturer", "lecturer.user"})
    List<Module> findAll();

    @Override
    @EntityGraph(attributePaths = {"lecturer", "lecturer.user"})
    Optional<Module> findById(Long id);

    Optional<Module> findByModuleCode(String moduleCode);

    boolean existsByModuleCode(String moduleCode);

    boolean existsByModuleCodeAndIdNot(String moduleCode, Long id);

    List<Module> findByLecturer(Lecturer lecturer);

    @EntityGraph(attributePaths = {"lecturer", "lecturer.user"})
    List<Module> findByLecturerId(Long lecturerId);
}
