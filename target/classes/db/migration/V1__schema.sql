-- ═══════════════════════════════════════════════════════════════════════════════
-- CeMIS — Certificate Management Information System
-- University of Zambia, Centre for ICT — Technology and E-Learning Support Unit
-- V1 — Initial schema
-- ═══════════════════════════════════════════════════════════════════════════════

-- ── Enums ─────────────────────────────────────────────────────────────────────
CREATE TYPE app_role            AS ENUM ('admin', 'user');
CREATE TYPE certificate_status  AS ENUM ('valid', 'revoked');
CREATE TYPE enrolment_status    AS ENUM ('enrolled', 'in_progress', 'completed', 'certified');
CREATE TYPE payment_status_type AS ENUM ('pending', 'paid', 'waived', 'free');

-- ── Users (admin accounts) ─────────────────────────────────────────────────────
CREATE TABLE users (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name     VARCHAR(255),
    role          app_role    NOT NULL DEFAULT 'user',
    active        BOOLEAN     NOT NULL DEFAULT true,
    must_change_password BOOLEAN NOT NULL DEFAULT false,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ── Org settings (singleton row) ───────────────────────────────────────────────
CREATE TABLE org_settings (
    id                BOOLEAN     PRIMARY KEY DEFAULT true CHECK (id = true),
    org_name          VARCHAR(255) NOT NULL DEFAULT 'CICT-TeLS',
    org_prefix        VARCHAR(20)  NOT NULL DEFAULT 'TELS',
    signatory1_name   VARCHAR(255) NOT NULL DEFAULT '',
    signatory1_title  VARCHAR(255) NOT NULL DEFAULT '',
    signatory2_name   VARCHAR(255) NOT NULL DEFAULT '',
    signatory2_title  VARCHAR(255) NOT NULL DEFAULT '',
    template_layout   JSONB,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ── Courses ────────────────────────────────────────────────────────────────────
CREATE TABLE courses (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    code          VARCHAR(50)  UNIQUE NOT NULL,
    prefix        VARCHAR(20)  NOT NULL,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    category      VARCHAR(50)  NOT NULL DEFAULT 'general',
    mode          VARCHAR(50),
    duration_text VARCHAR(100),
    fee_unza      NUMERIC(10,2),
    fee_non_unza  NUMERIC(10,2),
    start_date    DATE,
    time_slot     VARCHAR(100),
    active        BOOLEAN      NOT NULL DEFAULT true,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Students ───────────────────────────────────────────────────────────────────
CREATE TABLE students (
    id                  UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name           VARCHAR(255) NOT NULL,
    email               VARCHAR(255),
    phone               VARCHAR(50),
    national_id         VARCHAR(50),
    unza_student_id     VARCHAR(50),
    category            VARCHAR(20)  NOT NULL DEFAULT 'non_unza',
    notes               TEXT,
    metadata            JSONB,
    pii_consent_at      TIMESTAMPTZ,
    pii_consent_source  VARCHAR(50),
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Certificates ───────────────────────────────────────────────────────────────
CREATE TABLE certificates (
    id               UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    certificate_id   VARCHAR(100) UNIQUE NOT NULL,
    certificate_code VARCHAR(100) UNIQUE,
    student_id       UUID         REFERENCES students(id) ON DELETE SET NULL,
    course_id        UUID         REFERENCES courses(id)  ON DELETE SET NULL,
    recipient_name   VARCHAR(255) NOT NULL,
    recipient_email  VARCHAR(255),
    programme        VARCHAR(255) NOT NULL,
    issuer_name      VARCHAR(255) NOT NULL DEFAULT 'CICT-TeLS',
    issued_by        UUID         REFERENCES users(id)    ON DELETE SET NULL,
    issue_date       DATE         NOT NULL DEFAULT CURRENT_DATE,
    expiry_date      DATE,
    status           certificate_status NOT NULL DEFAULT 'valid',
    revoked_at       TIMESTAMPTZ,
    revoke_reason    TEXT,
    email_status     VARCHAR(20)  NOT NULL DEFAULT 'pending',
    email_sent_at    TIMESTAMPTZ,
    email_attempts   INTEGER      NOT NULL DEFAULT 0,
    email_last_error TEXT,
    pdf_path         VARCHAR(500),
    signed_payload   JSONB,
    signature        TEXT,
    national_id      VARCHAR(50),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Enrolments ─────────────────────────────────────────────────────────────────
CREATE TABLE enrolments (
    id             UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id     UUID          NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    course_id      UUID          NOT NULL REFERENCES courses(id),
    status         enrolment_status NOT NULL DEFAULT 'enrolled',
    payment_status payment_status_type NOT NULL DEFAULT 'pending',
    fee_charged    NUMERIC(10,2),
    enrolled_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    started_at     TIMESTAMPTZ,
    completed_at   TIMESTAMPTZ,
    certificate_id UUID          REFERENCES certificates(id) ON DELETE SET NULL,
    notes          TEXT,
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ── Certificate counters (sequential numbering per prefix) ─────────────────────
CREATE TABLE certificate_counters (
    prefix     VARCHAR(20) PRIMARY KEY,
    last_value INTEGER     NOT NULL DEFAULT 0
);

-- ── Student access / audit log ─────────────────────────────────────────────────
CREATE TABLE student_access_log (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID        REFERENCES students(id) ON DELETE SET NULL,
    actor_id   UUID        REFERENCES users(id)    ON DELETE SET NULL,
    action     VARCHAR(100) NOT NULL,
    detail     TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ── Indexes ────────────────────────────────────────────────────────────────────
CREATE INDEX idx_enrolments_student   ON enrolments(student_id);
CREATE INDEX idx_enrolments_course    ON enrolments(course_id);
CREATE INDEX idx_enrolments_status    ON enrolments(status);
CREATE INDEX idx_certificates_code    ON certificates(certificate_code);
CREATE INDEX idx_certificates_student ON certificates(student_id);
CREATE INDEX idx_students_email       ON students(email);
CREATE INDEX idx_audit_student        ON student_access_log(student_id);
CREATE INDEX idx_audit_created        ON student_access_log(created_at DESC);

-- ── Seed data ─────────────────────────────────────────────────────────────────
INSERT INTO org_settings (org_name, org_prefix, signatory1_name, signatory1_title, signatory2_name, signatory2_title)
VALUES (
    'CICT — Technology and E-Learning Support Unit (TeLS)',
    'TELS',
    'Director of CICT',
    'Director, Centre for Information and Communication Technology',
    'University Registrar',
    'Registrar, University of Zambia'
) ON CONFLICT DO NOTHING;

-- Default admin account  (password: Admin@1234  — CHANGE IMMEDIATELY after first login)
-- Hash generated with BCrypt strength 12
INSERT INTO users (email, password_hash, full_name, role, must_change_password)
VALUES (
    'admin@tels.unza.ac.zm',
    '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    'System Administrator',
    'admin',
    true
) ON CONFLICT (email) DO NOTHING;
