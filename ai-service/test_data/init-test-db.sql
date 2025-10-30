-- Forensic Face Match System - Test Database Initialization
-- =========================================================
-- This script initializes the test database with sample data for validation testing

-- Create test schema
CREATE SCHEMA IF NOT EXISTS forensic_test;

-- Set search path
SET search_path TO forensic_test, public;

-- Test users table
CREATE TABLE IF NOT EXISTS test_users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(200) NOT NULL,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

-- Test cases table for validation
CREATE TABLE IF NOT EXISTS test_cases (
    case_id SERIAL PRIMARY KEY,
    case_name VARCHAR(200) NOT NULL,
    description TEXT,
    test_type VARCHAR(100) NOT NULL, -- 'landmark', 'measurement', 'biometric', etc.
    expected_precision DECIMAL(10,6) NOT NULL,
    expected_error_margin DECIMAL(10,6) NOT NULL,
    ground_truth_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Test results table
CREATE TABLE IF NOT EXISTS test_results (
    result_id SERIAL PRIMARY KEY,
    case_id INTEGER REFERENCES test_cases(case_id),
    test_execution_id VARCHAR(100) NOT NULL,
    actual_precision DECIMAL(10,6),
    actual_error_margin DECIMAL(10,6),
    confidence_interval_lower DECIMAL(10,6),
    confidence_interval_upper DECIMAL(10,6),
    test_passed BOOLEAN,
    execution_time DECIMAL(10,3),
    test_metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Biometric test results table
CREATE TABLE IF NOT EXISTS biometric_test_results (
    biometric_id SERIAL PRIMARY KEY,
    test_execution_id VARCHAR(100) NOT NULL,
    frr DECIMAL(10,6) NOT NULL, -- False Reject Rate
    far DECIMAL(10,6) NOT NULL, -- False Accept Rate
    eer DECIMAL(10,6) NOT NULL, -- Equal Error Rate
    auc_score DECIMAL(10,6) NOT NULL, -- Area Under Curve
    optimal_threshold DECIMAL(10,6) NOT NULL,
    iso_compliant BOOLEAN NOT NULL,
    test_samples INTEGER NOT NULL,
    genuine_scores DECIMAL(10,6)[] NOT NULL,
    impostor_scores DECIMAL(10,6)[] NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample test users
INSERT INTO test_users (username, email, role) VALUES
('test_admin', 'admin@forensic-test.com', 'administrator'),
('test_analyst', 'analyst@forensic-test.com', 'forensic_analyst'),
('test_operator', 'operator@forensic-test.com', 'system_operator'),
('test_auditor', 'auditor@forensic-test.com', 'compliance_auditor');

-- Insert sample test cases for landmark precision
INSERT INTO test_cases (case_name, description, test_type, expected_precision, expected_error_margin, ground_truth_data) VALUES
('Landmark Precision - Facial Contour', 'Test 17-point facial contour landmark detection', 'landmark_precision', 0.9996, 0.0004, 
 '{"landmark_type": "facial_contour", "point_count": 17, "reference_standard": "clinical_annotation"}'),

('Landmark Precision - Eyebrows', 'Test 10-point eyebrow landmark detection', 'landmark_precision', 0.9994, 0.0006,
 '{"landmark_type": "eyebrows", "point_count": 10, "reference_standard": "clinical_annotation"}'),

('Landmark Precision - Nose', 'Test 9-point nose landmark detection', 'landmark_precision', 0.9998, 0.0002,
 '{"landmark_type": "nose", "point_count": 9, "reference_standard": "clinical_annotation"}'),

('Landmark Precision - Eyes', 'Test 12-point eye landmark detection', 'landmark_precision', 0.9997, 0.0003,
 '{"landmark_type": "eyes", "point_count": 12, "reference_standard": "clinical_annotation"}'),

('Landmark Precision - Mouth', 'Test 20-point mouth landmark detection', 'landmark_precision', 0.9995, 0.0005,
 '{"landmark_type": "mouth", "point_count": 20, "reference_standard": "clinical_annotation"}');

-- Insert craniofacial measurement test cases
INSERT INTO test_cases (case_name, description, test_type, expected_precision, expected_error_margin, ground_truth_data) VALUES
('Craniofacial - Intercanthal Distance', 'Test intercanthal distance measurement accuracy', 'craniofacial_measurement', 0.9998, 0.0002,
 '{"measurement_type": "intercanthal_distance", "clinical_accuracy": "0.5mm", "reference_standard": "ISO_5725"}'),

('Craniofacial - Biocular Width', 'Test biocular width measurement accuracy', 'craniofacial_measurement', 0.9996, 0.0004,
 '{"measurement_type": "biocular_width", "clinical_accuracy": "0.5mm", "reference_standard": "ISO_5725"}'),

('Craniofacial - Nasal Width', 'Test nasal width measurement accuracy', 'craniofacial_measurement', 0.9997, 0.0003,
 '{"measurement_type": "nasal_width", "clinical_accuracy": "0.5mm", "reference_standard": "ISO_5725"}'),

('Craniofacial - Facial Height', 'Test facial height measurement accuracy', 'craniofacial_measurement', 0.9995, 0.0005,
 '{"measurement_type": "facial_height", "clinical_accuracy": "0.5mm", "reference_standard": "ISO_5725"}'),

('Craniofacial - Mandibular Width', 'Test mandibular width measurement accuracy', 'craniofacial_measurement', 0.9996, 0.0004,
 '{"measurement_type": "mandibular_width", "clinical_accuracy": "0.5mm", "reference_standard": "ISO_5725"}');

-- Insert biometric performance test cases
INSERT INTO test_cases (case_name, description, test_type, expected_precision, expected_error_margin, ground_truth_data) VALUES
('Biometric Performance - High Quality', 'FRR/FAR analysis with high quality images', 'biometric_performance', 0.9999, 0.0001,
 '{"image_quality": "high", "expected_frr": 0.001, "expected_far": 0.001, "iso_standard": "ISO_30107_3"}'),

('Biometric Performance - Standard Quality', 'FRR/FAR analysis with standard quality images', 'biometric_performance', 0.9995, 0.0005,
 '{"image_quality": "standard", "expected_frr": 0.005, "expected_far": 0.005, "iso_standard": "ISO_30107_3"}'),

('Biometric Performance - Edge Cases', 'FRR/FAR analysis with challenging conditions', 'biometric_performance', 0.9990, 0.0010,
 '{"image_quality": "challenging", "expected_frr": 0.010, "expected_far": 0.010, "iso_standard": "ISO_30107_3"}');

-- Insert statistical robustness test cases
INSERT INTO test_cases (case_name, description, test_type, expected_precision, expected_error_margin, ground_truth_data) VALUES
('Bootstrap Confidence Intervals', 'Test Bootstrap statistical method robustness', 'statistical_robustness', 0.9995, 0.0005,
 '{"method": "bootstrap", "confidence_level": 0.999, "bootstrap_samples": 1000, "statistical_standard": "medical_grade"}'),

('ROC Analysis Validation', 'Test ROC curve analysis and AUC calculation', 'statistical_robustness', 0.9998, 0.0002,
 '{"method": "roc_analysis", "expected_auc": 0.999, "statistical_standard": "forensic_grade"}');

-- Insert forensic integrity test cases
INSERT INTO test_cases (case_name, description, test_type, expected_precision, expected_error_margin, ground_truth_data) VALUES
('Hash Chain Integrity', 'Test forensic hash chain tamper detection', 'forensic_integrity', 0.9999, 0.0001,
 '{"hash_algorithm": "SHA-256", "chain_length": 1000, "tamper_detection_rate": 0.999, "standard": "forensic_grade"}'),

('Audit Trail Validation', 'Test comprehensive audit trail integrity', 'forensic_integrity', 0.9998, 0.0002,
 '{"audit_components": ["timestamp", "user", "action", "result"], "integrity_standard": "forensic_grade"}');

-- Create indexes for performance
CREATE INDEX idx_test_cases_type ON test_cases(test_type);
CREATE INDEX idx_test_results_case_id ON test_results(case_id);
CREATE INDEX idx_test_results_execution_id ON test_results(test_execution_id);
CREATE INDEX idx_biometric_results_execution_id ON biometric_test_results(test_execution_id);
CREATE INDEX idx_test_results_created_at ON test_results(created_at);

-- Create views for test reporting
CREATE OR REPLACE VIEW test_summary AS
SELECT 
    tc.test_type,
    COUNT(*) as total_tests,
    COUNT(CASE WHEN tr.test_passed THEN 1 END) as passed_tests,
    AVG(tr.actual_precision) as avg_precision,
    AVG(tr.actual_error_margin) as avg_error_margin,
    MIN(tr.actual_precision) as min_precision,
    MAX(tr.actual_precision) as max_precision,
    AVG(tr.execution_time) as avg_execution_time
FROM test_cases tc
LEFT JOIN test_results tr ON tc.case_id = tr.case_id
GROUP BY tc.test_type;

CREATE OR REPLACE VIEW biometric_summary AS
SELECT 
    test_execution_id,
    AVG(frr) as avg_frr,
    AVG(far) as avg_far,
    AVG(eer) as avg_eer,
    AVG(auc_score) as avg_auc,
    COUNT(CASE WHEN iso_compliant THEN 1 END) as iso_compliant_count,
    COUNT(*) as total_tests
FROM biometric_test_results
GROUP BY test_execution_id;

-- Grant permissions
GRANT ALL PRIVILEGES ON SCHEMA forensic_test TO test_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA forensic_test TO test_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA forensic_test TO test_user;

-- Commit transaction
COMMIT;

-- Log completion
DO $$
BEGIN
    RAISE NOTICE 'Forensic test database initialized successfully';
    RAISE NOTICE 'Test cases created: %', (SELECT COUNT(*) FROM forensic_test.test_cases);
    RAISE NOTICE 'Test users created: %', (SELECT COUNT(*) FROM forensic_test.test_users);
END $$;
