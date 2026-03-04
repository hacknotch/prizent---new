package com.elowen.product.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO returned to the client after a bulk product import.
 *
 * Example JSON:
 * {
 *   "totalRows": 500,
 *   "successCount": 490,
 *   "failedCount": 10,
 *   "errors": [ { "rowNumber": 4, "field": "Brand ID", "message": "..." }, ... ]
 * }
 */
public class ImportResultDTO {

    private int totalRows;
    private int successCount;
    private int failedCount;
    private List<ImportRowError> errors = new ArrayList<>();

    // ── constructors ─────────────────────────────────────────────────────────

    public ImportResultDTO() {}

    public ImportResultDTO(int successCount, int failedCount, int totalRows,
                           List<ImportRowError> errors) {
        this.successCount = successCount;
        this.failedCount  = failedCount;
        this.totalRows    = totalRows;
        this.errors       = errors != null ? errors : new ArrayList<>();
    }

    // ── getters / setters ────────────────────────────────────────────────────

    public int getTotalRows()    { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }

    public int getSuccessCount() { return successCount; }
    public void setSuccessCount(int successCount) { this.successCount = successCount; }

    public int getFailedCount()  { return failedCount; }
    public void setFailedCount(int failedCount) { this.failedCount = failedCount; }

    public List<ImportRowError> getErrors() { return errors; }
    public void setErrors(List<ImportRowError> errors) { this.errors = errors; }
}
