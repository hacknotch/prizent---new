package com.elowen.product.dto;

/**
 * Represents a single validation or parse error for one row in the import file.
 * Row numbers are 1-based matching the Excel row the user sees.
 */
public class ImportRowError {

    /** 1-based Excel row number */
    private int rowNumber;

    /** Column / field name where the error was detected */
    private String field;

    /** Human-readable description of the problem */
    private String message;

    // ── constructors ─────────────────────────────────────────────────────────

    public ImportRowError() {}

    public ImportRowError(int rowNumber, String field, String message) {
        this.rowNumber = rowNumber;
        this.field     = field;
        this.message   = message;
    }

    // ── getters / setters ────────────────────────────────────────────────────

    public int getRowNumber() { return rowNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return "Row " + rowNumber + " [" + field + "]: " + message;
    }
}
