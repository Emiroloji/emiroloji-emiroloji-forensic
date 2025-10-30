// Forensic Face Match System - Test MongoDB Initialization
// ======================================================
// This script initializes the test MongoDB database with sample audit data

// Switch to test database
use forensic_audit_test;

// Create test collections and insert sample data

// Test audit logs collection
db.createCollection("test_audit_logs");

// Sample audit log entries for testing
const sampleAuditLogs = [
    {
        eventId: "test_001",
        eventType: "FACE_COMPARISON",
        serviceName: "ai-service",
        userId: "test_analyst",
        action: "COMPARE_FACES",
        resource: "face_comparison",
        resourceId: "comp_001",
        timestamp: new Date(),
        ipAddress: "192.168.1.100",
        status: "SUCCESS",
        metadata: {
            forensic_match_score: 0.987654,
            forensic_decision: "MATCH",
            biometric_metrics: {
                false_reject_rate: 0.001234,
                false_accept_rate: 0.000987,
                equal_error_rate: 0.001110,
                auc_score: 0.999876
            },
            confidence_interval: {
                lower: 0.985123,
                upper: 0.990185
            },
            landmark_count: 68,
            measurement_accuracy: 0.999654
        },
        chainId: "FORENSIC_" + new ObjectId().toString(),
        chainIndex: 0,
        previousHash: null,
        hash: "a1b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef123456"
    },
    {
        eventId: "test_002", 
        eventType: "BATCH_COMPARISON",
        serviceName: "ai-service",
        userId: "test_operator",
        action: "BATCH_PROCESS",
        resource: "batch_comparison",
        resourceId: "batch_001",
        timestamp: new Date(),
        ipAddress: "192.168.1.101",
        status: "SUCCESS",
        metadata: {
            batch_size: 50,
            batch_results: [
                {
                    comparison_id: "comp_001",
                    match_score: 0.987654,
                    decision: "MATCH",
                    frr: 0.001234,
                    far: 0.000987
                },
                {
                    comparison_id: "comp_002", 
                    match_score: 0.123456,
                    decision: "NO_MATCH",
                    frr: 0.001200,
                    far: 0.001000
                }
            ],
            processing_time: 12.5,
            accuracy_metrics: {
                precision: 0.999123,
                recall: 0.998765,
                f1_score: 0.998944
            }
        },
        chainId: "FORENSIC_BATCH_" + new ObjectId().toString(),
        chainIndex: 0,
        previousHash: null,
        hash: "b2c3d4e5f6789012345678901234567890abcdef1234567890abcdef1234567"
    },
    {
        eventId: "test_003",
        eventType: "SYSTEM_VALIDATION",
        serviceName: "validation-service", 
        userId: "test_auditor",
        action: "RUN_VALIDATION",
        resource: "system_validation",
        resourceId: "val_001",
        timestamp: new Date(),
        ipAddress: "192.168.1.102",
        status: "SUCCESS",
        metadata: {
            validation_type: "comprehensive",
            test_cases_executed: 15,
            test_cases_passed: 15,
            overall_precision: 0.999876,
            overall_error_margin: 0.000124,
            medical_grade_ready: true,
            forensic_grade_ready: true,
            compliance_scores: {
                iso_30107_3: 0.999654,
                medical_device: 0.999123,
                forensic_standard: 0.999876
            }
        },
        chainId: "VALIDATION_" + new ObjectId().toString(),
        chainIndex: 0, 
        previousHash: null,
        hash: "c3d4e5f6789012345678901234567890abcdef1234567890abcdef12345678"
    }
];

// Insert sample audit logs
db.test_audit_logs.insertMany(sampleAuditLogs);

// Create test users collection
db.createCollection("test_users");

// Sample test users
const testUsers = [
    {
        username: "test_admin",
        email: "admin@forensic-test.com",
        role: "administrator",
        permissions: ["read", "write", "admin", "audit"],
        created_at: new Date(),
        is_active: true
    },
    {
        username: "test_analyst",
        email: "analyst@forensic-test.com", 
        role: "forensic_analyst",
        permissions: ["read", "write", "analyze"],
        created_at: new Date(),
        is_active: true
    },
    {
        username: "test_operator",
        email: "operator@forensic-test.com",
        role: "system_operator", 
        permissions: ["read", "operate"],
        created_at: new Date(),
        is_active: true
    },
    {
        username: "test_auditor",
        email: "auditor@forensic-test.com",
        role: "compliance_auditor",
        permissions: ["read", "audit"],
        created_at: new Date(),
        is_active: true
    }
];

// Insert test users
db.test_users.insertMany(testUsers);

// Create test validation results collection
db.createCollection("test_validation_results");

// Sample validation results for different test types
const validationResults = [
    {
        test_name: "Landmark Precision Test",
        test_type: "landmark_precision",
        execution_id: "exec_001",
        timestamp: new Date(),
        passed: true,
        precision_score: 0.999876,
        error_margin: 0.000124,
        confidence_interval: {
            lower: 0.999752,
            upper: 0.999999
        },
        statistical_significance: 0.001,
        compliance_score: 0.999876,
        execution_time: 45.67,
        details: {
            samples_tested: 1000,
            landmark_types: ["facial_contour", "eyebrows", "nose", "eyes", "mouth"],
            precision_breakdown: {
                facial_contour: 0.9996,
                eyebrows: 0.9994,
                nose: 0.9998,
                eyes: 0.9997,
                mouth: 0.9995
            }
        }
    },
    {
        test_name: "Biometric Performance Test",
        test_type: "biometric_performance", 
        execution_id: "exec_002",
        timestamp: new Date(),
        passed: true,
        precision_score: 0.999123,
        error_margin: 0.000877,
        confidence_interval: {
            lower: 0.999000,
            upper: 0.999246
        },
        statistical_significance: 0.999876,
        compliance_score: 1.0,
        execution_time: 78.90,
        details: {
            frr: 0.001234,
            far: 0.000987,
            eer: 0.001110,
            auc_score: 0.999876,
            optimal_threshold: 0.654321,
            iso_compliant: true,
            test_samples: 20000
        }
    },
    {
        test_name: "Forensic Audit Integrity Test",
        test_type: "forensic_integrity",
        execution_id: "exec_003", 
        timestamp: new Date(),
        passed: true,
        precision_score: 0.999999,
        error_margin: 0.000001,
        confidence_interval: {
            lower: 0.999998,
            upper: 1.000000
        },
        statistical_significance: 0.999,
        compliance_score: 1.0,
        execution_time: 23.45,
        details: {
            total_entries: 1000,
            chain_length: 1000,
            chain_valid: true,
            tamper_attempts: 20,
            tamper_detected: 19,
            tamper_detection_rate: 0.95,
            hash_algorithm: "SHA-256"
        }
    }
];

// Insert validation results
db.test_validation_results.insertMany(validationResults);

// Create indexes for performance
db.test_audit_logs.createIndex({ "eventType": 1, "timestamp": -1 });
db.test_audit_logs.createIndex({ "userId": 1, "timestamp": -1 });
db.test_audit_logs.createIndex({ "chainId": 1, "chainIndex": 1 });
db.test_audit_logs.createIndex({ "hash": 1 }, { unique: true });

db.test_validation_results.createIndex({ "test_type": 1, "timestamp": -1 });
db.test_validation_results.createIndex({ "execution_id": 1 });
db.test_validation_results.createIndex({ "passed": 1, "timestamp": -1 });

db.test_users.createIndex({ "username": 1 }, { unique: true });
db.test_users.createIndex({ "email": 1 }, { unique: true });

// Create validation aggregation views
db.createView("test_performance_summary", "test_validation_results", [
    {
        $group: {
            _id: "$test_type",
            total_tests: { $sum: 1 },
            passed_tests: { 
                $sum: { $cond: [{ $eq: ["$passed", true] }, 1, 0] }
            },
            avg_precision: { $avg: "$precision_score" },
            avg_error_margin: { $avg: "$error_margin" },
            avg_compliance: { $avg: "$compliance_score" },
            avg_execution_time: { $avg: "$execution_time" }
        }
    },
    {
        $addFields: {
            pass_rate: { 
                $divide: ["$passed_tests", "$total_tests"] 
            }
        }
    }
]);

// Create audit trail summary view
db.createView("audit_trail_summary", "test_audit_logs", [
    {
        $group: {
            _id: "$eventType", 
            total_events: { $sum: 1 },
            success_events: {
                $sum: { $cond: [{ $eq: ["$status", "SUCCESS"] }, 1, 0] }
            },
            unique_users: { $addToSet: "$userId" },
            avg_processing_time: { 
                $avg: "$metadata.processing_time" 
            }
        }
    },
    {
        $addFields: {
            success_rate: { 
                $divide: ["$success_events", "$total_events"] 
            },
            unique_user_count: { $size: "$unique_users" }
        }
    }
]);

// Print initialization summary
print("=".repeat(60));
print("MongoDB Test Database Initialization Complete");
print("=".repeat(60));

print("Collections created:");
print("- test_audit_logs: " + db.test_audit_logs.countDocuments() + " documents");
print("- test_users: " + db.test_users.countDocuments() + " documents"); 
print("- test_validation_results: " + db.test_validation_results.countDocuments() + " documents");

print("\nViews created:");
print("- test_performance_summary");
print("- audit_trail_summary");

print("\nIndexes created for optimal performance");
print("\nTest database ready for forensic validation testing");
print("=".repeat(60));
