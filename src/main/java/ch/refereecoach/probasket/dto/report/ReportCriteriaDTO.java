package ch.refereecoach.probasket.dto.report;

import ch.refereecoach.probasket.common.CriteriaState;
import ch.refereecoach.probasket.common.CriteriaStateType;
import ch.refereecoach.probasket.common.CriteriaType;
import jakarta.validation.constraints.NotNull;

public record ReportCriteriaDTO(@NotNull Long id,
                                @NotNull CriteriaType type,
                                @NotNull CriteriaStateType stateType,
                                @NotNull String description,
                                CriteriaState state) {

    public static ReportCriteriaDTO of(Long id, String type, String state) {
        var value = CriteriaType.valueOf(type);
        return new ReportCriteriaDTO(id, value, value.getCriteriaStateType(), value.getDescription(), state != null ? CriteriaState.valueOf(state) : null);
    }

}
