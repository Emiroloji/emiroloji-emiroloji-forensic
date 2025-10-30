package com.forensic.case.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class QRCodeService {

    private static final int QR_CODE_SIZE = 300;
    private static final String QR_CODE_FORMAT = "PNG";

    public byte[] generateQRCode(String data) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, QR_CODE_FORMAT, outputStream);

            return outputStream.toByteArray();

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Error generating QR code: " + e.getMessage(), e);
        }
    }

    public String generateVerificationQRCode(String caseId, String reportId, String verificationHash) {
        try {
            // Create verification data
            StringBuilder qrData = new StringBuilder();
            qrData.append("FORENSIC_REPORT_VERIFICATION|");
            qrData.append("CASE_ID:").append(caseId).append("|");
            qrData.append("REPORT_ID:").append(reportId).append("|");
            qrData.append("HASH:").append(verificationHash).append("|");
            qrData.append("TIMESTAMP:").append(System.currentTimeMillis());

            // Generate QR code
            return Base64.getEncoder().encodeToString(generateQRCode(qrData.toString()));

        } catch (Exception e) {
            throw new RuntimeException("Error generating verification QR code: " + e.getMessage(), e);
        }
    }

    public String generateChainOfCustodyQRCode(String caseId, String action, String userId, String timestamp) {
        try {
            // Create chain of custody data
            StringBuilder qrData = new StringBuilder();
            qrData.append("CHAIN_OF_CUSTODY|");
            qrData.append("CASE_ID:").append(caseId).append("|");
            qrData.append("ACTION:").append(action).append("|");
            qrData.append("USER_ID:").append(userId).append("|");
            qrData.append("TIMESTAMP:").append(timestamp).append("|");

            // Generate hash for integrity
            String dataToHash = qrData.toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
            String hashString = Base64.getEncoder().encodeToString(hash);

            qrData.append("HASH:").append(hashString);

            // Generate QR code
            return Base64.getEncoder().encodeToString(generateQRCode(qrData.toString()));

        } catch (Exception e) {
            throw new RuntimeException("Error generating chain of custody QR code: " + e.getMessage(), e);
        }
    }

    public boolean verifyQRCode(String qrCodeData, String expectedHash) {
        try {
            // Decode QR code data
            byte[] decodedData = Base64.getDecoder().decode(qrCodeData);
            String qrContent = new String(decodedData, StandardCharsets.UTF_8);

            // Extract hash from QR code content
            String[] parts = qrContent.split("\\|");
            String actualHash = null;

            for (String part : parts) {
                if (part.startsWith("HASH:")) {
                    actualHash = part.substring(5);
                    break;
                }
            }

            // Verify hash
            return expectedHash.equals(actualHash);

        } catch (Exception e) {
            return false;
        }
    }

    public String extractCaseIdFromQRCode(String qrCodeData) {
        try {
            // Decode QR code data
            byte[] decodedData = Base64.getDecoder().decode(qrCodeData);
            String qrContent = new String(decodedData, StandardCharsets.UTF_8);

            // Extract case ID from QR code content
            String[] parts = qrContent.split("\\|");
            for (String part : parts) {
                if (part.startsWith("CASE_ID:")) {
                    return part.substring(8);
                }
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }

    public String extractReportIdFromQRCode(String qrCodeData) {
        try {
            // Decode QR code data
            byte[] decodedData = Base64.getDecoder().decode(qrCodeData);
            String qrContent = new String(decodedData, StandardCharsets.UTF_8);

            // Extract report ID from QR code content
            String[] parts = qrContent.split("\\|");
            for (String part : parts) {
                if (part.startsWith("REPORT_ID:")) {
                    return part.substring(10);
                }
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }
}
