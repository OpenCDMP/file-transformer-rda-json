package org.opencdmp.filetransformer.rda;

import org.junit.jupiter.api.Test;
import org.opencdmp.filetransformer.rda.model.rda.Distribution;
import org.opencdmp.filetransformer.rda.service.json.JsonHandlingService;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RDA 1.3 defines {@code available_until} and {@code issued} as dates. They are written as "2030-09-30", and
 * files exported by earlier versions ("2030-09-30T00:00:00.000Z") must still import.
 */
class DistributionDatesTest {

    private final JsonHandlingService json = new JsonHandlingService();

    @Test
    void writesDatesWithoutTime() {
        Distribution distribution = new Distribution();
        distribution.setAvailableUntil(Date.from(Instant.parse("2030-09-30T00:00:00Z")));
        distribution.setIssued(Date.from(Instant.parse("2026-05-01T00:00:00Z")));

        String written = this.json.toJsonSafe(distribution);

        assertThat(written).contains("\"available_until\":\"2030-09-30\"").contains("\"issued\":\"2026-05-01\"");
    }

    @Test
    void readsDatesAndEarlierDateTimes() throws Exception {
        Date expected = Date.from(Instant.parse("2030-09-30T00:00:00Z"));

        assertThat(this.json.fromJson(Distribution.class, "{\"available_until\":\"2030-09-30\"}").getAvailableUntil()).isEqualTo(expected);
        assertThat(this.json.fromJson(Distribution.class, "{\"available_until\":\"2030-09-30T00:00:00.000Z\"}").getAvailableUntil()).isEqualTo(expected);
    }
}
