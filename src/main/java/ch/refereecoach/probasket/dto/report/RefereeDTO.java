package ch.refereecoach.probasket.dto.report;

import jakarta.validation.constraints.NotNull;

import java.util.Optional;

public record RefereeDTO(@NotNull Long id,
                         @NotNull String name) {
    public static Optional<RefereeDTO> of(Long id, String name) {
        if (id != null && name != null) {
            return Optional.of(new RefereeDTO(id, name));
        } else {
            return Optional.empty();
        }
    }
}
