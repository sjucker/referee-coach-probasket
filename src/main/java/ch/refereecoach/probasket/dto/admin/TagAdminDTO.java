package ch.refereecoach.probasket.dto.admin;

import jakarta.validation.constraints.NotNull;

public record TagAdminDTO(@NotNull Long id,
                          @NotNull String name,
                          @NotNull int usageCount) {
}
