package com.forensic.storage.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

/**
 * Metadata Extraction Service
 * 
 * This service extracts metadata from uploaded files including:
 * - EXIF data from images
 * - File properties
 * - Technical specifications
 * - Security information
 */
@Service
public class MetadataExtractionService {

    private static final Logger logger = LoggerFactory.getLogger(MetadataExtractionService.class);

    private final Tika tika = new Tika();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extract metadata from uploaded file
     * 
     * @param file Uploaded file
     * @return JSON string containing extracted metadata
     */
    public String extractMetadata(MultipartFile file) {
        try {
            ObjectNode metadata = objectMapper.createObjectNode();

            // Basic file information
            extractBasicMetadata(file, metadata);

            // Content type specific metadata
            String contentType = file.getContentType();
            if (contentType != null) {
                if (contentType.startsWith("image/")) {
                    extractImageMetadata(file, metadata);
                } else if (contentType.startsWith("video/")) {
                    extractVideoMetadata(file, metadata);
                } else if (contentType.equals("application/pdf")) {
                    extractPdfMetadata(file, metadata);
                }
            }

            // Tika metadata extraction
            extractTikaMetadata(file, metadata);

            // Security metadata
            extractSecurityMetadata(file, metadata);

            return objectMapper.writeValueAsString(metadata);

        } catch (Exception e) {
            logger.error("Failed to extract metadata: {}", e.getMessage());
            return "{}"; // Return empty JSON object on error
        }
    }

    /**
     * Extract basic file metadata
     */
    private void extractBasicMetadata(MultipartFile file, ObjectNode metadata) {
        metadata.put("originalFilename", file.getOriginalFilename());
        metadata.put("contentType", file.getContentType());
        metadata.put("size", file.getSize());
        metadata.put("extractionDate", LocalDateTime.now().toString());

        // File extension
        if (file.getOriginalFilename() != null) {
            String filename = file.getOriginalFilename();
            int lastDotIndex = filename.lastIndexOf('.');
            if (lastDotIndex != -1) {
                metadata.put("extension", filename.substring(lastDotIndex + 1).toLowerCase());
            }
        }
    }

    /**
     * Extract image-specific metadata
     */
    private void extractImageMetadata(MultipartFile file, ObjectNode metadata) {
        try {
            byte[] fileContent = file.getBytes();

            // Use ImageIO to get image metadata
            try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(fileContent))) {
                Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);

                if (readers.hasNext()) {
                    ImageReader reader = readers.next();
                    reader.setInput(iis);

                    // Get image dimensions
                    int width = reader.getWidth(0);
                    int height = reader.getHeight(0);
                    metadata.put("imageWidth", width);
                    metadata.put("imageHeight", height);
                    metadata.put("imageAspectRatio", (double) width / height);

                    // Get image metadata
                    IIOMetadata imageMetadata = reader.getImageMetadata(0);
                    if (imageMetadata != null) {
                        String[] metadataFormats = imageMetadata.getMetadataFormatNames();
                        if (metadataFormats.length > 0) {
                            metadata.put("metadataFormat", metadataFormats[0]);
                        }
                    }

                    // Get image type
                    BufferedImage image = reader.read(0);
                    if (image != null) {
                        metadata.put("imageType", image.getType());
                        metadata.put("colorModel", image.getColorModel().getClass().getSimpleName());
                        metadata.put("numColorComponents", image.getColorModel().getNumColorComponents());
                        metadata.put("hasAlpha", image.getColorModel().hasAlpha());
                    }

                    reader.dispose();
                }
            }

            // Extract EXIF data using Tika
            extractExifData(file, metadata);

        } catch (Exception e) {
            logger.warn("Failed to extract image metadata: {}", e.getMessage());
        }
    }

    /**
     * Extract EXIF data from image
     */
    private void extractExifData(MultipartFile file, ObjectNode metadata) {
        try {
            byte[] fileContent = file.getBytes();

            // Use Tika to extract EXIF data
            Metadata tikaMetadata = new Metadata();
            Parser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler();
            ParseContext context = new ParseContext();

            parser.parse(new ByteArrayInputStream(fileContent), handler, tikaMetadata, context);

            // Extract relevant EXIF fields
            String[] exifFields = {
                    "EXIF:DateTime", "EXIF:DateTimeOriginal", "EXIF:DateTimeDigitized",
                    "EXIF:Make", "EXIF:Model", "EXIF:Software",
                    "EXIF:ExposureTime", "EXIF:FNumber", "EXIF:ISO",
                    "EXIF:FocalLength", "EXIF:Flash", "EXIF:WhiteBalance",
                    "EXIF:GPSLatitude", "EXIF:GPSLongitude", "EXIF:GPSAltitude"
            };

            ObjectNode exifData = objectMapper.createObjectNode();
            for (String field : exifFields) {
                String value = tikaMetadata.get(field);
                if (value != null) {
                    exifData.put(field, value);
                }
            }

            if (exifData.size() > 0) {
                metadata.set("exifData", exifData);
            }

        } catch (Exception e) {
            logger.warn("Failed to extract EXIF data: {}", e.getMessage());
        }
    }

    /**
     * Extract video-specific metadata
     */
    private void extractVideoMetadata(MultipartFile file, ObjectNode metadata) {
        try {
            // Basic video metadata extraction
            // In a real implementation, you would use FFmpeg or similar library

            metadata.put("mediaType", "video");
            metadata.put("extractionMethod", "basic");

            // Placeholder for video-specific metadata
            ObjectNode videoData = objectMapper.createObjectNode();
            videoData.put("duration", "unknown");
            videoData.put("frameRate", "unknown");
            videoData.put("resolution", "unknown");
            videoData.put("codec", "unknown");

            metadata.set("videoData", videoData);

        } catch (Exception e) {
            logger.warn("Failed to extract video metadata: {}", e.getMessage());
        }
    }

    /**
     * Extract PDF-specific metadata
     */
    private void extractPdfMetadata(MultipartFile file, ObjectNode metadata) {
        try {
            byte[] fileContent = file.getBytes();

            // Use Tika to extract PDF metadata
            Metadata tikaMetadata = new Metadata();
            Parser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler();
            ParseContext context = new ParseContext();

            parser.parse(new ByteArrayInputStream(fileContent), handler, tikaMetadata, context);

            // Extract relevant PDF fields
            String[] pdfFields = {
                    "title", "author", "subject", "keywords", "creator", "producer",
                    "creation-date", "modification-date", "pdf:PDFVersion",
                    "pdf:encrypted", "pdf:hasXFA", "pdf:hasMarkedContent"
            };

            ObjectNode pdfData = objectMapper.createObjectNode();
            for (String field : pdfFields) {
                String value = tikaMetadata.get(field);
                if (value != null) {
                    pdfData.put(field, value);
                }
            }

            if (pdfData.size() > 0) {
                metadata.set("pdfData", pdfData);
            }

        } catch (Exception e) {
            logger.warn("Failed to extract PDF metadata: {}", e.getMessage());
        }
    }

    /**
     * Extract metadata using Apache Tika
     */
    private void extractTikaMetadata(MultipartFile file, ObjectNode metadata) {
        try {
            byte[] fileContent = file.getBytes();

            // Use Tika to extract general metadata
            Metadata tikaMetadata = new Metadata();
            Parser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler();
            ParseContext context = new ParseContext();

            parser.parse(new ByteArrayInputStream(fileContent), handler, tikaMetadata, context);

            // Extract general metadata fields
            String[] generalFields = {
                    "Content-Type", "Content-Length", "Content-Encoding",
                    "X-Parsed-By", "X-TIKA:content", "X-TIKA:content_handler"
            };

            ObjectNode tikaData = objectMapper.createObjectNode();
            for (String field : generalFields) {
                String value = tikaMetadata.get(field);
                if (value != null) {
                    tikaData.put(field, value);
                }
            }

            if (tikaData.size() > 0) {
                metadata.set("tikaData", tikaData);
            }

        } catch (Exception e) {
            logger.warn("Failed to extract Tika metadata: {}", e.getMessage());
        }
    }

    /**
     * Extract security-related metadata
     */
    private void extractSecurityMetadata(MultipartFile file, ObjectNode metadata) {
        try {
            ObjectNode securityData = objectMapper.createObjectNode();

            // File size analysis
            long fileSize = file.getSize();
            securityData.put("fileSize", fileSize);
            securityData.put("sizeCategory", categorizeFileSize(fileSize));

            // Content type analysis
            String contentType = file.getContentType();
            securityData.put("contentType", contentType);
            securityData.put("isImage", contentType != null && contentType.startsWith("image/"));
            securityData.put("isVideo", contentType != null && contentType.startsWith("video/"));
            securityData.put("isDocument", contentType != null &&
                    (contentType.startsWith("application/") || contentType.startsWith("text/")));

            // Filename analysis
            String filename = file.getOriginalFilename();
            if (filename != null) {
                securityData.put("hasExtension", filename.contains("."));
                securityData.put("filenameLength", filename.length());
                securityData.put("hasSpecialChars", filename.matches(".*[^a-zA-Z0-9._-].*"));
            }

            // Risk assessment
            securityData.put("riskLevel", assessRiskLevel(file));

            metadata.set("securityData", securityData);

        } catch (Exception e) {
            logger.warn("Failed to extract security metadata: {}", e.getMessage());
        }
    }

    /**
     * Categorize file size
     */
    private String categorizeFileSize(long size) {
        if (size < 1024) {
            return "tiny";
        } else if (size < 1024 * 1024) {
            return "small";
        } else if (size < 10 * 1024 * 1024) {
            return "medium";
        } else if (size < 100 * 1024 * 1024) {
            return "large";
        } else {
            return "very_large";
        }
    }

    /**
     * Assess risk level of file
     */
    private String assessRiskLevel(MultipartFile file) {
        String contentType = file.getContentType();
        long fileSize = file.getSize();

        // High risk indicators
        if (contentType == null) {
            return "high";
        }

        if (fileSize > 100 * 1024 * 1024) { // > 100MB
            return "high";
        }

        if (contentType.startsWith("application/") &&
                !contentType.equals("application/pdf")) {
            return "medium";
        }

        // Low risk indicators
        if (contentType.startsWith("image/") || contentType.startsWith("video/")) {
            return "low";
        }

        return "medium";
    }
}
