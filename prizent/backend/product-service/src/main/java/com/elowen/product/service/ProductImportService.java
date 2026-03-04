package com.elowen.product.service;

import com.elowen.product.client.AdminServiceClient;
import com.elowen.product.dto.*;
import com.elowen.product.entity.Product;
import com.elowen.product.entity.ProductMarketplaceMapping;
import com.elowen.product.repository.ProductMarketplaceMappingRepository;
import com.elowen.product.repository.ProductRepository;
import com.elowen.product.security.UserPrincipal;
import com.elowen.product.util.ExcelParserUtil;
import com.elowen.product.validator.ProductImportValidator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Service for bulk product import.
 *
 * <h3>Template generation</h3>
 * Builds an Excel (.xlsx) template that includes all standard product columns
 * plus one column per active custom field fetched from admin-service.
 *
 * <h3>Import processing</h3>
 * <ol>
 *   <li>Parse uploaded file (xlsx / xls / csv)</li>
 *   <li>Validate each row – collect errors per row</li>
 *   <li>Batch-insert valid rows (100 per transaction chunk)</li>
 *   <li>Save custom-field values via admin-service</li>
 *   <li>Save marketplace mappings</li>
 *   <li>Return {@link ImportResultDTO} with success / fail counts and per-row errors</li>
 * </ol>
 */
@Service
public class ProductImportService {

    private static final Logger log = LoggerFactory.getLogger(ProductImportService.class);
    private static final int BATCH_SIZE = 100;

    private final ProductRepository                  productRepository;
    private final ProductMarketplaceMappingRepository mappingRepository;
    private final AdminServiceClient                 adminServiceClient;
    private final ExcelParserUtil                    excelParserUtil;

    @Autowired
    public ProductImportService(ProductRepository productRepository,
                                ProductMarketplaceMappingRepository mappingRepository,
                                AdminServiceClient adminServiceClient,
                                ExcelParserUtil excelParserUtil) {
        this.productRepository  = productRepository;
        this.mappingRepository  = mappingRepository;
        this.adminServiceClient = adminServiceClient;
        this.excelParserUtil    = excelParserUtil;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Template generation
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Generate and return an Excel template (.xlsx) as a byte array.
     * Custom fields configured in admin-service are appended as extra columns.
     */
    public byte[] generateImportTemplate(String authToken) throws IOException {
        // Fetch custom field definitions (non-fatal if admin-service is down)
        List<Map<String, Object>> cfDefs =
                adminServiceClient.getCustomFieldDefinitions("p", authToken);

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet productsSheet = workbook.createSheet("Products");
            Sheet infoSheet     = workbook.createSheet("Instructions");

            buildProductsSheet(workbook, productsSheet, cfDefs);
            buildInstructionsSheet(infoSheet, cfDefs);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void buildProductsSheet(XSSFWorkbook wb, Sheet sheet,
                                    List<Map<String, Object>> cfDefs) {

        // ── Header style (dark background, white bold text) ───────────────────
        CellStyle headerStyle = wb.createCellStyle();
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Required column style (orange background)
        CellStyle requiredStyle = wb.createCellStyle();
        requiredStyle.cloneStyleFrom(headerStyle);
        requiredStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());

        // Example row style (normal black)
        CellStyle exampleStyle = wb.createCellStyle();
        Font exampleFont = wb.createFont();
        exampleFont.setItalic(false);
        exampleFont.setColor(IndexedColors.BLACK.getIndex());
        exampleStyle.setFont(exampleFont);

        // ── Build column list ─────────────────────────────────────────────────
        List<String> headers  = new ArrayList<>(ExcelParserUtil.STANDARD_HEADERS);
        List<Object> examples = new ArrayList<>(Arrays.asList(
                "My Product",       // Product Name*
                "PN-001",           // Product Number
                "SC-001",           // Style Code
                "SKU-001",          // SKU Code*
                1L,                 // Brand ID*
                1L,                 // Category ID*
                999.00,             // MRP
                750.00,             // Product Cost
                899.00,             // Proposed Selling Price (Sales)
                950.00,             // Proposed Selling Price (Non-Sales)
                "true",             // Enabled (true/false)
                "1,2"               // Marketplace IDs (comma-sep)
        ));

        // Add dynamic custom-field columns
        for (Map<String, Object> cf : cfDefs) {
            String name = (String) cf.get("name");
            if (name != null && !name.isBlank()) {
                headers.add(ExcelParserUtil.CF_PREFIX + name);
                examples.add("sample value");
            }
        }

        // ── Row 0: Header row ─────────────────────────────────────────────────
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(20);

        Set<String> requiredCols = Set.of(
                ExcelParserUtil.COL_PRODUCT_NAME,
                ExcelParserUtil.COL_SKU_CODE,
                ExcelParserUtil.COL_BRAND_ID,
                ExcelParserUtil.COL_CATEGORY_ID
        );

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(requiredCols.contains(headers.get(i)) ? requiredStyle : headerStyle);
            sheet.setColumnWidth(i, 7000);
        }

        // ── Row 1: Example data row ───────────────────────────────────────────
        Row exampleRow = sheet.createRow(1);
        for (int i = 0; i < examples.size(); i++) {
            Cell cell = exampleRow.createCell(i);
            Object val = examples.get(i);
            if (val instanceof Number) {
                cell.setCellValue(((Number) val).doubleValue());
            } else {
                cell.setCellValue(val != null ? val.toString() : "");
            }
            cell.setCellStyle(exampleStyle);
        }

        // Freeze the header row
        sheet.createFreezePane(0, 1);
    }

    private void buildInstructionsSheet(Sheet sheet,
                                         List<Map<String, Object>> cfDefs) {
        int ri = 0;
        addInfoRow(sheet, ri++, "=== Product Import Instructions ===");
        addInfoRow(sheet, ri++, "Fill product data in the 'Products' sheet starting from row 3.");
        addInfoRow(sheet, ri++, "Row 2 (orange headers) = required fields.  Row 2 (blue) = optional.");
        addInfoRow(sheet, ri++, "");
        addInfoRow(sheet, ri++, "COLUMN GUIDE:");
        addInfoRow(sheet, ri++, "  Product Name*        : Full product name (max 255 chars)");
        addInfoRow(sheet, ri++, "  Product Number       : Optional product number");
        addInfoRow(sheet, ri++, "  Style Code           : Optional style code");
        addInfoRow(sheet, ri++, "  SKU Code*            : Unique stock-keeping unit code");
        addInfoRow(sheet, ri++, "  Brand ID*            : Numeric ID of the brand (from Brands page)");
        addInfoRow(sheet, ri++, "  Category ID*         : Numeric ID of the category (from Categories page)");
        addInfoRow(sheet, ri++, "  MRP                  : Maximum Retail Price (>= 0)");
        addInfoRow(sheet, ri++, "  Product Cost         : Cost price (>= 0)");
        addInfoRow(sheet, ri++, "  Proposed SP (Sales)  : Proposed Selling Price for sales period");
        addInfoRow(sheet, ri++, "  Proposed SP (Non-S.) : Proposed Selling Price outside sales");
        addInfoRow(sheet, ri++, "  Enabled              : true / false  (default: true)");
        addInfoRow(sheet, ri++, "  Marketplace IDs      : Comma-separated marketplace IDs e.g. 1,2,3");

        if (!cfDefs.isEmpty()) {
            addInfoRow(sheet, ri++, "");
            addInfoRow(sheet, ri++, "CUSTOM FIELDS (CF: prefix columns):");
            for (Map<String, Object> cf : cfDefs) {
                String name  = (String) cf.get("name");
                String type  = (String) cf.get("fieldType");
                String desc  = (name != null ? name : "?") + "  [type: " + (type != null ? type : "TEXT") + "]";
                addInfoRow(sheet, ri++, "  CF:" + desc);
            }
        }

        sheet.setColumnWidth(0, 20000);
    }

    private void addInfoRow(Sheet sheet, int rowIndex, String text) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(text);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Import processing
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Parse, validate, and bulk-save products from an uploaded file.
     *
     * @param file      the uploaded .xlsx / .xls / .csv file
     * @param authToken forward to admin-service for custom-field operations
     * @return summary with success / fail counts and per-row errors
     */
    @Transactional
    public ImportResultDTO importProducts(MultipartFile file, String authToken) throws IOException {
        log.info("Starting bulk product import: file={}, size={}",
                file.getOriginalFilename(), file.getSize());

        // ── 1. Fetch custom field definitions for CF-column mapping ────────────
        List<Map<String, Object>> cfDefs =
                adminServiceClient.getCustomFieldDefinitions("p", authToken);
        Map<String, Long> cfNameToId = buildCfNameToIdMap(cfDefs);

        // ── 2. Resolve current user / tenant ──────────────────────────────────
        UserPrincipal principal = getCurrentUserPrincipal();
        Integer clientId = principal.getClientId();
        Long    userId   = principal.getId();

        // ── 3. Parse the uploaded file ─────────────────────────────────────────
        ExcelParserUtil.ParseResult parseResult = excelParserUtil.parseFile(file);
        List<ImportRowDTO>   rows      = parseResult.getRows();
        List<ImportRowError> allErrors = new ArrayList<>(parseResult.getErrors());

        log.info("Parsed {} rows, {} parse errors", rows.size(), allErrors.size());

        // ── 4. Row-level validation ────────────────────────────────────────────
        ProductImportValidator validator = new ProductImportValidator(productRepository, clientId);
        Set<String>            seenSkus  = new HashSet<>();
        List<ImportRowDTO>     validRows = new ArrayList<>();

        for (ImportRowDTO row : rows) {
            List<ImportRowError> rowErrors = validator.validate(row, seenSkus);
            if (rowErrors.isEmpty()) {
                seenSkus.add(normalise(row.getSkuCode()));
                validRows.add(row);
            } else {
                allErrors.addAll(rowErrors);
            }
        }

        log.info("{} valid rows after validation, {} rows failed", validRows.size(),
                rows.size() - validRows.size());

        // ── 5. Batch insert ────────────────────────────────────────────────────
        int successCount = 0;
        List<List<ImportRowDTO>> batches = partition(validRows, BATCH_SIZE);

        for (List<ImportRowDTO> batch : batches) {
            try {
                successCount += saveBatch(batch, clientId, userId, cfNameToId, authToken, allErrors);
            } catch (Exception e) {
                log.error("Unexpected error saving batch starting row {}: {}",
                        batch.get(0).getRowNumber(), e.getMessage(), e);
                for (ImportRowDTO row : batch) {
                    allErrors.add(new ImportRowError(row.getRowNumber(), "Save",
                            "Batch save failed: " + e.getMessage()));
                }
            }
        }

        int failedCount = rows.size() - successCount;
        log.info("Import complete: {} succeeded, {} failed", successCount, failedCount);
        return new ImportResultDTO(successCount, failedCount, rows.size(), allErrors);
    }

    // ── Batch save ────────────────────────────────────────────────────────────

    @Transactional
    protected int saveBatch(List<ImportRowDTO> batch,
                             Integer clientId,
                             Long userId,
                             Map<String, Long> cfNameToId,
                             String authToken,
                             List<ImportRowError> allErrors) {

        // Build Product entities
        List<Product> entities = new ArrayList<>();
        for (ImportRowDTO row : batch) {
            entities.add(buildProduct(row, clientId, userId));
        }

        // Batch-insert all products in this chunk
        List<Product> saved = productRepository.saveAll(entities);

        int savedCount = 0;
        for (int i = 0; i < batch.size(); i++) {
            ImportRowDTO row     = batch.get(i);
            Product      product = saved.get(i);
            savedCount++;

            // Save custom field values
            saveCustomFields(row, product.getId(), cfNameToId, authToken, row.getRowNumber(), allErrors);

            // Save marketplace mappings
            saveMarketplaceMappings(row, product, clientId, userId, row.getRowNumber(), allErrors);
        }
        return savedCount;
    }

    private void saveCustomFields(ImportRowDTO row, Long productId,
                                   Map<String, Long> cfNameToId, String authToken,
                                   int rowNum, List<ImportRowError> allErrors) {
        if (row.getCustomFieldValues() == null || row.getCustomFieldValues().isEmpty()) return;

        List<CustomFieldValueRequest> cfRequests = new ArrayList<>();
        for (Map.Entry<String, String> entry : row.getCustomFieldValues().entrySet()) {
            Long fieldId = cfNameToId.get(entry.getKey());
            if (fieldId == null) {
                log.debug("Row {}: No fieldId found for custom field '{}'", rowNum, entry.getKey());
                continue;
            }
            cfRequests.add(new CustomFieldValueRequest(fieldId, entry.getValue()));
        }

        if (!cfRequests.isEmpty()) {
            try {
                adminServiceClient.bulkSaveCustomFieldValues("p", productId, cfRequests, authToken);
            } catch (Exception e) {
                log.warn("Row {}: Failed to save custom fields for productId {}: {}",
                        rowNum, productId, e.getMessage());
                // Non-fatal – product was saved; log a warning-level error for the user
                allErrors.add(new ImportRowError(rowNum, "Custom Fields",
                        "Product saved but custom fields could not be stored: " + e.getMessage()));
            }
        }
    }

    private void saveMarketplaceMappings(ImportRowDTO row, Product product,
                                          Integer clientId, Long userId,
                                          int rowNum, List<ImportRowError> allErrors) {
        if (row.getMarketplaceIds() == null || row.getMarketplaceIds().isEmpty()) return;

        for (Long marketplaceId : row.getMarketplaceIds()) {
            try {
                ProductMarketplaceMapping mapping = new ProductMarketplaceMapping(
                        clientId.longValue(),
                        product.getId(),
                        product.getName(),
                        marketplaceId,
                        "",    // marketplaceName – enriched async / lookup can be added later
                        "",    // productMarketplaceName
                        userId
                );
                mappingRepository.save(mapping);
            } catch (Exception e) {
                log.warn("Row {}: Failed to save marketplace mapping {}: {}",
                        rowNum, marketplaceId, e.getMessage());
                allErrors.add(new ImportRowError(rowNum, "Marketplace IDs",
                        "Marketplace mapping " + marketplaceId + " could not be saved: " + e.getMessage()));
            }
        }
    }

    // ── Entity builder ────────────────────────────────────────────────────────

    private Product buildProduct(ImportRowDTO row, Integer clientId, Long userId) {

        String normalizedName = row.getName().trim();
        String normalizedSku  = normalise(row.getSkuCode());

        Product p = new Product(
                clientId,
                normalizedName,
                row.getBrandId(),
                normalizedSku,
                row.getCategoryId(),
                nvl(row.getMrp()),
                nvl(row.getProductCost()),
                nvl(row.getProposedSellingPriceSales()),
                nvl(row.getProposedSellingPriceNonSales()),
                userId
        );
        p.setEnabled(row.isEnabled());
        if (row.getProductNumber() != null && !row.getProductNumber().isBlank()) {
            p.setProductNumber(row.getProductNumber().trim());
        }
        if (row.getStyleCode() != null && !row.getStyleCode().isBlank()) {
            p.setStyleCode(row.getStyleCode().trim());
        }
        return p;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Map<String, Long> buildCfNameToIdMap(List<Map<String, Object>> cfDefs) {
        Map<String, Long> map = new HashMap<>();
        for (Map<String, Object> cf : cfDefs) {
            String name = (String) cf.get("name");
            Number id   = (Number) cf.get("id");
            if (name != null && id != null) {
                map.put(name.trim(), id.longValue());
            }
        }
        return map;
    }

    private <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> parts = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            parts.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return parts;
    }

    private String normalise(String sku) {
        return sku == null ? "" : sku.trim().toUpperCase();
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private UserPrincipal getCurrentUserPrincipal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal up) {
            return up;
        }
        // Fallback for testing (security is permissive on /api/products/**)
        return new UserPrincipal(1L, 1, "import-user", "ADMIN");
    }
}
