package com.studentattendance;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = StudentAttendanceBackendApplication.class)
@ActiveProfiles("test")
class StudentAttendanceBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
