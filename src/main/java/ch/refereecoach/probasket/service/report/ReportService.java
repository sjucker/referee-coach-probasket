package ch.refereecoach.probasket.service.report;

import ch.refereecoach.probasket.common.CategoryType;
import ch.refereecoach.probasket.common.CriteriaType;
import ch.refereecoach.probasket.common.Rank;
import ch.refereecoach.probasket.configuration.ApplicationProperties;
import ch.refereecoach.probasket.dto.report.CopyRefereeReportDTO;
import ch.refereecoach.probasket.dto.report.CreateRefereeReportDiscussionReplyDTO;
import ch.refereecoach.probasket.dto.report.CreateRefereeReportResultDTO;
import ch.refereecoach.probasket.dto.report.CreateVideoUploadDTO;
import ch.refereecoach.probasket.dto.report.RefereeReportDTO;
import ch.refereecoach.probasket.dto.report.ReportCommentDTO;
import ch.refereecoach.probasket.dto.report.ReportCriteriaDTO;
import ch.refereecoach.probasket.dto.report.ReportVideoCommentDTO;
import ch.refereecoach.probasket.dto.report.VideoUploadDTO;
import ch.refereecoach.probasket.jooq.tables.daos.ReportCommentDao;
import ch.refereecoach.probasket.jooq.tables.daos.ReportCriteriaDao;
import ch.refereecoach.probasket.jooq.tables.daos.ReportDao;
import ch.refereecoach.probasket.jooq.tables.daos.ReportVideoCommentDao;
import ch.refereecoach.probasket.jooq.tables.daos.ReportVideoCommentRefDao;
import ch.refereecoach.probasket.jooq.tables.daos.ReportVideoCommentReplyDao;
import ch.refereecoach.probasket.jooq.tables.daos.ReportVideoCommentTagDao;
import ch.refereecoach.probasket.jooq.tables.daos.ReportVideoUploadDao;
import ch.refereecoach.probasket.jooq.tables.pojos.Report;
import ch.refereecoach.probasket.jooq.tables.pojos.ReportComment;
import ch.refereecoach.probasket.jooq.tables.pojos.ReportCriteria;
import ch.refereecoach.probasket.jooq.tables.pojos.ReportVideoComment;
import ch.refereecoach.probasket.jooq.tables.pojos.ReportVideoCommentRef;
import ch.refereecoach.probasket.jooq.tables.pojos.ReportVideoCommentReply;
import ch.refereecoach.probasket.jooq.tables.pojos.ReportVideoCommentTag;
import ch.refereecoach.probasket.jooq.tables.pojos.ReportVideoUpload;
import ch.refereecoach.probasket.service.basketplan.BasketplanGameService;
import ch.refereecoach.probasket.service.mail.MailService;
import ch.refereecoach.probasket.service.storage.VideoStorageService;
import ch.refereecoach.probasket.util.AsportUtil;
import ch.refereecoach.probasket.util.DateUtil;
import ch.refereecoach.probasket.util.YouTubeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.refereecoach.probasket.common.ReportType.REFEREE_COMMENT_REPORT;
import static ch.refereecoach.probasket.common.ReportType.REFEREE_VIDEO_REPORT;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private static final BigDecimal DEFAULT_SCORE = new BigDecimal("7.00");

    // presigned upload URLs are used immediately by the browser after they are handed out
    private static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(15);

    private final ReportDao reportDao;
    private final ReportCommentDao reportCommentDao;
    private final ReportCriteriaDao reportCriteriaDao;
    private final ReportVideoCommentDao reportVideoCommentDao;
    private final ReportVideoCommentTagDao reportVideoCommentTagDao;
    private final ReportVideoCommentRefDao reportVideoCommentRefDao;
    private final ReportVideoCommentReplyDao reportVideoCommentReplyDao;
    private final ReportVideoUploadDao reportVideoUploadDao;
    private final BasketplanGameService basketplanGameService;
    private final UserService userService;
    private final MailService mailService;
    private final VideoStorageService videoStorageService;
    private final ApplicationProperties applicationProperties;

    public CreateRefereeReportResultDTO createRefereeReport(String gameNumber, String videoUrl, Long reporteeId, boolean internal, long userId) throws InvalidVideoUrlException {
        var coach = userService.getById(userId);
        var reportee = userService.getById(reporteeId);

        if (isNotBlank(videoUrl) && YouTubeUtil.parseYouTubeId(videoUrl).isEmpty() && AsportUtil.parseAsportEventId(videoUrl).isEmpty()) {
            throw new InvalidVideoUrlException();
        }

        var reportType = isBlank(videoUrl) ? REFEREE_COMMENT_REPORT : REFEREE_VIDEO_REPORT;
        if (!coach.hasRequiredRole(reportType)) {
            throw new IllegalStateException("user %s is not a coach!".formatted(coach.fullName()));
        }
        var game = basketplanGameService.findGameByNumber(gameNumber)
                                        .orElseThrow(() -> new IllegalArgumentException("game %s not found".formatted(gameNumber)));

        if (!game.containsReferee(reporteeId)) {
            throw new IllegalArgumentException("reportee %d not found in game %s".formatted(reporteeId, gameNumber));
        }

        var report = new Report();
        report.setExternalId(getExternalId());
        report.setReportType(reportType.name());
        report.setCoachId(coach.id());
        report.setCoachName(coach.fullName());
        report.setReporteeId(reportee.id());
        report.setReporteeName(reportee.fullName());
        report.setReporteeRank(reportee.rank().name());
        report.setGameNumber(game.gameNumber());
        report.setGameCompetition(game.competition());
        report.setGameDate(game.date());
        report.setGameResult(game.result());
        report.setGameHomeTeam(game.homeTeam());
        report.setGameHomeTeamId(game.homeTeamId());
        report.setGameGuestTeam(game.guestTeam());
        report.setGameGuestTeamId(game.guestTeamId());
        report.setGameReferee1Id(game.referee1Id());
        report.setGameReferee1Name(game.referee1Name());
        report.setGameReferee1Rank(ofNullable(game.referee1Rank()).map(Rank::name).orElse(null));
        report.setGameReferee2Id(game.referee2Id());
        report.setGameReferee2Name(game.referee2Name());
        report.setGameReferee2Rank(ofNullable(game.referee2Rank()).map(Rank::name).orElse(null));
        report.setGameReferee3Id(game.referee3Id());
        report.setGameReferee3Name(game.referee3Name());
        report.setGameReferee3Rank(ofNullable(game.referee3Rank()).map(Rank::name).orElse(null));
        report.setGameVideoUrl(videoUrl);
        report.setInternal(internal);

        report.setOverallScore(DEFAULT_SCORE);

        var now = DateUtil.now();
        report.setCreatedAt(now);
        report.setCreatedBy(coach.id());
        report.setUpdatedAt(now);
        report.setUpdatedBy(coach.id());
        report.setFinishedAt(null);
        report.setFinishedBy(null);
        report.setReminderSent(false);
        reportDao.insert(report);

        Arrays.stream(CategoryType.values())
              .forEach(categoryType -> {
                  var reportComment = new ReportComment(null, report.getId(), categoryType.name(), null, categoryType.isScoreRequired() ? DEFAULT_SCORE : null);
                  reportCommentDao.insert(reportComment);
                  CriteriaType.forCategory(categoryType)
                              .forEach(criteriaType -> reportCriteriaDao.insert(new ReportCriteria(null, reportComment.getId(),
                                                                                                   criteriaType.name(),
                                                                                                   criteriaType.getCriteriaStateType().getDefault().name())));
              });

        return new CreateRefereeReportResultDTO(report.getId(), report.getExternalId());
    }

    private String getExternalId() {
        String uuid;
        do {
            // insecure is good enough for this use-case
            uuid = RandomStringUtils.insecure().nextAlphabetic(10);
        } while (reportDao.fetchOptionalByExternalId(uuid).isPresent());

        return uuid;
    }

    public void updateRefereeReport(String externalId, RefereeReportDTO dto, long userId) {
        var coach = userService.getById(userId);
        var report = reportDao.fetchOptionalByExternalId(externalId).orElseThrow(() -> new IllegalArgumentException("report for external id %s not found".formatted(externalId)));
        if (!report.getCoachId().equals(coach.id())) {
            throw new IllegalStateException("report does not belong to user %s!".formatted(coach.fullName()));
        }

        if (report.getFinishedAt() != null) {
            throw new IllegalStateException("user is not allowed to update already finished video-report!");
        }

        var commentsPerType = dto.comments().stream().collect(toMap(ReportCommentDTO::type, identity()));
        reportCommentDao.fetchByReportId(report.getId())
                        .forEach(reportComment -> {
                            var reportCommentDTO = commentsPerType.get(CategoryType.valueOf(reportComment.getType()));
                            reportComment.setComment(reportCommentDTO.comment());
                            reportComment.setScore(reportCommentDTO.score());
                            reportCommentDao.update(reportComment);

                            var criteriaPerType = reportCommentDTO.criteria().stream().collect(toMap(ReportCriteriaDTO::type, identity()));
                            reportCriteriaDao.fetchByReportCommentId(reportComment.getId())
                                             .forEach(reportCriteria -> {
                                                 var reportCriteriaDTO = criteriaPerType.get(CriteriaType.valueOf(reportCriteria.getType()));
                                                 reportCriteria.setState(reportCriteriaDTO.state() != null ? reportCriteriaDTO.state().name() : null);
                                                 reportCriteriaDao.update(reportCriteria);
                                             });

                        });

        var existingVideoComments = reportVideoCommentDao.fetchByReportId(report.getId());

        var videoCommentsToUpdate = new HashMap<Long, ReportVideoCommentDTO>();
        var videoCommentRefsToUpdate = new HashMap<Long, ReportVideoCommentDTO>();
        dto.videoComments()
           .forEach(videoComment -> {
               if (videoComment.reference()) {
                   videoCommentRefsToUpdate.put(videoComment.id(), videoComment);
               } else if (videoComment.id() == null) {
                   // create: either a timestamped comment into the full-game video, or an uploaded snippet
                   var isSnippet = videoComment.uploadId() != null;
                   if (isSnippet || (videoComment.timestampInSeconds() != null && isNotBlank(videoComment.comment()))) {
                       // snippets carry their own clip and have no timestamp into a full-game video
                       var timestamp = videoComment.timestampInSeconds() != null ? videoComment.timestampInSeconds() : 0L;
                       var newVideoComment = new ReportVideoComment(null, report.getId(), timestamp, videoComment.comment(), DateUtil.now(), coach.id(), videoComment.requiresReply(), videoComment.uploadId());
                       reportVideoCommentDao.insert(newVideoComment);

                       videoComment.tags().forEach(tag -> reportVideoCommentTagDao.insert(new ReportVideoCommentTag(newVideoComment.getId(), tag.id())));
                   }
               } else {
                   // update
                   videoCommentsToUpdate.put(videoComment.id(), videoComment);
               }
           });
        existingVideoComments.forEach(reportVideoComment -> {
            var reportVideoCommentDTO = videoCommentsToUpdate.get(reportVideoComment.getId());
            if (reportVideoCommentDTO != null) {
                reportVideoComment.setTimestampInSeconds(reportVideoCommentDTO.timestampInSeconds());
                reportVideoComment.setComment(reportVideoCommentDTO.comment());
                reportVideoComment.setRequiresReply(reportVideoCommentDTO.requiresReply());
                reportVideoCommentDao.update(reportVideoComment);

                reportVideoCommentTagDao.delete(reportVideoCommentTagDao.fetchByReportVideoCommentId(reportVideoComment.getId()));
                reportVideoCommentDTO.tags().forEach(tag -> reportVideoCommentTagDao.insert(new ReportVideoCommentTag(reportVideoComment.getId(), tag.id())));
            } else {
                var uploadId = reportVideoComment.getReportVideoUploadId();
                reportVideoCommentDao.delete(reportVideoComment);
                // remove the uploaded clip (and its object) once the comment referencing it is gone
                deleteVideoUpload(uploadId);
            }
        });

        reportVideoCommentRefDao.fetchByReportId(report.getId())
                                .forEach(reportVideoCommentRef -> {
                                    var reportVideoCommentDTO = videoCommentRefsToUpdate.get(reportVideoCommentRef.getReportVideoCommentId());
                                    if (reportVideoCommentDTO != null) {
                                        reportVideoCommentRef.setRequiresReply(reportVideoCommentDTO.requiresReply());
                                        reportVideoCommentRefDao.update(reportVideoCommentRef);
                                    } else {
                                        reportVideoCommentRefDao.delete(reportVideoCommentRef);
                                    }
                                });

        report.setOverallScore(dto.score());
        report.setUpdatedAt(DateUtil.now());
        report.setUpdatedBy(coach.id());
        reportDao.update(report);
    }

    public void deleteRefereeReport(String externalId, long userId) {
        var coach = userService.getById(userId);
        var report = reportDao.fetchOptionalByExternalId(externalId).orElseThrow(() -> new IllegalArgumentException("report for external id %s not found".formatted(externalId)));

        if (coach.admin() || (report.getFinishedAt() == null && Objects.equals(report.getCoachId(), coach.id()))) {
            reportDao.delete(report);
        } else {
            log.error("user ({}) tried to delete video report ({}), but is not authorized to do so", coach, report);
            throw new IllegalStateException("user is not allowed to delete this video-report!");
        }
    }

    public void finishRefereeReport(String externalId, long userId) {
        var coach = userService.getById(userId);
        var report = reportDao.fetchOptionalByExternalId(externalId).orElseThrow(() -> new IllegalArgumentException("report for external id %s not found".formatted(externalId)));

        if (!report.getCoachId().equals(coach.id())) {
            throw new IllegalStateException("report does not belong to user %s!".formatted(coach.fullName()));
        }

        if (report.getFinishedAt() != null) {
            throw new IllegalStateException("user is not allowed to finish already finished video-report!");
        }

        log.info("finishing report {} by {}", report.getExternalId(), coach.fullName());
        report.setFinishedAt(DateUtil.now());
        report.setFinishedBy(coach.id());
        reportDao.update(report);

        if (!report.getInternal()) {
            mailService.sendFinishedReportMail(report);
            mailService.sendFinishedReportCopyMail(report);
        }
    }

    public CreateRefereeReportResultDTO copyReport(String externalId, CopyRefereeReportDTO dto, long userId) {
        var coach = userService.getById(userId);
        var report = reportDao.fetchOptionalByExternalId(externalId).orElseThrow(() -> new IllegalArgumentException("report for external id %s not found".formatted(externalId)));

        if (!report.getCoachId().equals(coach.id())) {
            throw new IllegalStateException("report does not belong to user %s!".formatted(coach.fullName()));
        }

        try {
            var newReport = createRefereeReport(report.getGameNumber(), report.getGameVideoUrl(), dto.reporteeId(), report.getInternal(), userId);

            // copy comments
            var sourceComments = reportCommentDao.fetchByReportId(report.getId()).stream()
                                                 .collect(toMap(ReportComment::getType, identity()));

            reportCommentDao.fetchByReportId(newReport.id()).forEach(it -> {
                var source = sourceComments.get(it.getType());
                it.setComment(source.getComment());
                // do not set score, only copy source comment
                reportCommentDao.update(it);
            });

            // copy source video-comments as references
            reportVideoCommentRefDao.insert(reportVideoCommentDao.fetchByReportId(report.getId()).stream()
                                                                 .map(it -> new ReportVideoCommentRef(newReport.id(), it.getId(), it.getRequiresReply()))
                                                                 .toList());

            return newReport;
        } catch (InvalidVideoUrlException e) {
            throw new IllegalStateException("could not copy report due to invalid video-URL in source report - this should never happen...");
        }
    }

    public void saveDiscussionReply(String externalId, CreateRefereeReportDiscussionReplyDTO dto, long userId) {
        var commenter = userService.getById(userId);
        var report = reportDao.fetchOptionalByExternalId(externalId).orElseThrow(() -> new IllegalArgumentException("report for external id %s not found".formatted(externalId)));

        // TODO(caspar) dürfen referee-coaches ach zu anderen reports schreiben?
        if (!Objects.equals(report.getCoachId(), commenter.id()) && !Objects.equals(report.getReporteeId(), commenter.id())) {
            throw new IllegalStateException("user %s is not allowed to reply to this report!".formatted(commenter.fullName()));
        }

        var reportVideoComments = reportVideoCommentDao.fetchByReportId(report.getId()).stream().collect(toMap(ReportVideoComment::getId, identity()));
        var reportVideoCommentRefs = reportVideoCommentRefDao.fetchByReportId(report.getId()).stream().collect(toMap(ReportVideoCommentRef::getReportVideoCommentId, identity()));

        var totalRepliesAdded = new AtomicInteger(0);
        var totalVideoCommentsAdded = new AtomicInteger(0);

        var reportIds = getRelevantReportIds(report.getGameNumber(), report.getCoachId());

        dto.replies().forEach(reply -> {
            if (isBlank(reply.reply())) {
                return;
            }

            if (reportVideoComments.containsKey(reply.commentId()) || reportVideoCommentRefs.containsKey(reply.commentId())) {
                reportVideoCommentReplyDao.insert(new ReportVideoCommentReply(null,
                                                                              reply.commentId(),
                                                                              reply.reply(),
                                                                              DateUtil.now(),
                                                                              commenter.id()));
                totalRepliesAdded.incrementAndGet();
            } else {
                log.error("reply to unknown comment id %d".formatted(reply.commentId()));
            }
        });

        dto.comments().forEach(comment -> {
            if (isBlank(comment.comment())) {
                return;
            }
            var newReportVideoComment = new ReportVideoComment(null,
                                                               report.getId(),
                                                               comment.timestampInSeconds(),
                                                               comment.comment(),
                                                               DateUtil.now(),
                                                               commenter.id(),
                                                               false,
                                                               null);
            reportVideoCommentDao.insert(newReportVideoComment);
            totalVideoCommentsAdded.incrementAndGet();

            // add references to other reports
            reportIds.stream()
                     .filter(id -> !Objects.equals(id, report.getId()))
                     .forEach(id -> reportVideoCommentRefDao.insert(new ReportVideoCommentRef(id, newReportVideoComment.getId(), false)));
        });

        if (totalRepliesAdded.get() > 0 || totalVideoCommentsAdded.get() > 0) {
            reportIds.stream()
                     .filter(id -> !Objects.equals(id, report.getId()))
                     .forEach(id -> {
                         var otherReport = reportDao.fetchOneById(id);
                         var referee = userService.getById(otherReport.getReporteeId());

                         mailService.sendNewDiscussionMail(commenter, referee, otherReport);
                     });

            if (!Objects.equals(report.getCoachId(), commenter.id())) {
                // send to coach as well
                var coach = userService.getById(report.getCoachId());
                mailService.sendNewDiscussionMail(commenter, coach, report);
            }
        }
    }

    /**
     * Register a coach-uploaded video snippet and hand back a short-lived presigned URL the browser uses to
     * PUT the file directly into the bucket. The upload is linked to a video comment later, when the report
     * is saved with a {@link ReportVideoCommentDTO} carrying the returned {@code uploadId}.
     */
    public VideoUploadDTO createVideoUpload(String externalId, CreateVideoUploadDTO dto, long userId) {
        var coach = userService.getById(userId);
        var report = reportDao.fetchOptionalByExternalId(externalId)
                              .orElseThrow(() -> new IllegalArgumentException("report for external id %s not found".formatted(externalId)));
        if (!report.getCoachId().equals(coach.id())) {
            throw new IllegalStateException("report does not belong to user %s!".formatted(coach.fullName()));
        }
        if (report.getFinishedAt() != null) {
            throw new IllegalStateException("user is not allowed to add uploads to an already finished video-report!");
        }
        if (dto.contentType() == null || !dto.contentType().startsWith("video/")) {
            throw new IllegalArgumentException("only video uploads are allowed");
        }
        var maxBytes = applicationProperties.getStorage().getMaxUploadBytes();
        if (dto.sizeBytes() == null || dto.sizeBytes() <= 0 || dto.sizeBytes() > maxBytes) {
            throw new IllegalArgumentException("invalid upload size (max %d bytes)".formatted(maxBytes));
        }

        var objectKey = buildObjectKey(report.getId(), dto.filename());
        var upload = new ReportVideoUpload(null, objectKey, dto.filename(), dto.contentType(), dto.sizeBytes(), false, DateUtil.now(), coach.id());
        reportVideoUploadDao.insert(upload);

        var uploadUrl = videoStorageService.createUploadUrl(objectKey, dto.contentType(), UPLOAD_URL_TTL);
        return new VideoUploadDTO(upload.getId(), uploadUrl);
    }

    /**
     * Confirm that a previously requested upload has actually landed in the bucket.
     */
    public void completeVideoUpload(String externalId, Long uploadId, long userId) {
        var coach = userService.getById(userId);
        var report = reportDao.fetchOptionalByExternalId(externalId)
                              .orElseThrow(() -> new IllegalArgumentException("report for external id %s not found".formatted(externalId)));
        if (!report.getCoachId().equals(coach.id())) {
            throw new IllegalStateException("report does not belong to user %s!".formatted(coach.fullName()));
        }
        var upload = reportVideoUploadDao.fetchOptionalById(uploadId)
                                         .orElseThrow(() -> new IllegalArgumentException("upload %d not found".formatted(uploadId)));
        if (!videoStorageService.exists(upload.getObjectKey())) {
            throw new IllegalStateException("uploaded file %s not found in storage".formatted(upload.getObjectKey()));
        }
        upload.setUploaded(true);
        reportVideoUploadDao.update(upload);
    }

    private void deleteVideoUpload(Long uploadId) {
        if (uploadId == null) {
            return;
        }
        reportVideoUploadDao.fetchOptionalById(uploadId).ifPresent(upload -> {
            try {
                videoStorageService.delete(upload.getObjectKey());
            } catch (RuntimeException e) {
                log.error("could not delete video upload object {} from storage", upload.getObjectKey(), e);
            }
            reportVideoUploadDao.deleteById(uploadId);
        });
    }

    private static String buildObjectKey(Long reportId, String filename) {
        var extension = "";
        if (filename != null) {
            var dot = filename.lastIndexOf('.');
            if (dot >= 0 && dot < filename.length() - 1) {
                extension = "." + filename.substring(dot + 1).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
            }
        }
        return "reports/%d/%s%s".formatted(reportId, UUID.randomUUID(), extension);
    }

    public Set<Long> getRelevantReportIds(String gameNumber, Long coachId) {
        return reportDao.fetchByGameNumber(gameNumber).stream()
                        .filter(report -> Objects.equals(report.getCoachId(), coachId))
                        .map(Report::getId)
                        .collect(toSet());
    }

    public static class InvalidVideoUrlException extends Exception {
    }
}
