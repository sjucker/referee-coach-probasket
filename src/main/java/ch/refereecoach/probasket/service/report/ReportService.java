package ch.refereecoach.probasket.service.report;

import ch.refereecoach.probasket.common.CategoryType;
import ch.refereecoach.probasket.common.CriteriaType;
import ch.refereecoach.probasket.dto.report.CopyRefereeReportDTO;
import ch.refereecoach.probasket.dto.report.CreateRefereeReportDiscussionReplyDTO;
import ch.refereecoach.probasket.dto.report.CreateRefereeReportResultDTO;
import ch.refereecoach.probasket.dto.report.RefereeReportDTO;
import ch.refereecoach.probasket.dto.report.ReportCommentDTO;
import ch.refereecoach.probasket.dto.report.ReportCriteriaDTO;
import ch.refereecoach.probasket.dto.report.ReportVideoCommentDTO;
import ch.refereecoach.probasket.jooq.tables.daos.ReportCommentDao;
import ch.refereecoach.probasket.jooq.tables.daos.ReportCriteriaDao;
import ch.refereecoach.probasket.jooq.tables.daos.ReportDao;
import ch.refereecoach.probasket.jooq.tables.daos.ReportVideoCommentDao;
import ch.refereecoach.probasket.jooq.tables.daos.ReportVideoCommentRefDao;
import ch.refereecoach.probasket.jooq.tables.daos.ReportVideoCommentReplyDao;
import ch.refereecoach.probasket.jooq.tables.daos.ReportVideoCommentTagDao;
import ch.refereecoach.probasket.jooq.tables.pojos.Report;
import ch.refereecoach.probasket.jooq.tables.pojos.ReportComment;
import ch.refereecoach.probasket.jooq.tables.pojos.ReportCriteria;
import ch.refereecoach.probasket.jooq.tables.pojos.ReportVideoComment;
import ch.refereecoach.probasket.jooq.tables.pojos.ReportVideoCommentRef;
import ch.refereecoach.probasket.jooq.tables.pojos.ReportVideoCommentReply;
import ch.refereecoach.probasket.service.basketplan.BasketplanService;
import ch.refereecoach.probasket.service.mail.MailService;
import ch.refereecoach.probasket.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.refereecoach.probasket.common.CriteriaState.NEUTRAL;
import static ch.refereecoach.probasket.common.ReportType.REFEREE_COMMENT_REPORT;
import static ch.refereecoach.probasket.common.ReportType.REFEREE_VIDEO_REPORT;
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

    private final ReportDao reportDao;
    private final ReportCommentDao reportCommentDao;
    private final ReportCriteriaDao reportCriteriaDao;
    private final ReportVideoCommentDao reportVideoCommentDao;
    private final ReportVideoCommentTagDao reportVideoCommentTagDao;
    private final ReportVideoCommentRefDao reportVideoCommentRefDao;
    private final ReportVideoCommentReplyDao reportVideoCommentReplyDao;
    private final BasketplanService basketplanService;
    private final UserService userService;
    private final MailService mailService;

    public CreateRefereeReportResultDTO createRefereeReport(String gameNumber, String videoUrl, Long reporteeId, String username) {
        var coach = userService.getByBasketplanUsername(username);
        var reportee = userService.getById(reporteeId);

        var reportType = videoUrl == null ? REFEREE_COMMENT_REPORT : REFEREE_VIDEO_REPORT;
        if (!coach.hasRequiredRole(reportType)) {
            throw new IllegalStateException("user %s is not a coach!".formatted(coach.username()));
        }
        var game = basketplanService.findGameByNumber(gameNumber)
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
        report.setGameReferee2Id(game.referee2Id());
        report.setGameReferee2Name(game.referee2Name());
        report.setGameReferee3Id(game.referee3Id());
        report.setGameReferee3Name(game.referee3Name());
        report.setGameVideoUrl(videoUrl);

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
                  var reportComment = new ReportComment(null, report.getId(), categoryType.name(), null, DEFAULT_SCORE);
                  reportCommentDao.insert(reportComment);
                  CriteriaType.forCategory(categoryType)
                              .forEach(criteriaType -> reportCriteriaDao.insert(new ReportCriteria(null, reportComment.getId(), criteriaType.name(), NEUTRAL.name())));
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

    public void updateRefereeReport(String externalId, RefereeReportDTO dto, String username) {
        var coach = userService.getByBasketplanUsername(username);
        var report = reportDao.fetchOptionalByExternalId(externalId).orElseThrow(() -> new IllegalArgumentException("report for external id %s not found".formatted(externalId)));
        if (!report.getCoachId().equals(coach.id())) {
            throw new IllegalStateException("report does not belong to user %s!".formatted(coach.username()));
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

        var videoCommentsToInsert = new ArrayList<ReportVideoComment>();
        var videoCommentsToUpdate = new HashMap<Long, ReportVideoCommentDTO>();
        var videoCommentRefsToUpdate = new HashMap<Long, ReportVideoCommentDTO>();
        dto.videoComments()
           .forEach(videoComment -> {
               if (videoComment.reference()) {
                   videoCommentRefsToUpdate.put(videoComment.id(), videoComment);
               } else if (videoComment.id() == null) {
                   // create
                   if (videoComment.timestampInSeconds() != null && isNotBlank(videoComment.comment())) {
                       videoCommentsToInsert.add(new ReportVideoComment(null, report.getId(), videoComment.timestampInSeconds(), videoComment.comment(), DateUtil.now(), coach.id(), videoComment.requiresReply()));
                   }
               } else {
                   // update
                   videoCommentsToUpdate.put(videoComment.id(), videoComment);
               }
           });
        reportVideoCommentDao.fetchByReportId(report.getId())
                             .forEach(reportVideoComment -> {
                                 var reportVideoCommentDTO = videoCommentsToUpdate.get(reportVideoComment.getId());
                                 if (reportVideoCommentDTO != null) {
                                     reportVideoComment.setTimestampInSeconds(reportVideoCommentDTO.timestampInSeconds());
                                     reportVideoComment.setComment(reportVideoCommentDTO.comment());
                                     reportVideoComment.setRequiresReply(reportVideoCommentDTO.requiresReply());
                                     reportVideoCommentDao.update(reportVideoComment);
                                 } else {
                                     reportVideoCommentDao.delete(reportVideoComment);
                                 }
                             });

        reportVideoCommentDao.insert(videoCommentsToInsert);

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

    public void finishRefereeReport(String externalId, String username) {
        var coach = userService.getByBasketplanUsername(username);
        var report = reportDao.fetchOptionalByExternalId(externalId).orElseThrow(() -> new IllegalArgumentException("report for external id %s not found".formatted(externalId)));

        if (!report.getCoachId().equals(coach.id())) {
            throw new IllegalStateException("report does not belong to user %s!".formatted(coach.username()));
        }

        if (report.getFinishedAt() != null) {
            throw new IllegalStateException("user is not allowed to finish already finished video-report!");
        }

        report.setFinishedAt(DateUtil.now());
        report.setFinishedBy(coach.id());
        reportDao.update(report);

        mailService.sendFinishedReportMail(report);
    }

    public CreateRefereeReportResultDTO copyReport(String externalId, CopyRefereeReportDTO dto, String username) {
        var coach = userService.getByBasketplanUsername(username);
        var report = reportDao.fetchOptionalByExternalId(externalId).orElseThrow(() -> new IllegalArgumentException("report for external id %s not found".formatted(externalId)));

        if (!report.getCoachId().equals(coach.id())) {
            throw new IllegalStateException("report does not belong to user %s!".formatted(coach.username()));
        }

        var newReport = createRefereeReport(report.getGameNumber(), report.getGameVideoUrl(), dto.reporteeId(), username);

        // copy source video-comments as references
        reportVideoCommentRefDao.insert(reportVideoCommentDao.fetchByReportId(report.getId()).stream()
                                                             .map(it -> new ReportVideoCommentRef(newReport.id(), it.getId(), it.getRequiresReply()))
                                                             .toList());

        return newReport;
    }

    public void saveDiscussionReply(String externalId, CreateRefereeReportDiscussionReplyDTO dto, String username) {
        var commenter = userService.getByBasketplanUsername(username);
        var report = reportDao.fetchOptionalByExternalId(externalId).orElseThrow(() -> new IllegalArgumentException("report for external id %s not found".formatted(externalId)));

        // TODO(caspar) dürfen referee-coaches ach zu anderen reports schreiben?
        if (!Objects.equals(report.getCoachId(), commenter.id()) && !Objects.equals(report.getReporteeId(), commenter.id())) {
            throw new IllegalStateException("user %s is not allowed to reply to this report!".formatted(commenter.username()));
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
                                                               false);
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

                         mailService.sendNewDiscussionMail(commenter.fullName(), referee, otherReport);
                     });

            if (!Objects.equals(report.getCoachId(), commenter.id())) {
                // send to coach as well
                var coach = userService.getById(report.getCoachId());
                mailService.sendNewDiscussionMail(commenter.fullName(), coach, report);
            }
        }
    }

    public Set<Long> getRelevantReportIds(String gameNumber, Long coachId) {
        return reportDao.fetchByGameNumber(gameNumber).stream()
                        .filter(report -> Objects.equals(report.getCoachId(), coachId))
                        .map(Report::getId)
                        .collect(toSet());
    }
}
