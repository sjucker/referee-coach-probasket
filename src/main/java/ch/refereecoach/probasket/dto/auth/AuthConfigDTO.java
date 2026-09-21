package ch.refereecoach.probasket.dto.auth;

import ch.refereecoach.probasket.common.AuthProvider;
import jakarta.validation.constraints.NotNull;

/**
 * publicly readable authentication configuration, so the frontend knows which login flow to present.
 */
public record AuthConfigDTO(@NotNull AuthProvider authProvider) {
}
