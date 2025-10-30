package com.forensic.case.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ReportRequest {
    @NotNull
    private Long caseId;

    private String reportType = "STANDARD"; // STANDARD, DETAILED, SUMMARY

    private List<String> includeSections; // COMPARISONS, METADATA, CHAIN_OF_CUSTODY, etc.

    private String outputFormat = "PDF"; // PDF, HTML, XML

    private boolean includeDigitalSignature = true;

    private boolean includeQRCode = true;

    private String language = "en"; // en, tr, etc.

    private String template = "default"; // default, custom, etc.
}
