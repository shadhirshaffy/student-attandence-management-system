package com.studentattendance.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI studentAttendanceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Student Attendance Management System API")
                        .version("v1")
                        .description("REST API foundation for the Student Attendance Management System."));
    }
}
