package ch.refereecoach.probasket.dto.report;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReportSearchResultDTO(@NotNull List<ReportOverviewDTO> items,
                                    @NotNull int total) {
}
