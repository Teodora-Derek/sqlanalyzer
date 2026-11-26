package com.sofiauni.sqlanalyzer.analyzer.model;

public enum DetectionType {
    N_PLUS_ONE,
    REPEATED_QUERY,
    MISSING_PAGINATION,
    SELECT_STAR,
    LONG_RUNNING_QUERY,
    BULK_INSTEAD_OF_BATCH,
    TRANSACTION_SPAN_ANOMALY
}
