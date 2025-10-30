package com.forensic.case.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {
    private String reportId;
    private Long caseId;
    private LocalDateTime generatedAt;
    private byte[] reportData;
    private String verificationHash;
    private byte[] qrCodeData;
    private byte[] digitalSignature;
    private String reportType;
    private String outputFormat;
    private boolean isSigned;
    private boolean hasQRCode;
    private String fileName;
    private long fileSize;
    private String mimeType;
}
