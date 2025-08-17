package ch.refereecoach.probasket.dto.report;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReportVideoCommentReplyDTO(@NotNull Long id,
                                         @NotNull String reply,
                                         @NotNull LocalDateTime createdAt,
                                         @NotNull Long createdById,
                                         @NotNull String createdBy) {

    public static ReportVideoCommentReplyDTO of(Long id, String reply, LocalDateTime createdAt, Long createdById, String createdByFirstName, String createdByLastName) {
        return new ReportVideoCommentReplyDTO(id, reply, createdAt, createdById, createdByFirstName + " " + createdByLastName);
    }

}
