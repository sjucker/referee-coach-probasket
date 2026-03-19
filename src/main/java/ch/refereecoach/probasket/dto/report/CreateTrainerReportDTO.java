package ch.refereecoach.probasket.dto.report;

import jakarta.validation.constraints.NotNull;

public record CreateTrainerReportDTO(@NotNull String gameNumber,
                                     // TODO how identified and where stored?
                                     @NotNull String trainer) {
}
