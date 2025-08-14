package ch.refereecoach.probasket.common;

import lombok.Getter;

import java.util.function.Function;

@Getter
public enum CategoryType {
    GENERAL("General", false),
    IMAGE("Image, Approach", true),
    FITNESS("Fitness Condition", true),
    MECHANICS(officiatingMode -> officiatingMode.getDescription() + " Mechanics & Individual Officiating Techniques", true),
    FOULS("Critera: Fouls", true),
    VIOLATIONS("Critera: Violations", true),
    GAME_CONTROL("Game Control and Management", true),
    POINTS_TO_KEEP("Points to Keep", false),
    POINTS_TO_IMPROVE("Points to Improve", false);

    private final Function<OfficiatingMode, String> description;
    private final boolean scoreRequired;

    CategoryType(Function<OfficiatingMode, String> description, boolean scoreRequired) {
        this.description = description;
        this.scoreRequired = scoreRequired;
    }

    CategoryType(String description, boolean scoreRequired) {
        this(_ -> description, scoreRequired);
    }

}
