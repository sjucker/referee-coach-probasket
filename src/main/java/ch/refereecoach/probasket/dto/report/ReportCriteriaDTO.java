package ch.refereecoach.probasket.dto.report;

import ch.refereecoach.probasket.common.CriteriaState;
import ch.refereecoach.probasket.common.CriteriaType;
import jakarta.validation.constraints.NotNull;

public record ReportCriteriaDTO(@NotNull Long id,
                                @NotNull CriteriaType type,
                                @NotNull String description,
                                String comment,
                                CriteriaState state) {

    public static ReportCriteriaDTO of(Long id, String type, String comment, String state) {
        var value = CriteriaType.valueOf(type);
        return new ReportCriteriaDTO(id, value, value.getDescription(), comment, state != null ? CriteriaState.valueOf(state) : null);
    }

}
