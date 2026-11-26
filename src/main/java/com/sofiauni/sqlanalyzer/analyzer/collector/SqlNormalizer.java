package com.sofiauni.sqlanalyzer.analyzer.collector;

import com.sofiauni.sqlanalyzer.analyzer.model.QueryType;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.regex.Pattern;

/**
 * Textual SQL normalization and hashing (methodology hypothesis H2). Literals and bind
 * parameters are collapsed to {@code ?}, IN-lists are canonicalized, and whitespace is
 * normalized, so structurally identical queries share a single {@code sqlHash}.
 *
 * <p>This is a textual (not AST) normalizer; its limitations are acknowledged in section 4.7.
 */
public class SqlNormalizer {

    private static final Pattern STRING_LITERAL = Pattern.compile("'(?:[^']|'')*'");
    private static final Pattern NUMERIC_LITERAL = Pattern.compile("(?<![a-zA-Z_$.])\\b\\d+(?:\\.\\d+)?\\b");
    private static final Pattern PLACEHOLDER_LIST = Pattern.compile("\\?(?:\\s*,\\s*\\?)+");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    public String normalize(String rawSql) {
        if (rawSql == null || rawSql.isBlank()) {
            return "";
        }
        String result = rawSql;
        result = STRING_LITERAL.matcher(result).replaceAll("?");
        result = NUMERIC_LITERAL.matcher(result).replaceAll("?");
        result = PLACEHOLDER_LIST.matcher(result).replaceAll("?");
        result = WHITESPACE.matcher(result).replaceAll(" ");
        return result.trim().toLowerCase();
    }

    public String hash(String normalizedSql) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(normalizedSql.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(normalizedSql.hashCode());
        }
    }

    public QueryType detectType(String sql) {
        if (sql == null || sql.isBlank()) {
            return QueryType.OTHER;
        }
        String trimmed = sql.stripLeading().toLowerCase();
        if (trimmed.startsWith("select")) {
            return QueryType.SELECT;
        }
        if (trimmed.startsWith("insert")) {
            return QueryType.INSERT;
        }
        if (trimmed.startsWith("update")) {
            return QueryType.UPDATE;
        }
        if (trimmed.startsWith("delete")) {
            return QueryType.DELETE;
        }
        if (trimmed.startsWith("create") || trimmed.startsWith("alter")
                || trimmed.startsWith("drop") || trimmed.startsWith("truncate")) {
            return QueryType.DDL;
        }
        return QueryType.OTHER;
    }
}
