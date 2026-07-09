package ch.refereecoach.probasket.dto.report;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record ReportVideoCommentDTO(Long id,
                                    @NotNull Long timestampInSeconds,
                                    @NotNull String comment,
                                    @NotNull LocalDateTime createdAt,
                                    @NotNull Long createdById,
                                    @NotNull String createdBy,
                                    boolean requiresReply,
                                    boolean reference,
                                    @NotNull List<ReportVideoCommentReplyDTO> replies,
                                    @NotNull List<TagDTO> tags,
                                    // set for coach-uploaded video snippets (instead of a timestamp into the full-game video)
                                    Long uploadId,
                                    // short-lived presigned URL to play the uploaded snippet; filled in on read
                                    String videoUrl,
                                    String videoFilename) {

    public static ReportVideoCommentDTO of(Long id, Long timestampInSeconds, String comment,
                                           LocalDateTime createdAt, Long createdById, String createdByFirstname, String createdByLastname,
                                           boolean requiresReply,
                                           List<ReportVideoCommentReplyDTO> replies,
                                           List<TagDTO> tags,
                                           Long uploadId,
                                           String videoFilename) {
        return new ReportVideoCommentDTO(id, timestampInSeconds, comment, createdAt, createdById, createdByFirstname + " " + createdByLastname, requiresReply, false, replies, tags, uploadId, null, videoFilename);
    }

    public static ReportVideoCommentDTO ofReference(Long id, Long timestampInSeconds, String comment,
                                                    LocalDateTime createdAt, Long createdById, String createdByFirstname, String createdByLastname,
                                                    boolean requiresReply,
                                                    List<ReportVideoCommentReplyDTO> replies,
                                                    List<TagDTO> tags,
                                                    Long uploadId,
                                                    String videoFilename) {
        return new ReportVideoCommentDTO(id, timestampInSeconds, comment, createdAt, createdById, createdByFirstname + " " + createdByLastname, requiresReply, true, replies, tags, uploadId, null, videoFilename);
    }

    public ReportVideoCommentDTO withVideoUrl(String videoUrl) {
        return new ReportVideoCommentDTO(id, timestampInSeconds, comment, createdAt, createdById, createdBy, requiresReply, reference, replies, tags, uploadId, videoUrl, videoFilename);
    }
}
