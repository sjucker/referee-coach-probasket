package ch.refereecoach.probasket.dto.auth;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SetPasswordDTO(@NotNull @Size(min = 8, max = 100) String password) {
}
