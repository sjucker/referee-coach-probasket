package ch.refereecoach.probasket.dto.report;

import ch.refereecoach.probasket.dto.basketplan.BasketplanGameDTO;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record RefereeReportDTO(@NotNull Long id,
                               @NotNull String externalId,
                               @NotNull Long coachId,
                               @NotNull String coachName,
                               @NotNull Long reporteeId,
                               @NotNull String reporteeName,
                               @NotNull BasketplanGameDTO game,
                               BigDecimal score,
                               @NotNull List<ReportCommentDTO> comments) {
}
