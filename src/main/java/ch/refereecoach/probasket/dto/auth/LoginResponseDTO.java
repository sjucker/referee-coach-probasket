package ch.refereecoach.probasket.dto.auth;

import jakarta.validation.constraints.NotNull;

// TODO add roles, etc.
public record LoginResponseDTO(@NotNull String token,
                               @NotNull String username) {
}
