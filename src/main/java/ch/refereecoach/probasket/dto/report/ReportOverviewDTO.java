package ch.refereecoach.probasket.dto.report;

import ch.refereecoach.probasket.common.ReportType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReportOverviewDTO(@NotNull String externalId,
                                @NotNull ReportType type,
                                @NotNull LocalDate date,
                                @NotNull String gameNumber,
                                @NotNull String competition,
                                @NotNull String teams,
                                @NotNull String coach,
                                @NotNull Long coachId,
                                @NotNull String reportee,
                                @NotNull Long reporteeId,
                                RefereeDTO referee1,
                                RefereeDTO referee2,
                                RefereeDTO referee3,
                                boolean finished) {
}
