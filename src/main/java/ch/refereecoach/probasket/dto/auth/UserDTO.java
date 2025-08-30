package ch.refereecoach.probasket.dto.auth;

import ch.refereecoach.probasket.common.Rank;
import ch.refereecoach.probasket.common.ReportType;
import jakarta.validation.constraints.NotNull;

public record UserDTO(@NotNull Long id,
                      @NotNull String firstName,
                      @NotNull String lastName,
                      @NotNull String email,
                      Rank rank,
                      boolean refereeCoach,
                      boolean referee,
                      boolean trainerCoach,
                      boolean trainer,
                      boolean admin,
                      boolean active) {

    public boolean hasRequiredRole(ReportType reportType) {
        return switch (reportType) {
            case REFEREE_VIDEO_REPORT, REFEREE_COMMENT_REPORT -> refereeCoach;
            case TRAINER_REPORT -> trainerCoach;
            case GAME_DISCUSSION -> referee;
        };
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

}
