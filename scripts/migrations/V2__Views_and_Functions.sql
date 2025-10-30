-- Flyway migration script for Forensic Face Matching System
-- Version: 1.0.1
-- Description: Views, functions, and additional database objects

-- Set search path
SET search_path TO auth, cases, storage, audit, public;

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

-- File summary view
CREATE OR REPLACE VIEW storage.file_summary AS
SELECT 
    f.id,
    f.original_filename,
    f.stored_filename,
    f.file_size,
    f.mime_type,
    f.file_hash,
    f.case_id,
    c.case_number,
    f.uploaded_by,
    u.first_name || ' ' || u.last_name as uploaded_by_name,
    f.upload_date,
    f.virus_scan_status,
    f.access_count,
    f.last_accessed
FROM storage.files f
LEFT JOIN cases.cases c ON f.case_id = c.id
LEFT JOIN auth.users u ON f.uploaded_by = u.id;

-- Recent audit events view
CREATE OR REPLACE VIEW audit.recent_audit_events AS
SELECT 
    ae.id,
    ae.event_type,
    ae.event_category,
    ae.user_id,
    u.username,
    ae.resource_type,
    ae.resource_id,
    ae.action,
    ae.success,
    ae.event_timestamp,
    ae.ip_address
FROM audit.audit_events ae
LEFT JOIN auth.users u ON ae.user_id = u.id
WHERE ae.event_timestamp >= NOW() - INTERVAL '24 hours'
ORDER BY ae.event_timestamp DESC;

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

-- Function to verify audit chain integrity
CREATE OR REPLACE FUNCTION verify_audit_chain()
RETURNS TABLE(
    is_valid BOOLEAN,
    total_events BIGINT,
    broken_links BIGINT,
    errors TEXT[]
) AS $$
DECLARE
    event_record RECORD;
    previous_hash VARCHAR(64) := NULL;
    current_hash VARCHAR(64);
    error_count BIGINT := 0;
    error_list TEXT[] := ARRAY[]::TEXT[];
    total_count BIGINT;
BEGIN
    -- Get total count
    SELECT COUNT(*) INTO total_count FROM audit.audit_events;
    
    -- Check each event in chronological order
    FOR event_record IN 
        SELECT id, current_hash, event_timestamp
        FROM audit.audit_events 
        ORDER BY event_timestamp ASC
    LOOP
        -- Verify hash calculation
        current_hash := calculate_audit_hash(event_record.id);
        
        IF event_record.current_hash != current_hash THEN
            error_count := error_count + 1;
            error_list := array_append(error_list, 
                'Event ' || event_record.id || ' has incorrect hash');
        END IF;
        
        previous_hash := event_record.current_hash;
    END LOOP;
    
    RETURN QUERY SELECT 
        (error_count = 0) as is_valid,
        total_count,
        error_count,
        error_list;
END;
$$ LANGUAGE plpgsql;

-- Function to get user permissions
CREATE OR REPLACE FUNCTION get_user_permissions(user_uuid UUID)
RETURNS TEXT[] AS $$
DECLARE
    user_roles TEXT[];
    all_permissions TEXT[] := ARRAY[]::TEXT[];
    role_permissions TEXT[];
    role_name TEXT;
BEGIN
    -- Get user roles
    SELECT ARRAY_AGG(role) INTO user_roles
    FROM auth.user_roles 
    WHERE user_id = user_uuid;
    
    -- Get permissions for each role
    IF user_roles IS NOT NULL THEN
        FOREACH role_name IN ARRAY user_roles
        LOOP
            role_permissions := CASE role_name
                WHEN 'ADMIN' THEN ARRAY[
                    'USER_MANAGE', 'CASE_CREATE', 'CASE_READ', 'CASE_UPDATE', 'CASE_DELETE',
                    'ANALYSIS_PERFORM', 'REPORT_GENERATE', 'AUDIT_READ', 'SYSTEM_CONFIGURE'
                ]
                WHEN 'INVESTIGATOR' THEN ARRAY[
                    'CASE_CREATE', 'CASE_READ', 'CASE_UPDATE', 'ANALYSIS_PERFORM', 'REPORT_GENERATE'
                ]
                WHEN 'ANALYST' THEN ARRAY[
                    'CASE_READ', 'ANALYSIS_PERFORM', 'REPORT_GENERATE'
                ]
                WHEN 'VIEWER' THEN ARRAY[
                    'CASE_READ', 'REPORT_READ'
                ]
                WHEN 'AUDITOR' THEN ARRAY[
                    'AUDIT_READ', 'REPORT_READ'
                ]
                ELSE ARRAY[]::TEXT[]
            END;
            
            all_permissions := all_permissions || role_permissions;
        END LOOP;
    END IF;
    
    -- Remove duplicates
    SELECT ARRAY_AGG(DISTINCT permission) INTO all_permissions
    FROM unnest(all_permissions) as permission;
    
    RETURN COALESCE(all_permissions, ARRAY[]::TEXT[]);
END;
$$ LANGUAGE plpgsql;

-- Function to check if user has permission
CREATE OR REPLACE FUNCTION user_has_permission(user_uuid UUID, permission_name TEXT)
RETURNS BOOLEAN AS $$
DECLARE
    user_permissions TEXT[];
BEGIN
    user_permissions := get_user_permissions(user_uuid);
    RETURN permission_name = ANY(user_permissions);
END;
$$ LANGUAGE plpgsql;

-- Function to log audit event
CREATE OR REPLACE FUNCTION log_audit_event(
    p_event_type VARCHAR(50),
    p_event_category VARCHAR(30),
    p_user_id UUID,
    p_session_id UUID,
    p_resource_type VARCHAR(50),
    p_resource_id UUID,
    p_action VARCHAR(50),
    p_details JSONB,
    p_ip_address INET,
    p_user_agent TEXT,
    p_success BOOLEAN,
    p_error_message TEXT
)
RETURNS UUID AS $$
DECLARE
    event_id UUID;
    previous_hash VARCHAR(64);
    current_hash VARCHAR(64);
    record_data TEXT;
BEGIN
    -- Generate new event ID
    event_id := uuid_generate_v4();
    
    -- Get previous hash
    SELECT current_hash INTO previous_hash
    FROM audit.audit_events 
    ORDER BY event_timestamp DESC 
    LIMIT 1;
    
    -- Prepare record data for hashing
    record_data := p_event_type || '|' || 
                   COALESCE(p_user_id::text, '') || '|' || 
                   p_action || '|' || 
                   NOW()::text || '|' ||
                   COALESCE(p_details::text, '');
    
    -- Calculate current hash
    current_hash := encode(digest(COALESCE(previous_hash, '') || '|' || record_data, 'sha256'), 'hex');
    
    -- Insert audit event
    INSERT INTO audit.audit_events (
        id, event_type, event_category, user_id, session_id, resource_type, 
        resource_id, action, details, ip_address, user_agent, success, 
        error_message, previous_hash, current_hash
    ) VALUES (
        event_id, p_event_type, p_event_category, p_user_id, p_session_id, 
        p_resource_type, p_resource_id, p_action, p_details, p_ip_address, 
        p_user_agent, p_success, p_error_message, previous_hash, current_hash
    );
    
    RETURN event_id;
END;
$$ LANGUAGE plpgsql;

-- =============================================
-- SECURITY POLICIES (Row Level Security)
-- =============================================

-- Enable RLS on sensitive tables
ALTER TABLE auth.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE cases.cases ENABLE ROW LEVEL SECURITY;
ALTER TABLE cases.face_comparisons ENABLE ROW LEVEL SECURITY;
ALTER TABLE storage.files ENABLE ROW LEVEL SECURITY;

-- Create policies (basic examples - expand as needed)
CREATE POLICY user_access_policy ON auth.users
    FOR ALL TO authenticated
    USING (id = current_setting('app.current_user_id')::uuid OR 
           EXISTS (SELECT 1 FROM auth.user_roles WHERE user_id = current_setting('app.current_user_id')::uuid AND role = 'ADMIN'));

CREATE POLICY case_access_policy ON cases.cases
    FOR ALL TO authenticated
    USING (
        investigator_id = current_setting('app.current_user_id')::uuid OR
        EXISTS (
            SELECT 1 FROM cases.case_participants cp 
            WHERE cp.case_id = cases.id AND cp.user_id = current_setting('app.current_user_id')::uuid
        ) OR
        EXISTS (SELECT 1 FROM auth.user_roles WHERE user_id = current_setting('app.current_user_id')::uuid AND role = 'ADMIN')
    );

-- =============================================
-- ADDITIONAL INDEXES
-- =============================================

-- Composite indexes for better performance
CREATE INDEX IF NOT EXISTS idx_audit_events_user_timestamp ON audit.audit_events(user_id, event_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_audit_events_type_timestamp ON audit.audit_events(event_type, event_timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_face_comparisons_case_analyzed ON cases.face_comparisons(case_id, analyzed_at DESC);
CREATE INDEX IF NOT EXISTS idx_files_case_upload ON storage.files(case_id, upload_date DESC);
CREATE INDEX IF NOT EXISTS idx_cases_investigator_status ON cases.cases(investigator_id, status);

-- Partial indexes for active records
CREATE INDEX IF NOT EXISTS idx_users_active ON auth.users(id) WHERE status = 'ACTIVE';
CREATE INDEX IF NOT EXISTS idx_cases_open ON cases.cases(id) WHERE status IN ('OPEN', 'IN_PROGRESS');
CREATE INDEX IF NOT EXISTS idx_audit_events_recent ON audit.audit_events(event_timestamp DESC) WHERE event_timestamp >= NOW() - INTERVAL '30 days';

-- =============================================
-- GRANTS AND PERMISSIONS
-- =============================================

-- Create application user if not exists
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
GRANT SELECT ON ALL VIEWS IN SCHEMA auth, cases, storage, audit TO forensic_app;
