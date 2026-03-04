package com.elowen.product.controller;

import com.elowen.product.dto.ImportResultDTO;
import com.elowen.product.service.ProductImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * REST controller for the bulk product import feature.
 *
 * <pre>
 *   GET  /api/products/import/template  → download Excel template (.xlsx)
 *   POST /api/products/import           → upload Excel/CSV, returns import result
 * </pre>
 */
@RestController
@RequestMapping("/api/products/import")
@CrossOrigin(origins = "*")
public class ProductImportController {

    private static final Logger log = LoggerFactory.getLogger(ProductImportController.class);

    private final ProductImportService importService;

    @Autowired
    public ProductImportController(ProductImportService importService) {
        this.importService = importService;
    }

    // ── GET /api/products/import/template ─────────────────────────────────────

    /**
     * Generate and return an Excel template (.xlsx) that the user fills with
     * product data and uploads back.
     *
     * <p>Dynamic custom-field columns are appended automatically based on what
     * is configured in admin-service for module {@code "p"}.
     *
     * @param authToken optional Bearer token forwarded to admin-service
     * @return binary .xlsx file attachment
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate(
            @RequestHeader(value = "Authorization", required = false) String authToken) {

        try {
            byte[] templateBytes = importService.generateImportTemplate(authToken);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename("product_import_template.xlsx")
                            .build());
            headers.setContentLength(templateBytes.length);

            log.info("Excel import template generated ({} bytes)", templateBytes.length);
            return ResponseEntity.ok().headers(headers).body(templateBytes);

        } catch (IOException e) {
            log.error("Failed to generate import template: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ── POST /api/products/import ─────────────────────────────────────────────

    /**
     * Accept an uploaded product file (.xlsx / .xls / .csv), parse it, validate
     * each row, and bulk-insert valid products.
     *
     * <p>Returns an {@link ImportResultDTO} regardless of partial failures so the
     * user can see which rows succeeded and which failed with reasons.
     *
     * @param file      multipart upload (required)
     * @param authToken JWT forwarded to admin-service for custom-field saving
     * @return JSON summary:
     *   {@code { totalRows, successCount, failedCount, errors: [{rowNumber, field, message}] }}
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importProducts(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authToken) {

        // Basic pre-checks
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No file uploaded. Please attach a file."));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.toLowerCase().endsWith(".xlsx")
                && !filename.toLowerCase().endsWith(".xls")
                && !filename.toLowerCase().endsWith(".csv"))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "Unsupported file format. Please upload a .xlsx, .xls, or .csv file."));
        }

        // File size guard (frontend already limits to 5 MB; we allow 50 MB server-side)
        long maxBytes = 50L * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File exceeds maximum allowed size of 50 MB."));
        }

        log.info("Product import request received: file='{}', size={} bytes", filename, file.getSize());

        try {
            ImportResultDTO result = importService.importProducts(file, authToken);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Import rejected – bad input: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            log.error("IO error during product import: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to read the uploaded file: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during product import: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Import failed: " + e.getMessage()));
        }
    }
}
