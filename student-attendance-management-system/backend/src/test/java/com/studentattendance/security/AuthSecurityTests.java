package com.studentattendance.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentattendance.StudentAttendanceBackendApplication;
import com.studentattendance.entity.Role;
import com.studentattendance.entity.User;
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
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        createUser("admin@example.com", "Admin User", "Admin@12345", Role.ADMIN);
        createUser("lecturer@example.com", "Lecturer User", "Lecturer@12345", Role.LECTURER);
        createUser("student@example.com", "Student User", "Student@12345", Role.STUDENT);
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

    private void createUser(String email, String name, String password, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        userRepository.save(user);
    }
}
