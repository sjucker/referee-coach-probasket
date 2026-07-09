package ch.refereecoach.probasket.dto.report;

import jakarta.validation.constraints.NotNull;

public record CreateVideoUploadDTO(@NotNull String filename,
                                   @NotNull String contentType,
                                   @NotNull Long sizeBytes) {
}
