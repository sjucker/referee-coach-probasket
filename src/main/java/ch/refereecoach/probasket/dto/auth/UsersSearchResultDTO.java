package ch.refereecoach.probasket.dto.auth;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UsersSearchResultDTO(@NotNull List<UserDTO> items,
                                   @NotNull int total) {
}
