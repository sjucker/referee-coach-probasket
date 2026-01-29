package ch.refereecoach.probasket.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum CriteriaType {
    IMAGE_ON_TIME("pünktlich", CategoryType.IMAGE, CriteriaStateType.RADIO),
    IMAGE_PRE_GAME_CHECKS("Kontrollen, OT", CategoryType.IMAGE, CriteriaStateType.RADIO),
    IMAGE_PRE_GAME("Pre-Game", CategoryType.IMAGE, CriteriaStateType.RADIO),

    FITNESS_SPEED("Speed", CategoryType.FITNESS, CriteriaStateType.RADIO),
    FITNESS_ENDURANCE("Ausdauer", CategoryType.FITNESS, CriteriaStateType.RADIO),
    FITNESS_EFFECT("Wirkung", CategoryType.FITNESS, CriteriaStateType.RADIO),

    LEAD_OA("Lead: OA / 45°", CategoryType.MECHANICS, CriteriaStateType.RADIO),
    LEAD_DS("Lead: DS", CategoryType.MECHANICS, CriteriaStateType.RADIO),
    LEAD_CD("Lead: CD", CategoryType.MECHANICS, CriteriaStateType.RADIO),
    LEAD_ETP("Lead: ETP", CategoryType.MECHANICS, CriteriaStateType.RADIO),
    LEAD_AOR("Lead: AOR", CategoryType.MECHANICS, CriteriaStateType.RADIO),

    TRAIL_AOR("Trail: AOR", CategoryType.MECHANICS, CriteriaStateType.RADIO),
    TRAIL_DS("Trail: DS", CategoryType.MECHANICS, CriteriaStateType.RADIO),
    TRAIL_PENETRATION("Trail: penetration / cross step", CategoryType.MECHANICS, CriteriaStateType.RADIO),
    TRAIL_RB("Trail: RB", CategoryType.MECHANICS, CriteriaStateType.RADIO),
    TRAIL_AOR3("Trail: AOR (i.e. Zone 3)", CategoryType.MECHANICS, CriteriaStateType.RADIO),

    FOULS_HC("HC", CategoryType.FOULS, CriteriaStateType.RADIO),
    FOULS_BLOCK("blk/ch", CategoryType.FOULS, CriteriaStateType.RADIO),
    FOULS_AOS("AOS", CategoryType.FOULS, CriteriaStateType.RADIO),
    FOULS_RB("RB", CategoryType.FOULS, CriteriaStateType.RADIO),
    FOULS_PNR("PNR", CategoryType.FOULS, CriteriaStateType.RADIO),
    FOULS_OFF_BALL("off ball", CategoryType.FOULS, CriteriaStateType.RADIO),

    VIOLATION_TV("TV", CategoryType.VIOLATIONS, CriteriaStateType.RADIO),
    VIOLATION_DD("DD", CategoryType.VIOLATIONS, CriteriaStateType.RADIO),
    VIOLATION_OOB("OOB", CategoryType.VIOLATIONS, CriteriaStateType.RADIO),
    VIOLATION_BCV("BCV", CategoryType.VIOLATIONS, CriteriaStateType.RADIO),
    VIOLATION_SECS("3 / 5 / 8 / 14 / 24", CategoryType.VIOLATIONS, CriteriaStateType.RADIO),

    GAME_CONTROL_PLAYER("Player Management", CategoryType.GAME_CONTROL, CriteriaStateType.RADIO),
    GAME_CONTROL_COACH("Coach Management", CategoryType.GAME_CONTROL, CriteriaStateType.RADIO),
    GAME_CONTROL_PREVENTION("Prevention", CategoryType.GAME_CONTROL, CriteriaStateType.RADIO),
    GAME_CONTROL_EOP("EOP / EOG / EO TO", CategoryType.GAME_CONTROL, CriteriaStateType.RADIO),

    KEEP_IMAGE("Image", CategoryType.POINTS_TO_KEEP, CriteriaStateType.CHECKBOX),
    KEEP_FOULS("Fouls", CategoryType.POINTS_TO_KEEP, CriteriaStateType.CHECKBOX),
    KEEP_VIOLATIONS("Violations", CategoryType.POINTS_TO_KEEP, CriteriaStateType.CHECKBOX),
    KEEP_MECHANICS("Mechanics", CategoryType.POINTS_TO_KEEP, CriteriaStateType.CHECKBOX),
    KEEP_FITNESS("Fitness", CategoryType.POINTS_TO_KEEP, CriteriaStateType.CHECKBOX),
    KEEP_GAME_CONTROL("Game Control", CategoryType.POINTS_TO_KEEP, CriteriaStateType.CHECKBOX),

    IMPROVE_IMAGE("Image", CategoryType.POINTS_TO_IMPROVE, CriteriaStateType.CHECKBOX),
    IMPROVE_FOULS("Fouls", CategoryType.POINTS_TO_IMPROVE, CriteriaStateType.CHECKBOX),
    IMPROVE_VIOLATIONS("Violations", CategoryType.POINTS_TO_IMPROVE, CriteriaStateType.CHECKBOX),
    IMPROVE_MECHANICS("Mechanics", CategoryType.POINTS_TO_IMPROVE, CriteriaStateType.CHECKBOX),
    IMPROVE_FITNESS("Fitness", CategoryType.POINTS_TO_IMPROVE, CriteriaStateType.CHECKBOX),
    IMPROVE_GAME_CONTROL("Game Control", CategoryType.POINTS_TO_IMPROVE, CriteriaStateType.CHECKBOX);

    private final String description;
    private final CategoryType categoryType;
    private final CriteriaStateType criteriaStateType;

    public static List<CriteriaType> forCategory(CategoryType categoryType) {
        return Arrays.stream(values()).filter(criteriaType -> criteriaType.getCategoryType() == categoryType).toList();
    }
}
