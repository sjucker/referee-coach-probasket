package ch.refereecoach.probasket.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserDTO(@NotNull @NotBlank String firstName,
                            @NotNull @NotBlank String lastName,
                            @NotNull @Email String email,
                            @NotNull @NotBlank @Size(max = 255) String username,
                            @NotNull @Size(min = 8, max = 100) String password) {
}
