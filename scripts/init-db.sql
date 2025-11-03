-- Forensic Face Matching System Database Schema
-- PostgreSQL initialization script

-- Create database if not exists (this will be handled by Docker)
-- CREATE DATABASE forensic_db;

-- Connect to the database - use current database
-- \c forensic_db;

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create schemas
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS cases;
CREATE SCHEMA IF NOT EXISTS storage;
CREATE SCHEMA IF NOT EXISTS audit;

-- Set search path
SET search_path TO auth, cases, storage, audit, public;

-- =============================================
-- AUTH SCHEMA - User Management
-- =============================================

-- Users table
CREATE TABLE IF NOT EXISTS auth.users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING', 'EXPIRED', 'LOCKED')),
    two_factor_enabled BOOLEAN DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    last_login TIMESTAMP WITH TIME ZONE,
    failed_login_attempts INTEGER DEFAULT 0,
    account_locked_until TIMESTAMP WITH TIME ZONE,
    password_changed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE, -- <-- BU SATIRI EKLE
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- User roles table
CREATE TABLE IF NOT EXISTS auth.user_roles (
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'INVESTIGATOR', 'ANALYST', 'VIEWER', 'AUDITOR')),
    granted_by UUID REFERENCES auth.users(id),
    granted_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    PRIMARY KEY (user_id, role)
);

-- User sessions table
CREATE TABLE IF NOT EXISTS auth.user_sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    session_token VARCHAR(500) NOT NULL,
    refresh_token VARCHAR(500),
    ip_address INET,
    user_agent TEXT,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_accessed TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Password reset tokens
CREATE TABLE IF NOT EXISTS auth.password_reset_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    token VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- =============================================
-- CASES SCHEMA - Case Management
-- =============================================

-- Cases table
CREATE TABLE IF NOT EXISTS cases.cases (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    case_number VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'IN_PROGRESS', 'COMPLETED', 'CLOSED', 'ARCHIVED')),
    priority VARCHAR(10) NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    classification VARCHAR(20) NOT NULL DEFAULT 'CONFIDENTIAL' CHECK (classification IN ('PUBLIC', 'CONFIDENTIAL', 'SECRET', 'TOP_SECRET')),
    investigator_id UUID NOT NULL REFERENCES auth.users(id),
    department VARCHAR(100),
    jurisdiction VARCHAR(100),
    case_type VARCHAR(50),
    incident_date DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    closed_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE -- <-- BU SATIRI EKLE
);

-- Case participants
CREATE TABLE IF NOT EXISTS cases.case_participants (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    case_id UUID NOT NULL REFERENCES cases.cases(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('INVESTIGATOR', 'ANALYST', 'REVIEWER', 'OBSERVER')),
    assigned_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    assigned_by UUID REFERENCES auth.users(id),
    UNIQUE(case_id, user_id)
);

-- Face comparisons
CREATE TABLE IF NOT EXISTS cases.face_comparisons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    case_id UUID NOT NULL REFERENCES cases.cases(id) ON DELETE CASCADE,
    comparison_name VARCHAR(200),
    image1_id UUID NOT NULL,
    image2_id UUID NOT NULL,
    analysis_result JSONB,
    match_score DECIMAL(5,4),
    decision VARCHAR(20) CHECK (decision IN ('MATCH', 'NO_MATCH', 'UNCERTAIN')),
    confidence_level VARCHAR(20) CHECK (confidence_level IN ('VERY_HIGH', 'HIGH', 'MEDIUM', 'LOW', 'VERY_LOW')),
    model_version VARCHAR(50),
    processing_time_ms INTEGER,
    analyzed_by UUID REFERENCES auth.users(id),
    analyzed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Batch comparisons
CREATE TABLE IF NOT EXISTS cases.batch_comparisons (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    case_id UUID NOT NULL REFERENCES cases.cases(id) ON DELETE CASCADE,
    batch_name VARCHAR(200),
    reference_image_id UUID NOT NULL,
    candidate_count INTEGER NOT NULL,
    matches_found INTEGER DEFAULT 0,
    processing_status VARCHAR(20) DEFAULT 'PENDING' CHECK (processing_status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    results JSONB,
    processed_by UUID REFERENCES auth.users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE
);

-- =============================================
-- STORAGE SCHEMA - File Management
-- =============================================

-- Files table
CREATE TABLE IF NOT EXISTS storage.files (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    file_hash VARCHAR(64) NOT NULL, -- SHA-256 hash
    encryption_key_id VARCHAR(100),
    case_id UUID REFERENCES cases.cases(id),
    uploaded_by UUID NOT NULL REFERENCES auth.users(id),
    upload_date TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_accessed TIMESTAMP WITH TIME ZONE,
    access_count INTEGER DEFAULT 0,
    metadata JSONB,
    virus_scan_status VARCHAR(20) DEFAULT 'PENDING' CHECK (virus_scan_status IN ('PENDING', 'CLEAN', 'INFECTED', 'ERROR')),
    virus_scan_date TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE -- <-- BU SATIRI EKLE
);

-- File versions
CREATE TABLE IF NOT EXISTS storage.file_versions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    file_id UUID NOT NULL REFERENCES storage.files(id) ON DELETE CASCADE,
    version_number INTEGER NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_hash VARCHAR(64) NOT NULL,
    created_by UUID NOT NULL REFERENCES auth.users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    change_description TEXT,
    UNIQUE(file_id, version_number)
);

-- File access logs
CREATE TABLE IF NOT EXISTS storage.file_access_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    file_id UUID NOT NULL REFERENCES storage.files(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id),
    access_type VARCHAR(20) NOT NULL CHECK (access_type IN ('READ', 'WRITE', 'DELETE', 'DOWNLOAD')),
    ip_address INET,
    user_agent TEXT,
    accessed_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- =============================================
-- AUDIT SCHEMA - Audit Logging
-- =============================================

-- Audit events
CREATE TABLE IF NOT EXISTS audit.audit_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_type VARCHAR(50) NOT NULL,
    event_category VARCHAR(30) NOT NULL,
    user_id UUID REFERENCES auth.users(id),
    session_id UUID,
    resource_type VARCHAR(50),
    resource_id UUID,
    action VARCHAR(50) NOT NULL,
    details JSONB,
    ip_address INET,
    user_agent TEXT,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    event_timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    previous_hash VARCHAR(64), -- For chain integrity
    current_hash VARCHAR(64) NOT NULL -- SHA-256 of this record
);

-- System events
CREATE TABLE IF NOT EXISTS audit.system_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_type VARCHAR(50) NOT NULL,
    severity VARCHAR(10) NOT NULL CHECK (severity IN ('INFO', 'WARN', 'ERROR', 'CRITICAL')),
    component VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    details JSONB,
    event_timestamp TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- =============================================
-- INDEXES FOR PERFORMANCE
-- =============================================

-- Auth indexes
CREATE INDEX IF NOT EXISTS idx_users_username ON auth.users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON auth.users(email);
CREATE INDEX IF NOT EXISTS idx_users_status ON auth.users(status);
CREATE INDEX IF NOT EXISTS idx_user_sessions_user_id ON auth.user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_user_sessions_token ON auth.user_sessions(session_token);
CREATE INDEX IF NOT EXISTS idx_user_sessions_expires ON auth.user_sessions(expires_at);

-- Cases indexes
CREATE INDEX IF NOT EXISTS idx_cases_case_number ON cases.cases(case_number);
CREATE INDEX IF NOT EXISTS idx_cases_investigator ON cases.cases(investigator_id);
CREATE INDEX IF NOT EXISTS idx_cases_status ON cases.cases(status);
CREATE INDEX IF NOT EXISTS idx_cases_created_at ON cases.cases(created_at);
CREATE INDEX IF NOT EXISTS idx_face_comparisons_case_id ON cases.face_comparisons(case_id);
CREATE INDEX IF NOT EXISTS idx_face_comparisons_analyzed_at ON cases.face_comparisons(analyzed_at);
CREATE INDEX IF NOT EXISTS idx_batch_comparisons_case_id ON cases.batch_comparisons(case_id);

-- Storage indexes
CREATE INDEX IF NOT EXISTS idx_files_case_id ON storage.files(case_id);
CREATE INDEX IF NOT EXISTS idx_files_uploaded_by ON storage.files(uploaded_by);
CREATE INDEX IF NOT EXISTS idx_files_hash ON storage.files(file_hash);
CREATE INDEX IF NOT EXISTS idx_files_upload_date ON storage.files(upload_date);
CREATE INDEX IF NOT EXISTS idx_file_access_logs_file_id ON storage.file_access_logs(file_id);
CREATE INDEX IF NOT EXISTS idx_file_access_logs_user_id ON storage.file_access_logs(user_id);

-- Audit indexes
CREATE INDEX IF NOT EXISTS idx_audit_events_user_id ON audit.audit_events(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_events_event_type ON audit.audit_events(event_type);
CREATE INDEX IF NOT EXISTS idx_audit_events_timestamp ON audit.audit_events(event_timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_events_resource ON audit.audit_events(resource_type, resource_id);
CREATE INDEX IF NOT EXISTS idx_system_events_component ON audit.system_events(component);
CREATE INDEX IF NOT EXISTS idx_system_events_timestamp ON audit.system_events(event_timestamp);

-- =============================================
-- TRIGGERS FOR UPDATED_AT
-- =============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply triggers to tables with updated_at columns
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON auth.users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_cases_updated_at BEFORE UPDATE ON cases.cases FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_face_comparisons_updated_at BEFORE UPDATE ON cases.face_comparisons FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- INITIAL DATA
-- =============================================

-- Insert default admin user (password: admin123 - CHANGE THIS!)
INSERT INTO auth.users (username, email, password, first_name, last_name, status) 
VALUES ('admin', 'admin@forensic.local', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4J/8KzKz2K', 'System', 'Administrator', 'ACTIVE')
ON CONFLICT (username) DO NOTHING;

-- Grant admin role to default user
INSERT INTO auth.user_roles (user_id, role) 
SELECT id, 'ADMIN' FROM auth.users WHERE username = 'admin'
ON CONFLICT (user_id, role) DO NOTHING;

-- =============================================
-- VIEWS FOR COMMON QUERIES
-- =============================================

-- User with roles view
CREATE OR REPLACE VIEW auth.user_with_roles AS
SELECT 
    u.id,
    u.username,
    u.email,
    u.first_name,
    u.last_name,
    u.status,
    u.two_factor_enabled,
    u.last_login,
    u.created_at,
    u.updated_at,
    ARRAY_AGG(ur.role) as roles
FROM auth.users u
LEFT JOIN auth.user_roles ur ON u.id = ur.user_id
GROUP BY u.id, u.username, u.email, u.first_name, u.last_name, u.status, u.two_factor_enabled, u.last_login, u.created_at, u.updated_at;

-- Case summary view
CREATE OR REPLACE VIEW cases.case_summary AS
SELECT 
    c.id,
    c.case_number,
    c.title,
    c.status,
    c.priority,
    c.classification,
    c.investigator_id,
    u.first_name || ' ' || u.last_name as investigator_name,
    c.created_at,
    c.updated_at,
    COUNT(fc.id) as comparison_count,
    COUNT(bc.id) as batch_count
FROM cases.cases c
LEFT JOIN auth.users u ON c.investigator_id = u.id
LEFT JOIN cases.face_comparisons fc ON c.id = fc.case_id
LEFT JOIN cases.batch_comparisons bc ON c.id = bc.case_id
GROUP BY c.id, c.case_number, c.title, c.status, c.priority, c.classification, c.investigator_id, u.first_name, u.last_name, c.created_at, c.updated_at;

-- =============================================
-- SECURITY POLICIES (Row Level Security)
-- =============================================

-- Enable RLS on sensitive tables (commented out for now - configure after deployment)
-- ALTER TABLE auth.users ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE cases.cases ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE cases.face_comparisons ENABLE ROW LEVEL SECURITY;
-- ALTER TABLE storage.files ENABLE ROW LEVEL SECURITY;

-- Create policies (basic examples - expand as needed)
-- Note: Policies are commented out for initial setup
-- CREATE POLICY user_access_policy ON auth.users
--     FOR ALL TO authenticated
--     USING (id = current_setting('app.current_user_id')::uuid OR 
--            EXISTS (SELECT 1 FROM auth.user_roles WHERE user_id = current_setting('app.current_user_id')::uuid AND role = 'ADMIN'));

-- =============================================
-- FUNCTIONS FOR AUDIT CHAIN
-- =============================================

-- Function to calculate hash for audit chain
CREATE OR REPLACE FUNCTION calculate_audit_hash(record_id UUID)
RETURNS VARCHAR(64) AS $$
DECLARE
    record_data TEXT;
    previous_hash VARCHAR(64);
    combined_data TEXT;
    calculated_hash VARCHAR(64);
BEGIN
    -- Get the record data
    SELECT 
        event_type || '|' || 
        COALESCE(user_id::text, '') || '|' || 
        action || '|' || 
        event_timestamp::text || '|' ||
        COALESCE(details::text, '')
    INTO record_data
    FROM audit.audit_events 
    WHERE id = record_id;
    
    -- Get previous hash
    SELECT current_hash INTO previous_hash
    FROM audit.audit_events 
    WHERE id = (
        SELECT id FROM audit.audit_events 
        WHERE event_timestamp < (SELECT event_timestamp FROM audit.audit_events WHERE id = record_id)
        ORDER BY event_timestamp DESC 
        LIMIT 1
    );
    
    -- Combine data
    combined_data = COALESCE(previous_hash, '') || '|' || record_data;
    
    -- Calculate hash
    calculated_hash = encode(digest(combined_data, 'sha256'), 'hex');
    
    RETURN calculated_hash;
END;
$$ LANGUAGE plpgsql;

-- =============================================
-- GRANTS AND PERMISSIONS
-- =============================================

-- Create application user
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'forensic_app') THEN
        CREATE ROLE forensic_app;
    END IF;
END
$$;

-- Grant permissions
GRANT USAGE ON SCHEMA auth, cases, storage, audit TO forensic_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA auth, cases, storage, audit TO forensic_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA auth, cases, storage, audit TO forensic_app;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA auth, cases, storage, audit TO forensic_app;

-- =============================================
-- COMPLETION MESSAGE
-- =============================================

DO $$
BEGIN
    RAISE NOTICE 'Forensic Face Matching System database schema initialized successfully!';
    RAISE NOTICE 'Default admin user created: admin@forensic.local (password: admin123)';
    RAISE NOTICE 'Please change the default password immediately!';
END
$$;
