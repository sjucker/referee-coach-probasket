package ch.refereecoach.probasket.dto.report;

import ch.refereecoach.probasket.common.CategoryType;
import ch.refereecoach.probasket.common.Rank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

import static ch.refereecoach.probasket.common.OfficiatingMode.OFFICIATING_2PO;
import static ch.refereecoach.probasket.common.OfficiatingMode.OFFICIATING_3PO;

public record ReportCommentDTO(@NotNull Long id,
                               @NotNull CategoryType type,
                               @NotNull String typeDescription,
                               @NotNull List<String> criteriaHints,
                               String comment,
                               boolean scoreRequired,
                               BigDecimal score,
                               @NotNull List<ReportCriteriaDTO> criteria) {

    public static ReportCommentDTO of(Long id, String type, String comment, BigDecimal score, String rank, Long referee3Id, List<ReportCriteriaDTO> criteria) {
        var value = CategoryType.valueOf(type);
        var officiatingMode = referee3Id != null ? OFFICIATING_3PO : OFFICIATING_2PO;
        return new ReportCommentDTO(id, value, value.getDescription().apply(officiatingMode), value.getCriteriaHintsPerRank(Rank.valueOf(rank)), comment, value.isScoreRequired(), score, criteria);
    }
}
