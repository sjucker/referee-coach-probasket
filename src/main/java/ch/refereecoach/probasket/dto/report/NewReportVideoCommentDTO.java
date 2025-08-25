package ch.refereecoach.probasket.dto.report;

import jakarta.validation.constraints.NotNull;

public record NewReportVideoCommentDTO(@NotNull Long timestampInSeconds,
                                       @NotNull String comment) {
}
