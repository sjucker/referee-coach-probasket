package ch.refereecoach.probasket.dto.search;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TagSearchResultDTO(@NotNull List<TagOverviewDTO> items,
                                 @NotNull int total) {
}
