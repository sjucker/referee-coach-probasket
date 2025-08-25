package ch.refereecoach.probasket.dto.report;

import jakarta.validation.constraints.NotNull;

public record TagDTO(@NotNull Long id,
                     @NotNull String name) {
}
