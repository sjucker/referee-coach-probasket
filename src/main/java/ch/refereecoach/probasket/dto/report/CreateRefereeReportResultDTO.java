package ch.refereecoach.probasket.dto.report;

import jakarta.validation.constraints.NotNull;

public record CreateRefereeReportResultDTO(@NotNull String externalId) {
}
