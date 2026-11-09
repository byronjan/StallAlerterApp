package com.example.stallalerterapp.data.csv

/**
 * Enum representing different types of import errors that can occur when processing CSV files.
 * These enums are used to provide meaningful feedback to users instead of generic exceptions.
 */
enum class ImportError {
    /** Missing required columns in the CSV file (expected: full_name, stage, last_contact) */
    MISSING_REQUIRED_COLUMNS,

    /** CSV file has only headers but no data rows */
    EMPTY_FILE,

    /** Error during CSV parsing (invalid dates, malformed data, etc.) */
    PARSE_ERROR
}