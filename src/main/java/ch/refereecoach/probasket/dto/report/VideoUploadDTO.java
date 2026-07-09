package ch.refereecoach.probasket.dto.report;

import jakarta.validation.constraints.NotNull;

/**
 * Response for a requested video-snippet upload: the new upload id plus a short-lived presigned URL
 * the browser uses to PUT the file directly into the bucket.
 */
public record VideoUploadDTO(@NotNull Long uploadId,
                             @NotNull String uploadUrl) {
}
