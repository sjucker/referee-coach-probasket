package ch.refereecoach.probasket.common;

public enum CriteriaStateType {
    RADIO,
    RADIO_RANGE,
    CHECKBOX;

    public CriteriaState getDefault() {
        return switch (this) {
            case RADIO -> CriteriaState.NEUTRAL;
            case CHECKBOX -> CriteriaState.FALSE;
            case RADIO_RANGE -> CriteriaState.NOT_APPLICABLE;
        };
    }
}
