-- ═══════════════════════════════════════════════════════════════════════════════
-- V3 — Certificate types: each course now produces one of five award types,
-- each with its own fully independent template (signatories + layout; the
-- background/seal/signature images are stored as type-prefixed files via the
-- existing branding-assets storage, no schema change needed for those).
-- ═══════════════════════════════════════════════════════════════════════════════

CREATE TYPE certificate_type AS ENUM (
    'competence',
    'advanced_certificate',
    'honours_diploma',
    'advanced_diploma',
    'professional_diploma'
);

ALTER TABLE courses      ADD COLUMN certificate_type certificate_type NOT NULL DEFAULT 'competence';
ALTER TABLE certificates ADD COLUMN certificate_type certificate_type NOT NULL DEFAULT 'competence';

-- ── Per-type template settings (signatories + layout) ──────────────────────────
CREATE TABLE certificate_templates (
    certificate_type  certificate_type PRIMARY KEY,
    signatory1_name   VARCHAR(255) NOT NULL DEFAULT '',
    signatory1_title  VARCHAR(255) NOT NULL DEFAULT '',
    signatory2_name   VARCHAR(255) NOT NULL DEFAULT '',
    signatory2_title  VARCHAR(255) NOT NULL DEFAULT '',
    template_layout   JSONB,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Migrate the existing (global) signatories + layout into the 'competence' row
-- so today's already-uploaded template keeps working without re-entry.
INSERT INTO certificate_templates (certificate_type, signatory1_name, signatory1_title, signatory2_name, signatory2_title, template_layout)
SELECT 'competence', signatory1_name, signatory1_title, signatory2_name, signatory2_title, template_layout
FROM org_settings WHERE id = true
ON CONFLICT (certificate_type) DO NOTHING;

INSERT INTO certificate_templates (certificate_type) VALUES
    ('advanced_certificate'),
    ('honours_diploma'),
    ('advanced_diploma'),
    ('professional_diploma')
ON CONFLICT (certificate_type) DO NOTHING;

-- Signatories + layout are now per-type — org_settings keeps only the truly
-- institution-wide fields (name, default certificate-code prefix).
ALTER TABLE org_settings
    DROP COLUMN signatory1_name,
    DROP COLUMN signatory1_title,
    DROP COLUMN signatory2_name,
    DROP COLUMN signatory2_title,
    DROP COLUMN template_layout;
