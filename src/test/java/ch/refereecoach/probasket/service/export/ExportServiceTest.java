package ch.refereecoach.probasket.service.export;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExportServiceTest {

    @Test
    void coachShortName() {
        assertThat(ExportService.coachShortName("Stefan", "Jucker", "Stefan Jucker")).isEqualTo("StJu");
        assertThat(ExportService.coachShortName(" X ", "Li", "X Li")).isEqualTo("XLi");
        assertThat(ExportService.coachShortName(null, null, "Stefan Jucker")).isEqualTo("Stefan Jucker");
    }

}
