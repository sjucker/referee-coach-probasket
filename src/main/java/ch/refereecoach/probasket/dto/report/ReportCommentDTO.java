package ch.refereecoach.probasket.dto.report;

import ch.refereecoach.probasket.common.CategoryType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

import static ch.refereecoach.probasket.common.OfficiatingMode.OFFICIATING_2PO;

public record ReportCommentDTO(@NotNull Long id,
                               @NotNull CategoryType type,
                               @NotNull String typeDescription,
                               String comment,
                               boolean scoreRequired,
                               BigDecimal score,
                               @NotNull List<ReportCriteriaDTO> criteria) {

    public static ReportCommentDTO of(Long id, String type, String comment, BigDecimal score, List<ReportCriteriaDTO> criteria) {
        var value = CategoryType.valueOf(type);
        // TODO officiating mode not hardcoded!
        return new ReportCommentDTO(id, value, value.getDescription().apply(OFFICIATING_2PO), comment, value.isScoreRequired(), score, criteria);
    }
}
