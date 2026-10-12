package ch.refereecoach.probasket.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaveTagDTO(@NotNull @NotBlank @Size(max = 255) String name) {
}
