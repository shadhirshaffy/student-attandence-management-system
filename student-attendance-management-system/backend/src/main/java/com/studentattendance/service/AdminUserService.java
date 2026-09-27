package com.studentattendance.service;

import java.util.List;

import com.studentattendance.dto.admin.CreateUserRequest;
import com.studentattendance.dto.admin.UpdateUserRequest;
import com.studentattendance.dto.admin.UserResponse;
import com.studentattendance.entity.Admin;
import com.studentattendance.entity.Lecturer;
import com.studentattendance.entity.Role;
import com.studentattendance.entity.Student;
import com.studentattendance.entity.User;
import com.studentattendance.exception.BadRequestException;
import com.studentattendance.exception.ConflictException;
import com.studentattendance.exception.NotFoundException;
import com.studentattendance.repository.AdminRepository;
import com.studentattendance.repository.LecturerRepository;
import com.studentattendance.repository.StudentRepository;
import com.studentattendance.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            LecturerRepository lecturerRepository,
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.lecturerRepository = lecturerRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(user -> userRepository.findWithProfilesById(user.getId()).orElse(user))
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse getUser(Long id) {
        return UserResponse.from(findUserWithProfiles(id));
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        Role role = parseRole(request.getRole());
        validatePassword(request.getPassword());
        validateRequiredProfileFields(role, request.getStudentNumber(), request.getEmployeeNumber());

        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists.");
        }

        validateProfileNumberAvailability(role, request.getStudentNumber(), request.getEmployeeNumber(), null);

        User user = new User();
        user.setName(requireText(request.getName(), "Name is required."));
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActive(true);

        saveProfileForRole(user, role, request.getStudentNumber(), request.getEmployeeNumber());
        return UserResponse.from(findUserWithProfiles(user.getId()));
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUserWithProfiles(id);

        Role newRole = StringUtils.hasText(request.getRole()) ? parseRole(request.getRole()) : user.getRole();
        validateRequiredProfileFieldsForUpdate(user, newRole, request.getStudentNumber(), request.getEmployeeNumber());

        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName().trim());
        }
        if (StringUtils.hasText(request.getEmail())) {
            String email = normalizeEmail(request.getEmail());
            if (userRepository.existsByEmailAndIdNot(email, id)) {
                throw new ConflictException("Email already exists.");
            }
            user.setEmail(email);
        }
        if (StringUtils.hasText(request.getPassword())) {
            validatePassword(request.getPassword());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        if (newRole != user.getRole()) {
            removeExistingProfile(user);
            user.setRole(newRole);
            saveProfileForRole(user, newRole, request.getStudentNumber(), request.getEmployeeNumber());
        } else {
            updateExistingProfile(user, request.getStudentNumber(), request.getEmployeeNumber());
        }

        userRepository.save(user);
        return UserResponse.from(findUserWithProfiles(id));
    }

    @Transactional
    public UserResponse setActive(Long id, boolean active) {
        User user = findUserWithProfiles(id);
        user.setActive(active);
        userRepository.save(user);
        return UserResponse.from(findUserWithProfiles(id));
    }

    private void saveProfileForRole(User user, Role role, String studentNumber, String employeeNumber) {
        if (role == Role.STUDENT) {
            String normalized = requireText(studentNumber, "Student number is required for student users.");
            if (studentRepository.existsByStudentNumber(normalized)) {
                throw new ConflictException("Student number already exists.");
            }
            Student student = new Student();
            student.setUser(user);
            student.setStudentNumber(normalized);
            studentRepository.save(student);
            user.setStudent(student);
            return;
        }

        if (role == Role.LECTURER) {
            String normalized = requireText(employeeNumber, "Employee number is required for lecturer users.");
            if (lecturerRepository.existsByEmployeeNumber(normalized)) {
                throw new ConflictException("Employee number already exists.");
            }
            Lecturer lecturer = new Lecturer();
            lecturer.setUser(user);
            lecturer.setEmployeeNumber(normalized);
            lecturerRepository.save(lecturer);
            user.setLecturer(lecturer);
            return;
        }

        Admin admin = new Admin();
        admin.setUser(user);
        adminRepository.save(admin);
        user.setAdmin(admin);
    }

    private void updateExistingProfile(User user, String studentNumber, String employeeNumber) {
        if (user.getRole() == Role.STUDENT && StringUtils.hasText(studentNumber)) {
            Student student = studentRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new NotFoundException("Student profile not found."));
            String normalized = studentNumber.trim();
            if (studentRepository.existsByStudentNumberAndIdNot(normalized, student.getId())) {
                throw new ConflictException("Student number already exists.");
            }
            student.setStudentNumber(normalized);
            studentRepository.save(student);
        }

        if (user.getRole() == Role.LECTURER && StringUtils.hasText(employeeNumber)) {
            Lecturer lecturer = lecturerRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new NotFoundException("Lecturer profile not found."));
            String normalized = employeeNumber.trim();
            if (lecturerRepository.existsByEmployeeNumberAndIdNot(normalized, lecturer.getId())) {
                throw new ConflictException("Employee number already exists.");
            }
            lecturer.setEmployeeNumber(normalized);
            lecturerRepository.save(lecturer);
        }
    }

    private void removeExistingProfile(User user) {
        studentRepository.findByUserId(user.getId()).ifPresent(studentRepository::delete);
        lecturerRepository.findByUserId(user.getId()).ifPresent(lecturerRepository::delete);
        adminRepository.findByUserId(user.getId()).ifPresent(adminRepository::delete);
        user.setStudent(null);
        user.setLecturer(null);
        user.setAdmin(null);
    }

    private void validateProfileNumberAvailability(
            Role role,
            String studentNumber,
            String employeeNumber,
            Long ignoredProfileId) {
        if (role == Role.STUDENT && studentRepository.existsByStudentNumber(requireText(studentNumber,
                "Student number is required for student users."))) {
            throw new ConflictException("Student number already exists.");
        }
        if (role == Role.LECTURER && lecturerRepository.existsByEmployeeNumber(requireText(employeeNumber,
                "Employee number is required for lecturer users."))) {
            throw new ConflictException("Employee number already exists.");
        }
    }

    private void validateRequiredProfileFields(Role role, String studentNumber, String employeeNumber) {
        if (role == Role.STUDENT) {
            requireText(studentNumber, "Student number is required for student users.");
        }
        if (role == Role.LECTURER) {
            requireText(employeeNumber, "Employee number is required for lecturer users.");
        }
    }

    private void validateRequiredProfileFieldsForUpdate(
            User user,
            Role newRole,
            String studentNumber,
            String employeeNumber) {
        if (newRole == Role.STUDENT && user.getRole() != Role.STUDENT) {
            requireText(studentNumber, "Student number is required when changing a user to student.");
        }
        if (newRole == Role.LECTURER && user.getRole() != Role.LECTURER) {
            requireText(employeeNumber, "Employee number is required when changing a user to lecturer.");
        }
    }

    private Role parseRole(String role) {
        try {
            return Role.valueOf(requireText(role, "Role is required.").toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid role. Allowed values are ADMIN, LECTURER, STUDENT.");
        }
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password) || password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long.");
        }
        if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*\\d.*")) {
            throw new BadRequestException("Password must include uppercase, lowercase, and numeric characters.");
        }
    }

    private User findUserWithProfiles(Long id) {
        return userRepository.findWithProfilesById(id)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    private String normalizeEmail(String email) {
        return requireText(email, "Email is required.").toLowerCase();
    }

    private String requireText(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(message);
        }
        return value.trim();
    }
}
