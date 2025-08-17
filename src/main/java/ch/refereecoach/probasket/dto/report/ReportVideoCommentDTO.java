package ch.refereecoach.probasket.dto.report;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record ReportVideoCommentDTO(@NotNull Long id,
                                    @NotNull Long timestampInSeconds,
                                    @NotNull String comment,
                                    @NotNull LocalDateTime createdAt,
                                    @NotNull Long createdById,
                                    @NotNull String createdBy,
                                    boolean requiresReply,
                                    boolean referenceOnly,
                                    @NotNull List<ReportVideoCommentReplyDTO> replies,
                                    @NotNull List<TagDTO> tags) {

    public static ReportVideoCommentDTO of(Long id, Long timestampInSeconds, String comment,
                                           LocalDateTime createdAt, Long createdById, String createdByFirstname, String createdByLastname,
                                           boolean requiresReply,
                                           List<ReportVideoCommentReplyDTO> replies,
                                           List<TagDTO> tags) {
        return new ReportVideoCommentDTO(id,
                                         timestampInSeconds,
                                         comment,
                                         createdAt,
                                         createdById,
                                         createdByFirstname + "_" + createdByLastname,
                                         requiresReply,
                                         false,
                                         List.of(),
                                         List.of());
    }
}
