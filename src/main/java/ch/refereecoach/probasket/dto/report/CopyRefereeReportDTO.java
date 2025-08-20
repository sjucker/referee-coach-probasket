package ch.refereecoach.probasket.dto.report;

import jakarta.validation.constraints.NotNull;

public record CopyRefereeReportDTO(@NotNull Long reporteeId) {
}
