package ch.refereecoach.probasket.common;

public enum CriteriaStateType {
    RADIO,
    CHECKBOX;

    public CriteriaState getDefault() {
        return switch (this) {
            case RADIO -> CriteriaState.NEUTRAL;
            case CHECKBOX -> CriteriaState.FALSE;
        };
    }
}
