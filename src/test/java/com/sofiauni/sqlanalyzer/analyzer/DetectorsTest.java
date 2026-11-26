package com.sofiauni.sqlanalyzer.analyzer;

import com.sofiauni.sqlanalyzer.analyzer.detection.impl.NPlusOneDetector;
import com.sofiauni.sqlanalyzer.analyzer.detection.impl.RepeatedQueryDetector;
import com.sofiauni.sqlanalyzer.analyzer.detection.impl.SelectStarDetector;
import com.sofiauni.sqlanalyzer.analyzer.model.Detection;
import com.sofiauni.sqlanalyzer.analyzer.model.DetectionType;
import com.sofiauni.sqlanalyzer.analyzer.model.QueryEvent;
import com.sofiauni.sqlanalyzer.analyzer.model.QueryType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class DetectorsTest {

    private static QueryEvent select(String normalizedSql) {
        return new QueryEvent(normalizedSql, normalizedSql, Integer.toHexString(normalizedSql.hashCode()),
                QueryType.SELECT, 1_000, -1, System.nanoTime(), "tx", "req", null);
    }

    @Test
    void whenPkLookupRepeatedOverThreshold_thenNPlusOneDetected() {
        QueryEvent pkLookup = select("select * from order_item where order_id = ?");
        List<QueryEvent> events = IntStream.range(0, 5).mapToObj(i -> pkLookup).toList();

        List<Detection> detections = new NPlusOneDetector(3).inspect("req", events);

        assertThat(detections).hasSize(1);
        assertThat(detections.get(0).type()).isEqualTo(DetectionType.N_PLUS_ONE);
        assertThat(detections.get(0).occurrences()).isEqualTo(5);
    }

    @Test
    void whenPkLookupBelowNPlusOneThreshold_thenRepeatedQueryDetectedInstead() {
        QueryEvent pkLookup = select("select * from orders where id = ?");

        List<Detection> nPlusOne = new NPlusOneDetector(3).inspect("req", List.of(pkLookup, pkLookup));
        List<Detection> repeated = new RepeatedQueryDetector(2, 3).inspect("req", List.of(pkLookup, pkLookup));

        assertThat(nPlusOne).isEmpty();
        assertThat(repeated).extracting(Detection::type).containsExactly(DetectionType.REPEATED_QUERY);
    }

    @Test
    void whenSelectStarPresent_thenDetectedOncePerHash() {
        QueryEvent star = select("select * from product");

        List<Detection> detections = new SelectStarDetector().inspect("req", List.of(star, star));

        assertThat(detections).extracting(Detection::type).containsExactly(DetectionType.SELECT_STAR);
    }
}
