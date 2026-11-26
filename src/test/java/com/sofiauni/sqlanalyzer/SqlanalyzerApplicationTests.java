package com.sofiauni.sqlanalyzer;

import com.sofiauni.sqlanalyzer.analyzer.model.DetectionType;
import com.sofiauni.sqlanalyzer.analyzer.report.ReportStore;
import com.sofiauni.sqlanalyzer.demo.DemoApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(classes = DemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SqlanalyzerApplicationTests {

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private ReportStore reportStore;

	@Test
	void detectsInjectedAntiPatternsViaHttpEndpoints() {
		restTemplate.getForObject("/api/demo/orders", String.class);
		restTemplate.getForObject("/api/demo/products", String.class);
		restTemplate.getForObject("/api/demo/products/raw", String.class);
		restTemplate.getForObject("/api/demo/orders/1/repeated", String.class);
		restTemplate.postForObject("/api/demo/import?count=60", null, String.class);

		await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			Map<DetectionType, Long> counts = reportStore.countsByType();
			assertThat(counts).containsKeys(
					DetectionType.N_PLUS_ONE,
					DetectionType.MISSING_PAGINATION,
					DetectionType.SELECT_STAR,
					DetectionType.REPEATED_QUERY,
					DetectionType.BULK_INSTEAD_OF_BATCH,
					DetectionType.TRANSACTION_SPAN_ANOMALY);
		});
	}
}
