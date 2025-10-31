package com.forensic.case.controller;

import com.forensic.case.dto.ReportRequest;
import com.forensic.case.dto.ReportResponse;
import com.forensic.case.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping("/generate")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> generateReport(@Valid @RequestBody ReportRequest request) {
        try {
            ReportResponse response = reportService.generateReport(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/generate/download")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<byte[]> generateAndDownloadReport(@Valid @RequestBody ReportRequest request) {
        try {
            ReportResponse response = reportService.generateReport(request);
            
            // Set headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                "forensic_report_" + request.getCaseId() + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
            headers.setContentLength(response.getReportData().length);
            
            return new ResponseEntity<>(response.getReportData(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/verify/{reportId}")
    public ResponseEntity<Boolean> verifyReport(@PathVariable String reportId, 
                                               @RequestParam String verificationHash) {
        try {
            boolean isValid = reportService.verifyReport(reportId, verificationHash);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/qr-code/{reportId}")
    public ResponseEntity<byte[]> getQRCode(@PathVariable String reportId) {
        try {
            // In a real implementation, you would retrieve the QR code from storage
            // For now, return a placeholder
            byte[] qrCodeData = "QR_CODE_PLACEHOLDER".getBytes();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrCodeData.length);
            
            return new ResponseEntity<>(qrCodeData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/signature/{reportId}")
    public ResponseEntity<byte[]> getDigitalSignature(@PathVariable String reportId) {
        try {
            // In a real implementation, you would retrieve the signature from storage
            // For now, return a placeholder
            byte[] signatureData = "SIGNATURE_PLACEHOLDER".getBytes();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(signatureData.length);
            
            return new ResponseEntity<>(signatureData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
