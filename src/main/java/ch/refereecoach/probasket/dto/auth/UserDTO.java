package ch.refereecoach.probasket.dto.auth;

import ch.refereecoach.probasket.common.ReportType;
import jakarta.validation.constraints.NotNull;

public record UserDTO(@NotNull Long id,
                      @NotNull String username,
                      @NotNull String firstName,
                      @NotNull String lastName,
                      @NotNull String email,
                      boolean refereeCoach,
                      boolean referee,
                      boolean trainerCoach,
                      boolean trainer,
                      boolean admin) {

    public boolean hasRequiredRole(ReportType reportType) {
        return switch (reportType) {
            case REFEREE_VIDEO_REPORT, REFEREE_COMMENT_REPORT -> refereeCoach;
            case TRAINER_REPORT -> trainerCoach;
            case GAME_DISCUSSION -> referee;
        };
    }

}
