-- Additions needed by legacy local databases created before the current schema.
-- Back up the database before applying. Does not delete or rewrite existing data.
BEGIN;
ALTER TABLE attendance_records ADD COLUMN IF NOT EXISTS marked_by_user_id BIGINT REFERENCES users(id);
ALTER TABLE attendance_records ADD COLUMN IF NOT EXISTS manual_change_reason VARCHAR(500);
CREATE TABLE IF NOT EXISTS absence_justifications (
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
COMMIT;
