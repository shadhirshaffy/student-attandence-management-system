package com.studentattendance.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;

import com.studentattendance.entity.AttendanceRecord;
import com.studentattendance.entity.AttendanceStatus;
import com.studentattendance.entity.Enrollment;
import com.studentattendance.entity.Lecturer;
import com.studentattendance.entity.MarkingMethod;
import com.studentattendance.entity.Module;
import com.studentattendance.entity.Role;
import com.studentattendance.entity.Session;
import com.studentattendance.entity.Student;
import com.studentattendance.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class JpaMappingTests {

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
    private AttendanceRecordRepository attendanceRecordRepository;

    @Test
    void createsCoreRelationshipsAndSupportsRepositoryLookups() {
        Lecturer lecturer = lecturerRepository.save(lecturer("EMP-001", "lecturer@example.com"));
        Student student = studentRepository.save(student("STU-001", "student@example.com"));
        Module module = moduleRepository.save(module("CS101", lecturer));
        Enrollment enrollment = enrollmentRepository.save(enrollment(student, module));
        Session session = sessionRepository.save(session(module, lecturer));
        AttendanceRecord record = attendanceRecordRepository.save(attendanceRecord(session, student));

        assertThat(userRepository.findByEmail("student@example.com")).isPresent();
        assertThat(studentRepository.findByStudentNumber("STU-001")).contains(student);
        assertThat(moduleRepository.findByModuleCode("CS101")).contains(module);
        assertThat(moduleRepository.findByLecturer(lecturer)).containsExactly(module);
        assertThat(enrollmentRepository.findByStudentAndModule(student, module)).contains(enrollment);
        assertThat(sessionRepository.findByModule(module)).containsExactly(session);
        assertThat(attendanceRecordRepository.findByStudentAndSession(student, session)).contains(record);
    }

    @Test
    void preventsDuplicateStudentModuleEnrollment() {
        Lecturer lecturer = lecturerRepository.save(lecturer("EMP-002", "lecturer2@example.com"));
        Student student = studentRepository.save(student("STU-002", "student2@example.com"));
        Module module = moduleRepository.save(module("CS102", lecturer));
        enrollmentRepository.saveAndFlush(enrollment(student, module));

        assertThatThrownBy(() -> enrollmentRepository.saveAndFlush(enrollment(student, module)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void preventsDuplicateAttendanceRecordForSameStudentAndSession() {
        Lecturer lecturer = lecturerRepository.save(lecturer("EMP-003", "lecturer3@example.com"));
        Student student = studentRepository.save(student("STU-003", "student3@example.com"));
        Module module = moduleRepository.save(module("CS103", lecturer));
        Session session = sessionRepository.save(session(module, lecturer));
        attendanceRecordRepository.saveAndFlush(attendanceRecord(session, student));

        assertThatThrownBy(() -> attendanceRecordRepository.saveAndFlush(attendanceRecord(session, student)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Student student(String studentNumber, String email) {
        User user = user("Student " + studentNumber, email, Role.STUDENT);
        Student student = new Student();
        student.setUser(user);
        student.setStudentNumber(studentNumber);
        return student;
    }

    private Lecturer lecturer(String employeeNumber, String email) {
        User user = user("Lecturer " + employeeNumber, email, Role.LECTURER);
        Lecturer lecturer = new Lecturer();
        lecturer.setUser(user);
        lecturer.setEmployeeNumber(employeeNumber);
        return lecturer;
    }

    private User user(String name, String email, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword("{noop}password");
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private Module module(String moduleCode, Lecturer lecturer) {
        Module module = new Module();
        module.setModuleCode(moduleCode);
        module.setModuleName("Programming Fundamentals");
        module.setDescription("Introductory programming module");
        module.setLecturer(lecturer);
        module.setActive(true);
        return module;
    }

    private Enrollment enrollment(Student student, Module module) {
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setModule(module);
        return enrollment;
    }

    private Session session(Module module, Lecturer lecturer) {
        Session session = new Session();
        session.setModule(module);
        session.setLecturer(lecturer);
        session.setSessionDate(LocalDate.now());
        session.setStartTime(LocalTime.of(9, 0));
        session.setEndTime(LocalTime.of(10, 0));
        session.setRoom("Lab 1");
        session.setAttendanceToken("token-" + module.getModuleCode());
        return session;
    }

    private AttendanceRecord attendanceRecord(Session session, Student student) {
        AttendanceRecord record = new AttendanceRecord();
        record.setSession(session);
        record.setStudent(student);
        record.setStatus(AttendanceStatus.PRESENT);
        record.setMarkingMethod(MarkingMethod.MANUAL);
        return record;
    }
}
