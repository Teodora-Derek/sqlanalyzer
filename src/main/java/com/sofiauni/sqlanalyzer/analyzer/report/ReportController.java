package com.sofiauni.sqlanalyzer.analyzer.report;

import com.sofiauni.sqlanalyzer.analyzer.model.DetectionType;
import com.sofiauni.sqlanalyzer.analyzer.model.Recommendation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Read-only HTTP view of the most recent recommendations and summary counts (section 4.6).
 * Registered only in web applications and when the report endpoint is enabled.
 */
@RestController
@RequestMapping("/api/v1/sqlanalyzer")
public class ReportController {

    private final ReportStore reportStore;

    public ReportController(ReportStore reportStore) {
        this.reportStore = reportStore;
    }

    @GetMapping("/reports")
    public List<Recommendation> reports() {
        return reportStore.findAll();
    }

    @GetMapping("/summary")
    public Map<DetectionType, Long> summary() {
        return reportStore.countsByType();
    }
}
