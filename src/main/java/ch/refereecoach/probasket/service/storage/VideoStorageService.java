package ch.refereecoach.probasket.service.storage;

import java.time.Duration;

/**
 * Abstraction over an S3-compatible object store (Heroku Bucketeer / AWS S3 / Cloudflare R2) used to
 * persist coach-uploaded video snippets. Files are uploaded and played back directly by the browser via
 * short-lived presigned URLs, so the video bytes never pass through the application.
 */
public interface VideoStorageService {

    /**
     * Create a short-lived presigned URL the browser can use to PUT the file directly into the bucket.
     * The upload request must send the same {@code Content-Type} that is passed here.
     */
    String createUploadUrl(String objectKey, String contentType, Duration ttl);

    /**
     * Create a short-lived presigned URL the browser can use to GET (play back) the file.
     */
    String createDownloadUrl(String objectKey, Duration ttl);

    /**
     * Whether an object actually exists in the bucket (used to confirm a completed upload).
     */
    boolean exists(String objectKey);

    /**
     * Delete an object from the bucket (called when a snippet comment is removed).
     */
    void delete(String objectKey);
}
