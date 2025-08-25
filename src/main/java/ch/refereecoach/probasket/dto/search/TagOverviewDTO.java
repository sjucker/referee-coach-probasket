package ch.refereecoach.probasket.dto.search;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record TagOverviewDTO(@NotNull String gameNumber,
                             @NotNull String competition,
                             @NotNull LocalDate date,
                             @NotNull Long timestampInSeconds,
                             @NotNull String comment,
                             @NotNull String youtubeId,
                             @NotNull String tags) {
}
