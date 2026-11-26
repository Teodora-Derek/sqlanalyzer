package com.sofiauni.sqlanalyzer.analyzer;

import com.sofiauni.sqlanalyzer.analyzer.collector.SqlNormalizer;
import com.sofiauni.sqlanalyzer.analyzer.model.QueryType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SqlNormalizerTest {

    private final SqlNormalizer subject = new SqlNormalizer();

    @Test
    void whenLiteralsDiffer_thenNormalizedFormAndHashMatch() {
        String first = subject.normalize("SELECT * FROM product WHERE id = 5");
        String second = subject.normalize("select *  from product where id = 42");

        assertThat(first).isEqualTo(second);
        assertThat(subject.hash(first)).isEqualTo(subject.hash(second));
    }

    @Test
    void whenInListVaries_thenCanonicalizedToSinglePlaceholder() {
        String two = subject.normalize("SELECT id FROM product WHERE id IN (1, 2)");
        String three = subject.normalize("SELECT id FROM product WHERE id IN (7, 8, 9)");

        assertThat(two).isEqualTo(three);
    }

    @Test
    void whenStringLiteralPresent_thenReplacedWithPlaceholder() {
        assertThat(subject.normalize("SELECT id FROM customer WHERE email = 'a@b.co'"))
                .isEqualTo("select id from customer where email = ?");
    }

    @Test
    void detectsQueryType() {
        assertThat(subject.detectType("  SELECT 1")).isEqualTo(QueryType.SELECT);
        assertThat(subject.detectType("INSERT INTO x VALUES (1)")).isEqualTo(QueryType.INSERT);
        assertThat(subject.detectType("update x set y = 1")).isEqualTo(QueryType.UPDATE);
        assertThat(subject.detectType("DELETE FROM x")).isEqualTo(QueryType.DELETE);
        assertThat(subject.detectType("CREATE TABLE x (id int)")).isEqualTo(QueryType.DDL);
    }
}
