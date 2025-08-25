package ch.refereecoach.probasket.dto.report;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateRefereeReportDiscussionReplyDTO(@NotNull List<NewReportVideoCommentReplyDTO> replies,
                                                    @NotNull List<NewReportVideoCommentDTO> comments) {
}
