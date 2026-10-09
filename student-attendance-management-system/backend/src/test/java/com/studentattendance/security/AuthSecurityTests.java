package com.studentattendance.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentattendance.StudentAttendanceBackendApplication;
import com.studentattendance.entity.Lecturer;
import com.studentattendance.entity.Role;
import com.studentattendance.entity.Session;
import com.studentattendance.entity.Student;
import com.studentattendance.entity.User;
import com.studentattendance.repository.AttendanceRecordRepository;
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
    private AttendanceRecordRepository attendanceRecordRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        attendanceRecordRepository.deleteAll();
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
                                """.replace("2026-10-02", java.time.LocalDate.now().plusDays(1).toString()).formatted(moduleId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"moduleId":%d,"sessionDate":"2026-10-01","startTime":"09:00","endTime":"10:00","room":"A1"}
                                """.replace("2026-10-02", java.time.LocalDate.now().plusDays(1).toString()).formatted(moduleId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + lecturerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"moduleId":%d,"sessionDate":"2026-10-01","startTime":"11:00","endTime":"10:00","room":"A1"}
                                """.replace("2026-10-02", java.time.LocalDate.now().plusDays(1).toString()).formatted(moduleId)))
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
                                """.replace("2026-10-02", java.time.LocalDate.now().plusDays(1).toString()).formatted(moduleId)))
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
    void manualAttendanceRequiresOwnedEditableSessionAndEnrolledStudents() throws Exception {
        String adminToken = login("admin@example.com", "Admin@12345");
        String lecturerToken = login("lecturer@example.com", "Lecturer@12345");
        String studentToken = login("student@example.com", "Student@12345");
        createLecturer("other.lecturer@example.com", "Other Lecturer", "OtherLecturer123", "LEC-TST-002");
        createStudent("second.student@example.com", "Second Student", "Second123", "STU-TST-002");
        createStudent("outside.student@example.com", "Outside Student", "Outside123", "STU-TST-003");
        String otherLecturerToken = login("other.lecturer@example.com", "OtherLecturer123");
        Long lecturerId = lecturerRepository.findByEmployeeNumber("LEC-TST-001").orElseThrow().getId();
        Long otherLecturerId = lecturerRepository.findByEmployeeNumber("LEC-TST-002").orElseThrow().getId();
        Long firstStudentId = studentRepository.findByStudentNumber("STU-TST-001").orElseThrow().getId();
        Long secondStudentId = studentRepository.findByStudentNumber("STU-TST-002").orElseThrow().getId();
        Long outsideStudentId = studentRepository.findByStudentNumber("STU-TST-003").orElseThrow().getId();

        Long moduleId = createModule(adminToken, "CS401", "Enterprise Applications", lecturerId);
        Long otherModuleId = createModule(adminToken, "CS402", "Cloud Systems", otherLecturerId);
        enrollStudent(adminToken, firstStudentId, moduleId);
        enrollStudent(adminToken, secondStudentId, moduleId);

        Long sessionId = createSession(lecturerToken, moduleId);
        Long otherSessionId = createSession(otherLecturerToken, otherModuleId);

        mockMvc.perform(get("/api/sessions/{id}/attendance/manual", sessionId)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/sessions/{id}/attendance/manual", sessionId)
                        .header("Authorization", "Bearer " + otherLecturerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/sessions/{id}/attendance/manual", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.editable").value(false))
                .andExpect(jsonPath("$.students.length()").value(2))
                .andExpect(jsonPath("$.students[0].status").doesNotExist());

        mockMvc.perform(put("/api/sessions/{id}/attendance/manual", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"records":[{"studentId":%d,"status":"PRESENT"}]}
                                """.formatted(firstStudentId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/sessions/{id}/activate", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/sessions/{id}/attendance/manual", sessionId)
                        .header("Authorization", "Bearer " + otherLecturerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"records":[{"studentId":%d,"status":"PRESENT"}]}
                                """.formatted(firstStudentId)))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/sessions/{id}/attendance/manual", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"records":[{"studentId":%d,"status":"PRESENT"}]}
                                """.formatted(outsideStudentId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/sessions/{id}/attendance/manual", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"records":[{"studentId":%d,"status":"PRESENT"},{"studentId":%d,"status":"ABSENT"}]}
                                """.formatted(firstStudentId, firstStudentId)))
                .andExpect(status().isBadRequest());

        MvcResult saveResult = mockMvc.perform(put("/api/sessions/{id}/attendance/manual", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "records":[
                                    {"studentId":%d,"status":"PRESENT"},
                                    {"studentId":%d,"status":"ABSENT"}
                                  ]
                                }
                                """.formatted(firstStudentId, secondStudentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.editable").value(true))
                .andExpect(jsonPath("$.students[0].markingMethod").value("MANUAL"))
                .andExpect(jsonPath("$.students[0].attendanceRecordId").isNumber())
                .andExpect(jsonPath("$.students[0].status").value("PRESENT"))
                .andExpect(jsonPath("$.students[1].status").value("ABSENT"))
                .andReturn();

        Long firstRecordId = objectMapper.readTree(saveResult.getResponse().getContentAsString())
                .get("students").get(0).get("attendanceRecordId").asLong();

        mockMvc.perform(put("/api/sessions/{id}/attendance/manual", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"records":[{"studentId":%d,"status":"ABSENT"}]}
                                """.formatted(firstStudentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.students[0].attendanceRecordId").value(firstRecordId))
                .andExpect(jsonPath("$.students[0].status").value("ABSENT"));

        mockMvc.perform(get("/api/sessions/{id}/attendance/manual", otherSessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/sessions/{id}/lock", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/sessions/{id}/attendance/manual", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"records":[{"studentId":%d,"status":"PRESENT"}]}
                                """.formatted(firstStudentId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void qrAttendanceUsesSecureTokenAndAuthenticatedStudent() throws Exception {
        String adminToken = login("admin@example.com", "Admin@12345");
        String lecturerToken = login("lecturer@example.com", "Lecturer@12345");
        String studentToken = login("student@example.com", "Student@12345");
        createStudent("outside.student@example.com", "Outside Student", "Outside123", "STU-TST-003");
        String outsideStudentToken = login("outside.student@example.com", "Outside123");
        Long lecturerId = lecturerRepository.findByEmployeeNumber("LEC-TST-001").orElseThrow().getId();
        Long studentId = studentRepository.findByStudentNumber("STU-TST-001").orElseThrow().getId();

        Long moduleId = createModule(adminToken, "CS501", "Secure Attendance", lecturerId);
        enrollStudent(adminToken, studentId, moduleId);

        Long sessionId = createSession(lecturerToken, moduleId);
        mockMvc.perform(get("/api/sessions/{id}/qr", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isBadRequest());

        MvcResult activatedResult = mockMvc.perform(patch("/api/sessions/{id}/activate", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendanceToken").isNotEmpty())
                .andReturn();

        String token = objectMapper.readTree(activatedResult.getResponse().getContentAsString())
                .get("attendanceToken").asText();
        org.assertj.core.api.Assertions.assertThat(token).hasSizeGreaterThanOrEqualTo(43);
        org.assertj.core.api.Assertions.assertThat(token)
                .doesNotMatch("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");

        mockMvc.perform(get("/api/sessions/{id}/qr", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(token))
                .andExpect(jsonPath("$.attendanceUrl").value(org.hamcrest.Matchers.containsString("/student/scan?token=")));

        mockMvc.perform(post("/api/attendance/qr")
                        .header("Authorization", "Bearer " + lecturerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/attendance/qr")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"invalid-token"}
                                """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/attendance/qr")
                        .header("Authorization", "Bearer " + outsideStudentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/attendance/qr")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s","studentId":99999}
                                """.formatted(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PRESENT"))
                .andExpect(jsonPath("$.markingMethod").value("QR"))
                .andExpect(jsonPath("$.moduleCode").value("CS501"));

        mockMvc.perform(post("/api/attendance/qr")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isConflict());

        Long expiredSessionId = createSession(lecturerToken, moduleId);
        String expiredToken = activateSessionAndReturnToken(lecturerToken, expiredSessionId);
        Session expiredSession = sessionRepository.findById(expiredSessionId).orElseThrow();
        expiredSession.setTokenExpiry(java.time.LocalDateTime.now().minusMinutes(1));
        sessionRepository.save(expiredSession);

        mockMvc.perform(post("/api/attendance/qr")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(expiredToken)))
                .andExpect(status().isBadRequest());

        Long lockedSessionId = createSession(lecturerToken, moduleId);
        String lockedToken = activateSessionAndReturnToken(lecturerToken, lockedSessionId);
        mockMvc.perform(patch("/api/sessions/{id}/lock", lockedSessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/attendance/qr")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(lockedToken)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void attendanceStatisticsUseBackendCompletedSessionCalculations() throws Exception {
        String adminToken = login("admin@example.com", "Admin@12345");
        String lecturerToken = login("lecturer@example.com", "Lecturer@12345");
        String studentToken = login("student@example.com", "Student@12345");
        Long lecturerId = lecturerRepository.findByEmployeeNumber("LEC-TST-001").orElseThrow().getId();
        Long studentId = studentRepository.findByStudentNumber("STU-TST-001").orElseThrow().getId();

        Long moduleId = createModule(adminToken, "CS601", "Attendance Analytics", lecturerId);
        enrollStudent(adminToken, studentId, moduleId);

        Long presentSessionId = createSession(lecturerToken, moduleId);
        String presentToken = activateSessionAndReturnToken(lecturerToken, presentSessionId);
        mockMvc.perform(post("/api/attendance/qr")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(presentToken)))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/sessions/{id}/complete", presentSessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk());

        Long absentSessionId = createSession(lecturerToken, moduleId);
        activateSessionAndReturnToken(lecturerToken, absentSessionId);
        mockMvc.perform(patch("/api/sessions/{id}/complete", absentSessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/students/me/attendance-summary")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lowAttendanceThreshold").value(80))
                .andExpect(jsonPath("$.enrolledModules").value(1))
                .andExpect(jsonPath("$.lowAttendanceModules").value(1))
                .andExpect(jsonPath("$.eligibleCompletedSessions").value(2))
                .andExpect(jsonPath("$.presentSessions").value(1))
                .andExpect(jsonPath("$.absentSessions").value(1))
                .andExpect(jsonPath("$.overallAttendancePercentage").value(50.00))
                .andExpect(jsonPath("$.overallLowAttendance").value(true))
                .andExpect(jsonPath("$.modules[0].moduleCode").value("CS601"))
                .andExpect(jsonPath("$.modules[0].attendancePercentage").value(50.00))
                .andExpect(jsonPath("$.modules[0].lowAttendance").value(true))
                .andExpect(jsonPath("$.history.length()").value(2));

        mockMvc.perform(get("/api/lecturers/me/attendance-summary")
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedModules").value(1))
                .andExpect(jsonPath("$.completedSessions").value(2))
                .andExpect(jsonPath("$.averageAttendancePercentage").value(50.00))
                .andExpect(jsonPath("$.lowAttendanceStudents").value(1))
                .andExpect(jsonPath("$.modules[0].students[0].attendancePercentage").value(50.00));

        mockMvc.perform(get("/api/lecturers/me/modules/{id}/attendance-summary", moduleId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moduleCode").value("CS601"))
                .andExpect(jsonPath("$.completedSessions").value(2))
                .andExpect(jsonPath("$.students[0].presentSessions").value(1))
                .andExpect(jsonPath("$.students[0].absentSessions").value(1));

        mockMvc.perform(get("/api/admin/attendance-summary")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedSessions").value(2))
                .andExpect(jsonPath("$.attendanceRecords").value(1))
                .andExpect(jsonPath("$.averageAttendancePercentage").value(50.00))
                .andExpect(jsonPath("$.lowAttendanceStudentModules").value(1));

        mockMvc.perform(get("/api/admin/attendance-summary")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void attendanceReportsExportCsvAndPdfWithAuthorization() throws Exception {
        String adminToken = login("admin@example.com", "Admin@12345");
        String lecturerToken = login("lecturer@example.com", "Lecturer@12345");
        String studentToken = login("student@example.com", "Student@12345");
        createLecturer("other.lecturer@example.com", "Other Lecturer", "OtherLecturer123", "LEC-TST-002");
        String otherLecturerToken = login("other.lecturer@example.com", "OtherLecturer123");
        Long lecturerId = lecturerRepository.findByEmployeeNumber("LEC-TST-001").orElseThrow().getId();
        Long otherLecturerId = lecturerRepository.findByEmployeeNumber("LEC-TST-002").orElseThrow().getId();
        Long studentId = studentRepository.findByStudentNumber("STU-TST-001").orElseThrow().getId();

        Long moduleId = createModule(adminToken, "CS701", "Reporting", lecturerId);
        Long otherModuleId = createModule(adminToken, "CS702", "Private Reporting", otherLecturerId);
        enrollStudent(adminToken, studentId, moduleId);

        Long sessionId = createSession(lecturerToken, moduleId);
        String token = activateSessionAndReturnToken(lecturerToken, sessionId);
        mockMvc.perform(post("/api/attendance/qr")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"token":"%s"}
                                """.formatted(token)))
                .andExpect(status().isOk());
        mockMvc.perform(patch("/api/sessions/{id}/complete", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/reports/attendance")
                        .header("Authorization", "Bearer " + lecturerToken)
                        .param("moduleId", moduleId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rowCount").value(1))
                .andExpect(jsonPath("$.rows[0].studentNumber").value("STU-TST-001"))
                .andExpect(jsonPath("$.rows[0].moduleCode").value("CS701"))
                .andExpect(jsonPath("$.rows[0].sessionsHeld").value(1))
                .andExpect(jsonPath("$.rows[0].sessionsPresent").value(1))
                .andExpect(jsonPath("$.rows[0].attendancePercentage").value(100.00));

        mockMvc.perform(get("/api/reports/attendance")
                        .header("Authorization", "Bearer " + lecturerToken)
                        .param("moduleId", otherModuleId.toString()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/reports/attendance")
                        .header("Authorization", "Bearer " + otherLecturerToken)
                        .param("sessionId", sessionId.toString()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/reports/attendance")
                        .header("Authorization", "Bearer " + studentToken)
                        .param("moduleId", moduleId.toString()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/reports/attendance/csv")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("moduleId", moduleId.toString()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attendance-report-module-" + moduleId + ".csv")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Student Number,Student Name,Module Code")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("STU-TST-001")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("CS701")));

        mockMvc.perform(get("/api/reports/attendance/pdf")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("sessionId", sessionId.toString()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("attendance-report-session-" + sessionId + ".pdf")))
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getContentAsByteArray())
                        .startsWith("%PDF".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
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

    @Test
    void disabledAndDeletedAccountsCannotReuseAnIssuedToken() throws Exception {
        String token = login("student@example.com", "Student@12345");
        User user = userRepository.findByEmail("student@example.com").orElseThrow();
        user.setActive(false);
        userRepository.save(user);
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        studentRepository.deleteAll();
        userRepository.deleteById(user.getId());
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void expiredAndTamperedJwtAreRejected() throws Exception {
        User user = userRepository.findByEmail("student@example.com").orElseThrow();
        JwtService expired = new JwtService("test-only-student-attendance-jwt-secret-change-this-123456789", -1000);
        String token = expired.generateToken(user);
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
        token = login("student@example.com", "Student@12345");
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer x" + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsAllowsConfiguredFrontendAndRejectsOtherOrigins() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options("/api/auth/login")
                        .header("Origin", "https://untrusted.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void concurrentQrAttemptsCreateExactlyOneRecord() throws Exception {
        String admin = login("admin@example.com", "Admin@12345");
        String lecturer = login("lecturer@example.com", "Lecturer@12345");
        String student = login("student@example.com", "Student@12345");
        Long module = createModule(admin, "RACE101", "Concurrent attendance",
                lecturerRepository.findByEmployeeNumber("LEC-TST-001").orElseThrow().getId());
        enrollStudent(admin, studentRepository.findByStudentNumber("STU-TST-001").orElseThrow().getId(), module);
        Long session = createSession(lecturer, module);
        String qr = activateSessionAndReturnToken(lecturer, session);
        java.util.concurrent.CountDownLatch ready = new java.util.concurrent.CountDownLatch(2);
        java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<Integer> submit = () -> {
                ready.countDown();
                if (!start.await(10, java.util.concurrent.TimeUnit.SECONDS)) throw new AssertionError("Timed out");
                return mockMvc.perform(post("/api/attendance/qr").header("Authorization", "Bearer " + student)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"" + qr + "\"}"))
                        .andReturn().getResponse().getStatus();
            };
            var first = executor.submit(submit);
            var second = executor.submit(submit);
            org.assertj.core.api.Assertions.assertThat(ready.await(10, java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            start.countDown();
            org.assertj.core.api.Assertions.assertThat(java.util.List.of(
                    first.get(20, java.util.concurrent.TimeUnit.SECONDS), second.get(20, java.util.concurrent.TimeUnit.SECONDS)))
                    .containsExactlyInAnyOrder(200, 409);
        }
        org.assertj.core.api.Assertions.assertThat(attendanceRecordRepository.findBySessionId(session)).hasSize(1);
    }

    @Test
    void fullWorkflowFinalizesAttendanceAndExportsLecturerReports() throws Exception {
        String admin = login("admin@example.com", "Admin@12345");
        MvcResult created = mockMvc.perform(post("/api/admin/users").header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Workflow Student","email":"workflow@example.com","password":"Workflow123",
                 "role":"STUDENT","studentNumber":"FLOW001"}
                """)).andExpect(status().isCreated()).andExpect(jsonPath("$.password").doesNotExist()).andReturn();
        Long userId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
        mockMvc.perform(put("/api/admin/users/{id}", userId).header("Authorization", "Bearer " + admin)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Updated Workflow Student\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated Workflow Student"));
        User stored = userRepository.findById(userId).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(stored.getPassword()).isNotEqualTo("Workflow123");
        org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches("Workflow123", stored.getPassword())).isTrue();
        Long studentId = studentRepository.findByUserId(userId).orElseThrow().getId();
        Long module = createModule(admin, "FLOW101", "Workflow module",
                lecturerRepository.findByEmployeeNumber("LEC-TST-001").orElseThrow().getId());
        enrollStudent(admin, studentId, module);
        String lecturer = login("lecturer@example.com", "Lecturer@12345");
        Long session = createSession(lecturer, module);
        String qr = activateSessionAndReturnToken(lecturer, session);
        mockMvc.perform(get("/api/sessions/{id}/qr", session).header("Authorization", "Bearer " + lecturer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").value(qr));
        String student = login("workflow@example.com", "Workflow123");
        String body = "{\"token\":\"" + qr + "\"}";
        mockMvc.perform(post("/api/attendance/qr").header("Authorization", "Bearer " + student)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk());
        mockMvc.perform(post("/api/attendance/qr").header("Authorization", "Bearer " + student)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
        String manual = "{\"records\":[{\"studentId\":" + studentId + ",\"status\":\"PRESENT\"}]}";
        mockMvc.perform(put("/api/sessions/{id}/attendance/manual", session).header("Authorization", "Bearer " + lecturer)
                .contentType(MediaType.APPLICATION_JSON).content(manual)).andExpect(status().isOk())
                .andExpect(jsonPath("$.students[0].markingMethod").value("MANUAL"));
        mockMvc.perform(patch("/api/sessions/{id}/lock", session).header("Authorization", "Bearer " + lecturer))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/attendance/qr").header("Authorization", "Bearer " + student)
                .contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/sessions/{id}/attendance/manual", session).header("Authorization", "Bearer " + lecturer)
                .contentType(MediaType.APPLICATION_JSON).content(manual)).andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/sessions/{id}/complete", session).header("Authorization", "Bearer " + lecturer))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/students/me/attendance-summary").header("Authorization", "Bearer " + student))
                .andExpect(status().isOk()).andExpect(jsonPath("$.overallAttendancePercentage").value(100))
                .andExpect(jsonPath("$.overallLowAttendance").value(false));
        Long absent = createSession(lecturer, module);
        activateSessionAndReturnToken(lecturer, absent);
        mockMvc.perform(patch("/api/sessions/{id}/complete", absent).header("Authorization", "Bearer " + lecturer))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/students/me/attendance-summary").header("Authorization", "Bearer " + student))
                .andExpect(status().isOk()).andExpect(jsonPath("$.overallAttendancePercentage").value(50))
                .andExpect(jsonPath("$.overallLowAttendance").value(true));
        mockMvc.perform(get("/api/reports/attendance").param("moduleId", module.toString())
                .header("Authorization", "Bearer " + lecturer)).andExpect(status().isOk())
                .andExpect(jsonPath("$.rows[0].sessionsHeld").value(2))
                .andExpect(jsonPath("$.rows[0].attendancePercentage").value(50));
        mockMvc.perform(get("/api/reports/attendance/csv").param("moduleId", module.toString())
                .header("Authorization", "Bearer " + lecturer)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("FLOW001")));
        mockMvc.perform(get("/api/reports/attendance/pdf").param("moduleId", module.toString())
                .header("Authorization", "Bearer " + lecturer)).andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"))
                .andExpect(result -> org.assertj.core.api.Assertions.assertThat(result.getResponse().getContentAsByteArray())
                        .startsWith("%PDF".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    @Test
    void percentagesHandleNoSessionsRoundingAndExactWarningThreshold() throws Exception {
        String admin = login("admin@example.com", "Admin@12345");
        String lecturer = login("lecturer@example.com", "Lecturer@12345");
        String student = login("student@example.com", "Student@12345");
        Long module = createModule(admin, "MATH101", "Percentage boundaries",
                lecturerRepository.findByEmployeeNumber("LEC-TST-001").orElseThrow().getId());
        enrollStudent(admin, studentRepository.findByStudentNumber("STU-TST-001").orElseThrow().getId(), module);
        createSession(lecturer, module);
        Long active = createSession(lecturer, module);
        activateSessionAndReturnToken(lecturer, active);
        mockMvc.perform(get("/api/students/me/attendance-summary").header("Authorization", "Bearer " + student))
                .andExpect(status().isOk()).andExpect(jsonPath("$.eligibleCompletedSessions").value(0))
                .andExpect(jsonPath("$.overallAttendancePercentage").value(0))
                .andExpect(jsonPath("$.overallLowAttendance").value(false));
        for (int i = 0; i < 5; i++) {
            Long session = createSession(lecturer, module);
            String qr = activateSessionAndReturnToken(lecturer, session);
            if (i != 0) {
                mockMvc.perform(post("/api/attendance/qr").header("Authorization", "Bearer " + student)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"" + qr + "\"}"))
                        .andExpect(status().isOk());
            }
            mockMvc.perform(patch("/api/sessions/{id}/complete", session).header("Authorization", "Bearer " + lecturer))
                    .andExpect(status().isOk());
            if (i == 2) {
                mockMvc.perform(get("/api/students/me/attendance-summary").header("Authorization", "Bearer " + student))
                        .andExpect(status().isOk()).andExpect(jsonPath("$.overallAttendancePercentage").value(66.67))
                        .andExpect(jsonPath("$.overallLowAttendance").value(true));
            }
        }
        mockMvc.perform(get("/api/students/me/attendance-summary").header("Authorization", "Bearer " + student))
                .andExpect(status().isOk()).andExpect(jsonPath("$.eligibleCompletedSessions").value(5))
                .andExpect(jsonPath("$.overallAttendancePercentage").value(80))
                .andExpect(jsonPath("$.overallLowAttendance").value(false));
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

    private Long createSession(String lecturerToken, Long moduleId) throws Exception {
        MvcResult sessionResult = mockMvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + lecturerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"moduleId":%d,"sessionDate":"2026-10-02","startTime":"09:00","endTime":"10:00","room":"A1"}
                                """.replace("2026-10-02", java.time.LocalDate.now().plusDays(1).toString()).formatted(moduleId)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(sessionResult.getResponse().getContentAsString()).get("id").asLong();
    }

    private String activateSessionAndReturnToken(String lecturerToken, Long sessionId) throws Exception {
        MvcResult activateResult = mockMvc.perform(patch("/api/sessions/{id}/activate", sessionId)
                        .header("Authorization", "Bearer " + lecturerToken))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(activateResult.getResponse().getContentAsString()).get("attendanceToken").asText();
    }

    private Long enrollStudent(String adminToken, Long studentId, Long moduleId) throws Exception {
        MvcResult enrollmentResult = mockMvc.perform(post("/api/enrollments")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"studentId":%d,"moduleId":%d}
                                """.formatted(studentId, moduleId)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(enrollmentResult.getResponse().getContentAsString()).get("id").asLong();
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
