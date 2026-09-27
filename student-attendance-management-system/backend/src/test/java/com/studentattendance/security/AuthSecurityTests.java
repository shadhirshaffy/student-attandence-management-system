package com.studentattendance.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentattendance.StudentAttendanceBackendApplication;
import com.studentattendance.entity.Lecturer;
import com.studentattendance.entity.Role;
import com.studentattendance.entity.Student;
import com.studentattendance.entity.User;
import com.studentattendance.repository.EnrollmentRepository;
import com.studentattendance.repository.LecturerRepository;
import com.studentattendance.repository.ModuleRepository;
import com.studentattendance.repository.SessionRepository;
import com.studentattendance.repository.StudentRepository;
import com.studentattendance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(classes = StudentAttendanceBackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private LecturerRepository lecturerRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        sessionRepository.deleteAll();
        moduleRepository.deleteAll();
        studentRepository.deleteAll();
        lecturerRepository.deleteAll();
        userRepository.deleteAll();
        createUser("admin@example.com", "Admin User", "Admin@12345", Role.ADMIN);
        createLecturer("lecturer@example.com", "Lecturer User", "Lecturer@12345", "LEC-TST-001");
        createStudent("student@example.com", "Student User", "Student@12345", "STU-TST-001");
    }

    @Test
    void validLoginReturnsJwtAndUserInformation() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@example.com","password":"Admin@12345"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email").value("admin@example.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void invalidPasswordReturnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@example.com","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointRejectsUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void roleRestrictedEndpointsAllowOnlyMatchingRole() throws Exception {
        String adminToken = login("admin@example.com", "Admin@12345");
        String lecturerToken = login("lecturer@example.com", "Lecturer@12345");
        String studentToken = login("student@example.com", "Student@12345");

        mockMvc.perform(get("/api/demo/admin").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/demo/admin").header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/demo/lecturer").header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/demo/lecturer").header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/demo/student").header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/demo/student").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminUserManagementRequiresAdminRole() throws Exception {
        String adminToken = login("admin@example.com", "Admin@12345");
        String lecturerToken = login("lecturer@example.com", "Lecturer@12345");
        String studentToken = login("student@example.com", "Student@12345");

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void adminCanCreateAndDeactivateStudentUser() throws Exception {
        String adminToken = login("admin@example.com", "Admin@12345");

        MvcResult createResult = mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Created Student",
                                  "email":"created.student@example.com",
                                  "password":"Created123",
                                  "role":"STUDENT",
                                  "studentNumber":"STU-NEW-001"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("created.student@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.studentNumber").value("STU-NEW-001"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andReturn();

        Long userId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"created.student@example.com","password":"Created123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("STUDENT"));

        mockMvc.perform(patch("/api/admin/users/{id}/status", userId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"active":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"created.student@example.com","password":"Created123"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCreateUserValidatesDuplicatesAndRoleProfileFields() throws Exception {
        String adminToken = login("admin@example.com", "Admin@12345");

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Duplicate Email",
                                  "email":"student@example.com",
                                  "password":"Valid123",
                                  "role":"STUDENT",
                                  "studentNumber":"STU-DUP-001"
                                }
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Invalid Role",
                                  "email":"invalid.role@example.com",
                                  "password":"Valid123",
                                  "role":"MANAGER"
                                }
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Missing Student Number",
                                  "email":"missing.student@example.com",
                                  "password":"Valid123",
                                  "role":"STUDENT"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void moduleManagementAndEnrollmentRespectRolesAndValidation() throws Exception {
        String adminToken = login("admin@example.com", "Admin@12345");
        String lecturerToken = login("lecturer@example.com", "Lecturer@12345");
        String studentToken = login("student@example.com", "Student@12345");
        Long lecturerId = lecturerRepository.findByEmployeeNumber("LEC-TST-001").orElseThrow().getId();
        Long studentId = studentRepository.findByStudentNumber("STU-TST-001").orElseThrow().getId();

        mockMvc.perform(get("/api/modules"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/modules").header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isForbidden());

        MvcResult moduleResult = mockMvc.perform(post("/api/modules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "moduleCode":"CS201",
                                  "moduleName":"Data Structures",
                                  "description":"Core data structures",
                                  "lecturerId":%d,
                                  "active":true
                                }
                                """.formatted(lecturerId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.moduleCode").value("CS201"))
                .andExpect(jsonPath("$.lecturerId").value(lecturerId))
                .andReturn();

        Long moduleId = objectMapper.readTree(moduleResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/modules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "moduleCode":"CS201",
                                  "moduleName":"Duplicate",
                                  "lecturerId":%d
                                }
                                """.formatted(lecturerId)))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/lecturers/me/modules").header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].moduleCode").value("CS201"));

        mockMvc.perform(get("/api/students/me/modules").header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        MvcResult enrollmentResult = mockMvc.perform(post("/api/enrollments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"studentId":%d,"moduleId":%d}
                                """.formatted(studentId, moduleId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.studentNumber").value("STU-TST-001"))
                .andExpect(jsonPath("$.moduleCode").value("CS201"))
                .andReturn();

        Long enrollmentId = objectMapper.readTree(enrollmentResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/enrollments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"studentId":%d,"moduleId":%d}
                                """.formatted(studentId, moduleId)))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/enrollments")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"studentId":%d,"moduleId":%d}
                                """.formatted(studentId, moduleId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/modules/{id}/students", moduleId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].enrollmentId").value(enrollmentId))
                .andExpect(jsonPath("$[0].studentNumber").value("STU-TST-001"));

        mockMvc.perform(get("/api/students/me/modules").header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].moduleCode").value("CS201"));

        mockMvc.perform(delete("/api/enrollments/{id}", enrollmentId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void lecturersCanManageOnlyTheirOwnAttendanceSessions() throws Exception {
        String adminToken = login("admin@example.com", "Admin@12345");
        String lecturerToken = login("lecturer@example.com", "Lecturer@12345");
        String studentToken = login("student@example.com", "Student@12345");
        createLecturer("other.lecturer@example.com", "Other Lecturer", "OtherLecturer123", "LEC-TST-002");
        String otherLecturerToken = login("other.lecturer@example.com", "OtherLecturer123");
        Long lecturerId = lecturerRepository.findByEmployeeNumber("LEC-TST-001").orElseThrow().getId();
        Long otherLecturerId = lecturerRepository.findByEmployeeNumber("LEC-TST-002").orElseThrow().getId();

        Long moduleId = createModule(adminToken, "CS301", "Software Architecture", lecturerId);
        Long otherModuleId = createModule(adminToken, "CS302", "Distributed Systems", otherLecturerId);

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"moduleId":%d,"sessionDate":"2026-10-01","startTime":"09:00","endTime":"10:00","room":"A1"}
                                """.formatted(moduleId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"moduleId":%d,"sessionDate":"2026-10-01","startTime":"09:00","endTime":"10:00","room":"A1"}
                                """.formatted(moduleId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + lecturerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"moduleId":%d,"sessionDate":"2026-10-01","startTime":"11:00","endTime":"10:00","room":"A1"}
                                """.formatted(moduleId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + lecturerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"moduleId":%d,"sessionDate":"2026-10-01","startTime":"09:00","endTime":"10:00","room":"A1"}
                                """.formatted(otherModuleId)))
                .andExpect(status().isForbidden());

        MvcResult sessionResult = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + lecturerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"moduleId":%d,"sessionDate":"2026-10-01","startTime":"09:00","endTime":"10:00","room":"A1"}
                                """.formatted(moduleId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.moduleCode").value("CS301"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.locked").value(false))
                .andReturn();

        Long sessionId = objectMapper.readTree(sessionResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/lecturers/me/sessions").header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(sessionId));

        mockMvc.perform(get("/api/modules/{id}/sessions", moduleId).header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].moduleCode").value("CS301"));

        mockMvc.perform(get("/api/modules/{id}/sessions", moduleId).header("Authorization", "Bearer " + otherLecturerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/sessions/{id}", sessionId).header("Authorization", "Bearer " + otherLecturerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/sessions/{id}/complete", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/sessions/{id}/activate", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.attendanceToken").isNotEmpty());

        mockMvc.perform(patch("/api/sessions/{id}/activate", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/sessions/{id}/lock", sessionId)
                        .header("Authorization", "Bearer " + otherLecturerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/sessions/{id}/lock", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LOCKED"))
                .andExpect(jsonPath("$.locked").value(true));

        mockMvc.perform(patch("/api/sessions/{id}/complete", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.locked").value(true));

        mockMvc.perform(patch("/api/sessions/{id}/lock", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void meReturnsCurrentAuthenticatedUser() throws Exception {
        String token = login("student@example.com", "Student@12345");

        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student@example.com"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("token").asText();
    }

    private Long createModule(String adminToken, String moduleCode, String moduleName, Long lecturerId) throws Exception {
        MvcResult moduleResult = mockMvc.perform(post("/api/modules")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "moduleCode":"%s",
                                  "moduleName":"%s",
                                  "description":"Test module",
                                  "lecturerId":%d,
                                  "active":true
                                }
                                """.formatted(moduleCode, moduleName, lecturerId)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(moduleResult.getResponse().getContentAsString()).get("id").asLong();
    }

    private User createUser(String email, String name, String password, Role role) {
        return userRepository.save(buildUser(email, name, password, role));
    }

    private User buildUser(String email, String name, String password, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private Lecturer createLecturer(String email, String name, String password, String employeeNumber) {
        User user = buildUser(email, name, password, Role.LECTURER);
        Lecturer lecturer = new Lecturer();
        lecturer.setUser(user);
        lecturer.setEmployeeNumber(employeeNumber);
        Lecturer savedLecturer = lecturerRepository.save(lecturer);
        user.setLecturer(savedLecturer);
        return savedLecturer;
    }

    private Student createStudent(String email, String name, String password, String studentNumber) {
        User user = buildUser(email, name, password, Role.STUDENT);
        Student student = new Student();
        student.setUser(user);
        student.setStudentNumber(studentNumber);
        Student savedStudent = studentRepository.save(student);
        user.setStudent(savedStudent);
        return savedStudent;
    }
}
