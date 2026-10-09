CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE admins (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    CONSTRAINT uk_admins_user_id UNIQUE (user_id),
    CONSTRAINT fk_admins_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE lecturers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    employee_number VARCHAR(50) NOT NULL,
    CONSTRAINT uk_lecturers_user_id UNIQUE (user_id),
    CONSTRAINT uk_lecturers_employee_number UNIQUE (employee_number),
    CONSTRAINT fk_lecturers_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE students (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    student_number VARCHAR(50) NOT NULL,
    CONSTRAINT uk_students_user_id UNIQUE (user_id),
    CONSTRAINT uk_students_student_number UNIQUE (student_number),
    CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE modules (
    id BIGSERIAL PRIMARY KEY,
    module_code VARCHAR(50) NOT NULL,
    module_name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    lecturer_id BIGINT NOT NULL,
    active BOOLEAN NOT NULL,
    CONSTRAINT uk_modules_module_code UNIQUE (module_code),
    CONSTRAINT fk_modules_lecturer FOREIGN KEY (lecturer_id) REFERENCES lecturers (id)
);

CREATE TABLE attendance_sessions (
    id BIGSERIAL PRIMARY KEY,
    module_id BIGINT NOT NULL,
    lecturer_id BIGINT NOT NULL,
    session_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room VARCHAR(100),
    status VARCHAR(30) NOT NULL,
    attendance_token VARCHAR(255),
    token_expiry TIMESTAMP,
    locked BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_attendance_sessions_attendance_token UNIQUE (attendance_token),
    CONSTRAINT fk_attendance_sessions_module FOREIGN KEY (module_id) REFERENCES modules (id),
    CONSTRAINT fk_attendance_sessions_lecturer FOREIGN KEY (lecturer_id) REFERENCES lecturers (id)
);

CREATE TABLE enrollments (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    module_id BIGINT NOT NULL,
    enrolled_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_enrollments_student_module UNIQUE (student_id, module_id),
    CONSTRAINT fk_enrollments_student FOREIGN KEY (student_id) REFERENCES students (id),
    CONSTRAINT fk_enrollments_module FOREIGN KEY (module_id) REFERENCES modules (id)
);

CREATE TABLE attendance_records (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    marking_method VARCHAR(20) NOT NULL,
    marked_at TIMESTAMP NOT NULL,
    marked_by_user_id BIGINT,
    manual_change_reason VARCHAR(500),
    CONSTRAINT uk_attendance_records_session_student UNIQUE (session_id, student_id),
    CONSTRAINT fk_attendance_records_session FOREIGN KEY (session_id) REFERENCES attendance_sessions (id),
    CONSTRAINT fk_attendance_records_student FOREIGN KEY (student_id) REFERENCES students (id),
    CONSTRAINT fk_attendance_records_marked_by FOREIGN KEY (marked_by_user_id) REFERENCES users (id)
);

CREATE TABLE absence_justifications (
    id BIGSERIAL PRIMARY KEY,
    session_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL,
    review_note VARCHAR(1000),
    submitted_at TIMESTAMP NOT NULL,
    reviewed_at TIMESTAMP,
    reviewed_by_user_id BIGINT,
    CONSTRAINT uk_absence_justifications_session_student UNIQUE (session_id, student_id),
    CONSTRAINT fk_absence_justifications_session FOREIGN KEY (session_id) REFERENCES attendance_sessions (id),
    CONSTRAINT fk_absence_justifications_student FOREIGN KEY (student_id) REFERENCES students (id),
    CONSTRAINT fk_absence_justifications_reviewed_by FOREIGN KEY (reviewed_by_user_id) REFERENCES users (id)
);

CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_modules_lecturer_id ON modules (lecturer_id);
CREATE INDEX idx_sessions_module_id ON attendance_sessions (module_id);
CREATE INDEX idx_sessions_lecturer_id ON attendance_sessions (lecturer_id);
CREATE INDEX idx_sessions_session_date ON attendance_sessions (session_date);
CREATE INDEX idx_enrollments_student_id ON enrollments (student_id);
CREATE INDEX idx_enrollments_module_id ON enrollments (module_id);
CREATE INDEX idx_attendance_records_session_id ON attendance_records (session_id);
CREATE INDEX idx_attendance_records_student_id ON attendance_records (student_id);
CREATE INDEX idx_absence_justifications_session_id ON absence_justifications (session_id);
CREATE INDEX idx_absence_justifications_student_id ON absence_justifications (student_id);
CREATE INDEX idx_absence_justifications_status ON absence_justifications (status);
