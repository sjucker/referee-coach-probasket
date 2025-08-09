package ch.refereecoach.probasket.dto.auth;

import jakarta.validation.constraints.NotNull;

public record LoginRequestDTO(@NotNull String username, @NotNull String password) {
}
