package ch.refereecoach.probasket.common;

import lombok.Getter;

import java.util.function.Function;

@Getter
public enum CategoryType {
    GENERAL("General"),
    IMAGE("Image, Approach"),
    FITNESS("Fitness Condition"),
    MECHANICS(officiatingMode -> officiatingMode.getDescription() + " Mechanics & Individual Officiating Techniques"),
    FOULS("Critera: Fouls"),
    VIOLATIONS("Critera: Violations"),
    GAME_CONTROL("Game Control and Management"),
    POINTS_TO_KEEP("Points to Keep"),
    POINTS_TO_IMPROVE("Points to Improve");

    private final Function<OfficiatingMode, String> description;

    CategoryType(Function<OfficiatingMode, String> description) {
        this.description = description;
    }

    CategoryType(String description) {
        this(_ -> description);
    }

}
