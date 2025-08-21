package ch.refereecoach.probasket.dto.report;

import jakarta.validation.constraints.NotNull;

public record CreateReportVideoCommentReplyDTO(@NotNull Long commentId,
                                               @NotNull String reply) {
}
