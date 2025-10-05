package ch.refereecoach.probasket.dto.report;

import jakarta.validation.constraints.NotNull;

public record CreateRefereeReportDTO(@NotNull String gameNumber,
                                     @NotNull Long reporteeId,
                                     String videoUrl,
                                     boolean internal) {
}
