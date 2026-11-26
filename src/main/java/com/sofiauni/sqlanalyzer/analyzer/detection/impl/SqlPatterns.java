package com.sofiauni.sqlanalyzer.analyzer.detection.impl;

import java.util.regex.Pattern;

/**
 * Shared structural checks over normalized SQL used by several detectors.
 */
final class SqlPatterns {

    private static final Pattern SELECT_STAR = Pattern.compile("select\\s+\\*");
    private static final Pattern EQUALITY_PREDICATE = Pattern.compile("where\\b.*=\\s*\\?");
    private static final Pattern LIMIT_CLAUSE =
            Pattern.compile("\\b(limit|offset|fetch\\s+first|fetch\\s+next|top\\s+\\?)\\b");
    private static final Pattern AGGREGATE_ONLY =
            Pattern.compile("select\\s+(count|sum|avg|min|max)\\s*\\(");

    private SqlPatterns() {
    }

    static boolean isSelectStar(String normalizedSql) {
        return normalizedSql != null && SELECT_STAR.matcher(normalizedSql).find();
    }

    static boolean hasEqualityPredicate(String normalizedSql) {
        return normalizedSql != null && EQUALITY_PREDICATE.matcher(normalizedSql).find();
    }

    static boolean hasLimit(String normalizedSql) {
        return normalizedSql != null && LIMIT_CLAUSE.matcher(normalizedSql).find();
    }

    static boolean hasWhere(String normalizedSql) {
        return normalizedSql != null && normalizedSql.contains("where");
    }

    static boolean isAggregateOnly(String normalizedSql) {
        return normalizedSql != null && AGGREGATE_ONLY.matcher(normalizedSql).find();
    }
}
