package ch.refereecoach.probasket.dto.report;

import jakarta.validation.constraints.NotNull;

public record ReportDTO(@NotNull Long id,
                        @NotNull String externalId) {
}
