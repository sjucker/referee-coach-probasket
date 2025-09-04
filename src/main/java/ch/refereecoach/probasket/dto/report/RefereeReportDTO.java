package ch.refereecoach.probasket.dto.report;

import ch.refereecoach.probasket.common.ReportType;
import ch.refereecoach.probasket.dto.basketplan.BasketplanGameDTO;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record RefereeReportDTO(@NotNull Long id,
                               @NotNull String externalId,
                               @NotNull ReportType reportType,
                               @NotNull Long coachId,
                               @NotNull String coachName,
                               @NotNull Long reporteeId,
                               @NotNull String reporteeName,
                               @NotNull BasketplanGameDTO game,
                               String youtubeId,
                               Long asportId,
                               BigDecimal score,
                               boolean finished,
                               boolean userIsReportee,
                               @NotNull List<ReportCommentDTO> comments,
                               @NotNull List<ReportVideoCommentDTO> videoComments) {
}
