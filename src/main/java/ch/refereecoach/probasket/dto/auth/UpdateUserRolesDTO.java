package ch.refereecoach.probasket.dto.auth;

import jakarta.validation.constraints.NotNull;

public record UpdateUserRolesDTO(@NotNull Boolean refereeCoach,
                                 @NotNull Boolean referee,
                                 @NotNull Boolean trainerCoach,
                                 @NotNull Boolean trainer) {
}
