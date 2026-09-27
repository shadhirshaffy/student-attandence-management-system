package com.studentattendance.config;

import com.studentattendance.entity.Admin;
import com.studentattendance.entity.Lecturer;
import com.studentattendance.entity.Role;
import com.studentattendance.entity.Student;
import com.studentattendance.entity.User;
import com.studentattendance.repository.AdminRepository;
import com.studentattendance.repository.LecturerRepository;
import com.studentattendance.repository.StudentRepository;
import com.studentattendance.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
public class DevelopmentDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevelopmentDataSeeder.class);

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final LecturerRepository lecturerRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public DevelopmentDataSeeder(
            UserRepository userRepository,
            AdminRepository adminRepository,
            LecturerRepository lecturerRepository,
            StudentRepository studentRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.lecturerRepository = lecturerRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        createAdmin();
        createLecturer();
        createStudent();
        log.info("Development seed accounts are ready: admin@example.com, lecturer@example.com, student@example.com");
    }

    private void createAdmin() {
        if (userRepository.existsByEmail("admin@example.com")) {
            return;
        }
        User user = buildUser("System Admin", "admin@example.com", "Admin@12345", Role.ADMIN);
        Admin admin = new Admin();
        admin.setUser(user);
        adminRepository.save(admin);
    }

    private void createLecturer() {
        if (userRepository.existsByEmail("lecturer@example.com")) {
            return;
        }
        User user = buildUser("Demo Lecturer", "lecturer@example.com", "Lecturer@12345", Role.LECTURER);
        Lecturer lecturer = new Lecturer();
        lecturer.setUser(user);
        lecturer.setEmployeeNumber("LEC-DEV-001");
        lecturerRepository.save(lecturer);
    }

    private void createStudent() {
        if (userRepository.existsByEmail("student@example.com")) {
            return;
        }
        User user = buildUser("Demo Student", "student@example.com", "Student@12345", Role.STUDENT);
        Student student = new Student();
        student.setUser(user);
        student.setStudentNumber("STU-DEV-001");
        studentRepository.save(student);
    }

    private User buildUser(String name, String email, String password, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        return user;
    }
}
