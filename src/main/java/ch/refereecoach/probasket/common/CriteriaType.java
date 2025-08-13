package ch.refereecoach.probasket.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum CriteriaType {
    IMAGE_ON_TIME("pünktlich", CategoryType.IMAGE),
    IMAGE_PRE_GAME_CHECKS("Kontrollen, OT", CategoryType.IMAGE),
    IMAGE_PRE_GAME("Pre-Game", CategoryType.IMAGE),

    FITNESS_SPEED("Speed", CategoryType.FITNESS),
    FITNESS_ENDURANCE("Ausdauer", CategoryType.FITNESS),
    FITNESS_EFFECT("Wirkung", CategoryType.FITNESS),

    LEAD_OA("Lead: OA / 45°", CategoryType.MECHANICS),
    LEAD_DS("Lead: DS", CategoryType.MECHANICS),
    LEAD_CD("Lead: CD", CategoryType.MECHANICS),
    LEAD_ETP("Lead: ETP", CategoryType.MECHANICS),
    LEAD_AOR("Lead: AOR", CategoryType.MECHANICS),

    TRAIL_AOR("Trail: AOR", CategoryType.MECHANICS),
    TRAIL_DS("Trail: DS", CategoryType.MECHANICS),
    TRAIL_PENETRATION("Trail: penetration / cross step", CategoryType.MECHANICS),
    TRAIL_RB("Trail: RB", CategoryType.MECHANICS),
    TRAIL_AOR3("Trail: AOR (i.e. Zone 3)", CategoryType.MECHANICS),

    FOULS_HC("HC", CategoryType.FOULS),
    FOULS_BLOCK("blk/ch", CategoryType.FOULS),
    FOULS_AOS("AOS", CategoryType.FOULS),
    FOULS_RB("RB", CategoryType.FOULS),
    FOULS_PNR("PNR", CategoryType.FOULS),
    FOULS_OFF_BALL("off ball", CategoryType.FOULS),

    VIOLATION_TV("TV", CategoryType.VIOLATIONS),
    VIOLATION_DD("DD", CategoryType.VIOLATIONS),
    VIOLATION_OOB("OOB", CategoryType.VIOLATIONS),
    VIOLATION_BCV("BCV", CategoryType.VIOLATIONS),
    VIOLATION_SECS("3 / 5 / 8 / 14 / 24", CategoryType.VIOLATIONS),

    GAME_CONTROL_PLAYER("Player Management", CategoryType.GAME_CONTROL),
    GAME_CONTROL_COACH("Coach Management", CategoryType.GAME_CONTROL),
    GAME_CONTROL_PREVENTION("Prevention", CategoryType.GAME_CONTROL),
    GAME_CONTROL_EOP("EOP / EOG / EO TO", CategoryType.GAME_CONTROL);

    private final String description;
    private final CategoryType categoryType;

    public static List<CriteriaType> forCategory(CategoryType categoryType) {
        return Arrays.stream(values()).filter(criteriaType -> criteriaType.getCategoryType() == categoryType).toList();
    }
}
