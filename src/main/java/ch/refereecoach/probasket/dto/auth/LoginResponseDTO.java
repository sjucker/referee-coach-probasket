package ch.refereecoach.probasket.dto.auth;

import ch.refereecoach.probasket.common.UserRole;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record LoginResponseDTO(@NotNull String token,
                               @NotNull String username,
                               @NotNull Long userId,
                               @NotNull List<UserRole> roles) {
}
