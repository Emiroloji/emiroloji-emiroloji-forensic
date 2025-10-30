// Forensic Face Matching System - MongoDB Initialization Script
// This script initializes MongoDB collections for audit logging and metadata storage

// Switch to forensic_audit database
db = db.getSiblingDB('forensic_audit');

// Create collections with validation schemas
print('Creating audit collections...');

// Audit Events Collection
db.createCollection('audit_events', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['eventId', 'eventType', 'eventCategory', 'action', 'timestamp', 'success'],
            properties: {
                eventId: {
                    bsonType: 'string',
                    description: 'Unique event identifier'
                },
                eventType: {
                    bsonType: 'string',
                    enum: ['AUTHENTICATION', 'AUTHORIZATION', 'DATA_ACCESS', 'DATA_MODIFICATION', 'SYSTEM_EVENT', 'SECURITY_EVENT'],
                    description: 'Type of audit event'
                },
                eventCategory: {
                    bsonType: 'string',
                    enum: ['LOGIN', 'LOGOUT', 'PASSWORD_CHANGE', 'ROLE_CHANGE', 'CASE_CREATE', 'CASE_UPDATE', 'CASE_DELETE', 'FILE_UPLOAD', 'FILE_DOWNLOAD', 'ANALYSIS_PERFORM', 'REPORT_GENERATE', 'USER_CREATE', 'USER_UPDATE', 'USER_DELETE', 'SYSTEM_START', 'SYSTEM_STOP', 'ERROR', 'SECURITY_VIOLATION'],
                    description: 'Category of the event'
                },
                userId: {
                    bsonType: 'string',
                    description: 'User who performed the action'
                },
                sessionId: {
                    bsonType: 'string',
                    description: 'Session identifier'
                },
                resourceType: {
                    bsonType: 'string',
                    enum: ['USER', 'CASE', 'FILE', 'COMPARISON', 'BATCH', 'SYSTEM'],
                    description: 'Type of resource affected'
                },
                resourceId: {
                    bsonType: 'string',
                    description: 'ID of the resource affected'
                },
                action: {
                    bsonType: 'string',
                    description: 'Action performed'
                },
                details: {
                    bsonType: 'object',
                    description: 'Additional event details'
                },
                ipAddress: {
                    bsonType: 'string',
                    description: 'IP address of the client'
                },
                userAgent: {
                    bsonType: 'string',
                    description: 'User agent string'
                },
                success: {
                    bsonType: 'bool',
                    description: 'Whether the action was successful'
                },
                errorMessage: {
                    bsonType: 'string',
                    description: 'Error message if action failed'
                },
                timestamp: {
                    bsonType: 'date',
                    description: 'When the event occurred'
                },
                previousHash: {
                    bsonType: 'string',
                    description: 'Hash of previous event for chain integrity'
                },
                currentHash: {
                    bsonType: 'string',
                    description: 'Hash of current event for chain integrity'
                },
                chainIndex: {
                    bsonType: 'int',
                    description: 'Position in the audit chain'
                }
            }
        }
    }
});

// System Events Collection
db.createCollection('system_events', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['eventId', 'eventType', 'severity', 'component', 'message', 'timestamp'],
            properties: {
                eventId: {
                    bsonType: 'string',
                    description: 'Unique event identifier'
                },
                eventType: {
                    bsonType: 'string',
                    enum: ['STARTUP', 'SHUTDOWN', 'ERROR', 'WARNING', 'INFO', 'PERFORMANCE', 'SECURITY'],
                    description: 'Type of system event'
                },
                severity: {
                    bsonType: 'string',
                    enum: ['INFO', 'WARN', 'ERROR', 'CRITICAL'],
                    description: 'Severity level'
                },
                component: {
                    bsonType: 'string',
                    enum: ['GATEWAY', 'AUTH', 'CASE', 'STORAGE', 'AUDIT', 'AI', 'FRONTEND', 'DATABASE', 'REDIS', 'RABBITMQ'],
                    description: 'System component that generated the event'
                },
                message: {
                    bsonType: 'string',
                    description: 'Event message'
                },
                details: {
                    bsonType: 'object',
                    description: 'Additional event details'
                },
                timestamp: {
                    bsonType: 'date',
                    description: 'When the event occurred'
                },
                hostname: {
                    bsonType: 'string',
                    description: 'Hostname where event occurred'
                },
                processId: {
                    bsonType: 'int',
                    description: 'Process ID'
                }
            }
        }
    }
});

// Analysis Metadata Collection
db.createCollection('analysis_metadata', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['analysisId', 'analysisType', 'userId', 'timestamp'],
            properties: {
                analysisId: {
                    bsonType: 'string',
                    description: 'Unique analysis identifier'
                },
                analysisType: {
                    bsonType: 'string',
                    enum: ['FACE_DETECTION', 'FACE_COMPARISON', 'VIDEO_PROCESSING', 'BATCH_COMPARISON'],
                    description: 'Type of analysis performed'
                },
                userId: {
                    bsonType: 'string',
                    description: 'User who requested the analysis'
                },
                caseId: {
                    bsonType: 'string',
                    description: 'Associated case ID'
                },
                inputFiles: {
                    bsonType: 'array',
                    items: {
                        bsonType: 'string'
                    },
                    description: 'Input file IDs'
                },
                outputFiles: {
                    bsonType: 'array',
                    items: {
                        bsonType: 'string'
                    },
                    description: 'Output file IDs'
                },
                parameters: {
                    bsonType: 'object',
                    description: 'Analysis parameters'
                },
                results: {
                    bsonType: 'object',
                    description: 'Analysis results'
                },
                processingTimeMs: {
                    bsonType: 'int',
                    description: 'Processing time in milliseconds'
                },
                modelVersion: {
                    bsonType: 'string',
                    description: 'AI model version used'
                },
                timestamp: {
                    bsonType: 'date',
                    description: 'When the analysis was performed'
                },
                status: {
                    bsonType: 'string',
                    enum: ['PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'],
                    description: 'Analysis status'
                }
            }
        }
    }
});

// File Metadata Collection
db.createCollection('file_metadata', {
    validator: {
        $jsonSchema: {
            bsonType: 'object',
            required: ['fileId', 'originalFilename', 'mimeType', 'fileSize', 'uploadedBy', 'uploadDate'],
            properties: {
                fileId: {
                    bsonType: 'string',
                    description: 'Unique file identifier'
                },
                originalFilename: {
                    bsonType: 'string',
                    description: 'Original filename'
                },
                storedFilename: {
                    bsonType: 'string',
                    description: 'Stored filename'
                },
                mimeType: {
                    bsonType: 'string',
                    description: 'MIME type of the file'
                },
                fileSize: {
                    bsonType: 'long',
                    description: 'File size in bytes'
                },
                fileHash: {
                    bsonType: 'string',
                    description: 'SHA-256 hash of the file'
                },
                caseId: {
                    bsonType: 'string',
                    description: 'Associated case ID'
                },
                uploadedBy: {
                    bsonType: 'string',
                    description: 'User who uploaded the file'
                },
                uploadDate: {
                    bsonType: 'date',
                    description: 'When the file was uploaded'
                },
                metadata: {
                    bsonType: 'object',
                    description: 'Additional file metadata (EXIF, etc.)'
                },
                virusScanStatus: {
                    bsonType: 'string',
                    enum: ['PENDING', 'CLEAN', 'INFECTED', 'ERROR'],
                    description: 'Virus scan status'
                },
                virusScanDate: {
                    bsonType: 'date',
                    description: 'When virus scan was performed'
                },
                accessCount: {
                    bsonType: 'int',
                    description: 'Number of times file was accessed'
                },
                lastAccessed: {
                    bsonType: 'date',
                    description: 'When file was last accessed'
                }
            }
        }
    }
});

// Create indexes for performance
print('Creating indexes...');

// Audit Events indexes
db.audit_events.createIndex({ 'eventId': 1 }, { unique: true });
db.audit_events.createIndex({ 'timestamp': -1 });
db.audit_events.createIndex({ 'userId': 1, 'timestamp': -1 });
db.audit_events.createIndex({ 'eventType': 1, 'timestamp': -1 });
db.audit_events.createIndex({ 'resourceType': 1, 'resourceId': 1 });
db.audit_events.createIndex({ 'success': 1, 'timestamp': -1 });
db.audit_events.createIndex({ 'chainIndex': 1 });

// System Events indexes
db.system_events.createIndex({ 'eventId': 1 }, { unique: true });
db.system_events.createIndex({ 'timestamp': -1 });
db.system_events.createIndex({ 'component': 1, 'timestamp': -1 });
db.system_events.createIndex({ 'severity': 1, 'timestamp': -1 });
db.system_events.createIndex({ 'eventType': 1, 'timestamp': -1 });

// Analysis Metadata indexes
db.analysis_metadata.createIndex({ 'analysisId': 1 }, { unique: true });
db.analysis_metadata.createIndex({ 'userId': 1, 'timestamp': -1 });
db.analysis_metadata.createIndex({ 'caseId': 1, 'timestamp': -1 });
db.analysis_metadata.createIndex({ 'analysisType': 1, 'timestamp': -1 });
db.analysis_metadata.createIndex({ 'status': 1, 'timestamp': -1 });

// File Metadata indexes
db.file_metadata.createIndex({ 'fileId': 1 }, { unique: true });
db.file_metadata.createIndex({ 'caseId': 1, 'uploadDate': -1 });
db.file_metadata.createIndex({ 'uploadedBy': 1, 'uploadDate': -1 });
db.file_metadata.createIndex({ 'fileHash': 1 });
db.file_metadata.createIndex({ 'mimeType': 1 });
db.file_metadata.createIndex({ 'virusScanStatus': 1 });

// Create TTL indexes for automatic cleanup (optional)
// Keep audit events for 7 years
db.audit_events.createIndex({ 'timestamp': 1 }, { expireAfterSeconds: 220752000 }); // 7 years
// Keep system events for 1 year
db.system_events.createIndex({ 'timestamp': 1 }, { expireAfterSeconds: 31536000 }); // 1 year

// Create initial audit chain entry
print('Creating initial audit chain entry...');

const initialEvent = {
    eventId: 'initial-chain-entry',
    eventType: 'SYSTEM_EVENT',
    eventCategory: 'SYSTEM_START',
    action: 'DATABASE_INITIALIZATION',
    details: {
        message: 'MongoDB audit database initialized',
        version: '1.0.0',
        timestamp: new Date()
    },
    success: true,
    timestamp: new Date(),
    previousHash: null,
    currentHash: 'initial-hash-placeholder',
    chainIndex: 0
};

db.audit_events.insertOne(initialEvent);

// Create initial system event
const initialSystemEvent = {
    eventId: 'system-init-event',
    eventType: 'STARTUP',
    severity: 'INFO',
    component: 'DATABASE',
    message: 'MongoDB audit database initialized successfully',
    details: {
        collections: ['audit_events', 'system_events', 'analysis_metadata', 'file_metadata'],
        indexes: 'Created',
        version: '1.0.0'
    },
    timestamp: new Date(),
    hostname: 'mongodb',
    processId: 1
};

db.system_events.insertOne(initialSystemEvent);

// Create views for common queries
print('Creating views...');

// Recent audit events view
db.createView('recent_audit_events', 'audit_events', [
    {
        $match: {
            timestamp: {
                $gte: new Date(Date.now() - 24 * 60 * 60 * 1000) // Last 24 hours
            }
        }
    },
    {
        $sort: { timestamp: -1 }
    },
    {
        $limit: 1000
    }
]);

// Failed events view
db.createView('failed_events', 'audit_events', [
    {
        $match: {
            success: false
        }
    },
    {
        $sort: { timestamp: -1 }
    }
]);

// User activity summary view
db.createView('user_activity_summary', 'audit_events', [
    {
        $group: {
            _id: '$userId',
            totalEvents: { $sum: 1 },
            successfulEvents: {
                $sum: { $cond: ['$success', 1, 0] }
            },
            failedEvents: {
                $sum: { $cond: ['$success', 0, 1] }
            },
            lastActivity: { $max: '$timestamp' },
            eventTypes: { $addToSet: '$eventType' }
        }
    },
    {
        $sort: { lastActivity: -1 }
    }
]);

// Create aggregation pipeline for audit chain verification
print('Creating audit chain verification function...');

db.system.js.save({
    _id: 'verifyAuditChain',
    value: function() {
        const events = db.audit_events.find().sort({ chainIndex: 1 }).toArray();
        let previousHash = null;
        let isValid = true;
        let errors = [];
        
        for (let i = 0; i < events.length; i++) {
            const event = events[i];
            
            // Verify chain index
            if (event.chainIndex !== i) {
                isValid = false;
                errors.push(`Event ${event.eventId} has incorrect chain index: expected ${i}, got ${event.chainIndex}`);
            }
            
            // Verify previous hash
            if (event.previousHash !== previousHash) {
                isValid = false;
                errors.push(`Event ${event.eventId} has incorrect previous hash`);
            }
            
            // Calculate current hash (simplified - in production use proper hash calculation)
            const currentHash = `hash-${event.eventId}-${event.timestamp.getTime()}`;
            if (event.currentHash !== currentHash) {
                isValid = false;
                errors.push(`Event ${event.eventId} has incorrect current hash`);
            }
            
            previousHash = event.currentHash;
        }
        
        return {
            isValid: isValid,
            totalEvents: events.length,
            errors: errors
        };
    }
});

// Create user for application access
print('Creating application user...');

db.createUser({
    user: 'forensic_app',
    pwd: 'forensic_mongo_app_password_2024',
    roles: [
        {
            role: 'readWrite',
            db: 'forensic_audit'
        }
    ]
});

print('MongoDB initialization completed successfully!');
print('Collections created: audit_events, system_events, analysis_metadata, file_metadata');
print('Indexes created for optimal performance');
print('Views created: recent_audit_events, failed_events, user_activity_summary');
print('Application user created: forensic_app');
print('Initial audit chain entry created');
