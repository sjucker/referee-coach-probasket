package ch.refereecoach.probasket.service.report;

import ch.refereecoach.probasket.common.Rank;
import ch.refereecoach.probasket.common.ReportType;
import ch.refereecoach.probasket.dto.auth.UserDTO;
import ch.refereecoach.probasket.dto.basketplan.BasketplanGameDTO;
import ch.refereecoach.probasket.dto.report.RefereeDTO;
import ch.refereecoach.probasket.dto.report.RefereeReportDTO;
import ch.refereecoach.probasket.dto.report.ReportCommentDTO;
import ch.refereecoach.probasket.dto.report.ReportCriteriaDTO;
import ch.refereecoach.probasket.dto.report.ReportOverviewDTO;
import ch.refereecoach.probasket.dto.report.ReportSearchResultDTO;
import ch.refereecoach.probasket.dto.report.ReportVideoCommentDTO;
import ch.refereecoach.probasket.dto.report.ReportVideoCommentReplyDTO;
import ch.refereecoach.probasket.dto.report.TagDTO;
import ch.refereecoach.probasket.jooq.tables.daos.ReportDao;
import ch.refereecoach.probasket.jooq.tables.pojos.Report;
import ch.refereecoach.probasket.util.AsportUtil;
import ch.refereecoach.probasket.util.DateUtil;
import ch.refereecoach.probasket.util.YouTubeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static ch.refereecoach.probasket.common.OfficiatingMode.OFFICIATING_2PO;
import static ch.refereecoach.probasket.common.OfficiatingMode.OFFICIATING_3PO;
import static ch.refereecoach.probasket.common.Rank.RK;
import static ch.refereecoach.probasket.common.ReportType.GAME_DISCUSSION;
import static ch.refereecoach.probasket.common.ReportType.REFEREE_COMMENT_REPORT;
import static ch.refereecoach.probasket.common.ReportType.REFEREE_VIDEO_REPORT;
import static ch.refereecoach.probasket.common.ReportType.TRAINER_REPORT;
import static ch.refereecoach.probasket.jooq.Tables.LOGIN;
import static ch.refereecoach.probasket.jooq.Tables.REPORT_COMMENT;
import static ch.refereecoach.probasket.jooq.Tables.REPORT_CRITERIA;
import static ch.refereecoach.probasket.jooq.Tables.REPORT_VIDEO_COMMENT;
import static ch.refereecoach.probasket.jooq.Tables.REPORT_VIDEO_COMMENT_REF;
import static ch.refereecoach.probasket.jooq.Tables.REPORT_VIDEO_COMMENT_REPLY;
import static ch.refereecoach.probasket.jooq.Tables.REPORT_VIDEO_COMMENT_TAG;
import static ch.refereecoach.probasket.jooq.Tables.TAG;
import static ch.refereecoach.probasket.jooq.tables.Report.REPORT;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.notExists;
import static org.jooq.impl.DSL.select;
import static org.jooq.impl.DSL.selectOne;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportSearchService {

    private final DSLContext jooqDsl;
    private final ReportDao reportDao;
    private final UserService userService;

    public Optional<RefereeReportDTO> findRefereeReportByExternalId(String externalId, Long userId) {
        var user = userService.getById(userId);

        if (reportDao.fetchOptionalByExternalId(externalId).isEmpty()) {
            return Optional.empty();
        }

        if (jooqDsl.selectOne()
                   .from(REPORT)
                   .where(REPORT.EXTERNAL_ID.eq(externalId).and(getUserCondition(user)))
                   .fetchOptional()
                   .isEmpty()) {
            log.error("user {} tried to access report {} but is not allowed to!", user.fullName(), externalId);
            throw new IllegalArgumentException("not allowed!");
        }

        return jooqDsl.select(REPORT,
                              multiset(
                                      select(REPORT_COMMENT.ID,
                                             REPORT_COMMENT.TYPE,
                                             REPORT_COMMENT.COMMENT,
                                             REPORT_COMMENT.SCORE,
                                             REPORT.REPORTEE_RANK,
                                             REPORT.GAME_REFEREE3_ID,
                                             multiset(select(REPORT_CRITERIA.ID,
                                                             REPORT_CRITERIA.TYPE,
                                                             REPORT_CRITERIA.STATE
                                                            )
                                                              .from(REPORT_CRITERIA)
                                                              .where(REPORT_CRITERIA.REPORT_COMMENT_ID.eq(REPORT_COMMENT.ID))
                                                              .orderBy(REPORT_CRITERIA.ID.asc())
                                                     )
                                                     .convertFrom(it -> it.map(mapping(ReportCriteriaDTO::of))))
                                              .from(REPORT_COMMENT)
                                              .where(REPORT_COMMENT.REPORT_ID.eq(REPORT.ID))
                                              .orderBy(REPORT_COMMENT.ID.asc())
                                      ).convertFrom(it -> it.map(mapping(ReportCommentDTO::of))),
                              multiset(
                                      select(REPORT_VIDEO_COMMENT.ID,
                                             REPORT_VIDEO_COMMENT.TIMESTAMP_IN_SECONDS,
                                             REPORT_VIDEO_COMMENT.COMMENT,
                                             REPORT_VIDEO_COMMENT.CREATED_AT,
                                             LOGIN.ID,
                                             LOGIN.FIRSTNAME,
                                             LOGIN.LASTNAME,
                                             REPORT_VIDEO_COMMENT.REQUIRES_REPLY,
                                             multiset(select(REPORT_VIDEO_COMMENT_REPLY.ID,
                                                             REPORT_VIDEO_COMMENT_REPLY.REPLY,
                                                             REPORT_VIDEO_COMMENT_REPLY.CREATED_AT,
                                                             LOGIN.ID,
                                                             LOGIN.FIRSTNAME,
                                                             LOGIN.LASTNAME)
                                                              .from(REPORT_VIDEO_COMMENT_REPLY)
                                                              .join(LOGIN).on(REPORT_VIDEO_COMMENT_REPLY.CREATED_BY.eq(LOGIN.ID))
                                                              .where(REPORT_VIDEO_COMMENT_REPLY.REPORT_VIDEO_COMMENT_ID.eq(REPORT_VIDEO_COMMENT.ID))
                                                              .orderBy(REPORT_VIDEO_COMMENT_REPLY.CREATED_AT.asc())
                                                     ).convertFrom(it -> it.map(mapping(ReportVideoCommentReplyDTO::of))),
                                             multiset(select(TAG.ID,
                                                             TAG.NAME)
                                                              .from(REPORT_VIDEO_COMMENT_TAG)
                                                              .join(TAG).on(REPORT_VIDEO_COMMENT_TAG.TAG_ID.eq(TAG.ID))
                                                              .where(REPORT_VIDEO_COMMENT_TAG.REPORT_VIDEO_COMMENT_ID.eq(REPORT_VIDEO_COMMENT.ID))
                                                     ).convertFrom(it -> it.map(mapping(TagDTO::new)))
                                            )
                                              .from(REPORT_VIDEO_COMMENT)
                                              .join(LOGIN).on(LOGIN.ID.eq(REPORT_VIDEO_COMMENT.CREATED_BY))
                                              .where(REPORT_VIDEO_COMMENT.REPORT_ID.eq(REPORT.ID))
                                              .orderBy(REPORT_VIDEO_COMMENT.ID.asc())
                                      ).convertFrom(it -> it.map(mapping(ReportVideoCommentDTO::of))),
                              multiset(
                                      select(REPORT_VIDEO_COMMENT.ID,
                                             REPORT_VIDEO_COMMENT.TIMESTAMP_IN_SECONDS,
                                             REPORT_VIDEO_COMMENT.COMMENT,
                                             REPORT_VIDEO_COMMENT.CREATED_AT,
                                             LOGIN.ID,
                                             LOGIN.FIRSTNAME,
                                             LOGIN.LASTNAME,
                                             REPORT_VIDEO_COMMENT_REF.REQUIRES_REPLY,
                                             multiset(select(REPORT_VIDEO_COMMENT_REPLY.ID,
                                                             REPORT_VIDEO_COMMENT_REPLY.REPLY,
                                                             REPORT_VIDEO_COMMENT_REPLY.CREATED_AT,
                                                             LOGIN.ID,
                                                             LOGIN.FIRSTNAME,
                                                             LOGIN.LASTNAME)
                                                              .from(REPORT_VIDEO_COMMENT_REPLY)
                                                              .join(LOGIN).on(REPORT_VIDEO_COMMENT_REPLY.CREATED_BY.eq(LOGIN.ID))
                                                              .where(REPORT_VIDEO_COMMENT_REPLY.REPORT_VIDEO_COMMENT_ID.eq(REPORT_VIDEO_COMMENT.ID))
                                                              .orderBy(REPORT_VIDEO_COMMENT_REPLY.CREATED_AT.asc())
                                                     ).convertFrom(it -> it.map(mapping(ReportVideoCommentReplyDTO::of))),
                                             multiset(select(TAG.ID,
                                                             TAG.NAME)
                                                              .from(REPORT_VIDEO_COMMENT_TAG)
                                                              .join(TAG).on(REPORT_VIDEO_COMMENT_TAG.TAG_ID.eq(TAG.ID))
                                                              .where(REPORT_VIDEO_COMMENT_TAG.REPORT_VIDEO_COMMENT_ID.eq(REPORT_VIDEO_COMMENT.ID))
                                                     ).convertFrom(it -> it.map(mapping(TagDTO::new)))
                                            )
                                              .from(REPORT_VIDEO_COMMENT)
                                              .join(REPORT_VIDEO_COMMENT_REF).on(REPORT_VIDEO_COMMENT.ID.eq(REPORT_VIDEO_COMMENT_REF.REPORT_VIDEO_COMMENT_ID))
                                              .join(LOGIN).on(LOGIN.ID.eq(REPORT_VIDEO_COMMENT.CREATED_BY))
                                              .where(REPORT_VIDEO_COMMENT_REF.REPORT_ID.eq(REPORT.ID))
                                              .orderBy(REPORT_VIDEO_COMMENT.ID.asc())
                                      ).convertFrom(it -> it.map(mapping(ReportVideoCommentDTO::ofReference))))
                      .from(REPORT)
                      .where(REPORT.EXTERNAL_ID.eq(externalId))
                      .fetchOptional(it -> {
                          var reportRecord = it.value1();
                          var comments = it.value2();

                          var videoComments = Stream.concat(it.value3().stream(), it.value4().stream())
                                                    .sorted(comparing(ReportVideoCommentDTO::id))
                                                    .toList();

                          return new RefereeReportDTO(reportRecord.getId(),
                                                      reportRecord.getExternalId(),
                                                      ReportType.valueOf(reportRecord.getReportType()),
                                                      reportRecord.getCoachId(),
                                                      reportRecord.getCoachName(),
                                                      reportRecord.getReporteeId(),
                                                      reportRecord.getReporteeName(),
                                                      Rank.of(reportRecord.getReporteeRank()).orElse(RK),
                                                      new BasketplanGameDTO(reportRecord.getGameNumber(),
                                                                            reportRecord.getGameCompetition(),
                                                                            reportRecord.getGameDate(),
                                                                            reportRecord.getGameResult(),
                                                                            reportRecord.getGameHomeTeam(),
                                                                            reportRecord.getGameHomeTeamId(),
                                                                            reportRecord.getGameGuestTeam(),
                                                                            reportRecord.getGameGuestTeamId(),
                                                                            reportRecord.getGameReferee3Id() != null ? OFFICIATING_3PO : OFFICIATING_2PO,
                                                                            reportRecord.getGameReferee1Id(),
                                                                            reportRecord.getGameReferee1Name(),
                                                                            Rank.of(reportRecord.getGameReferee1Rank()).orElse(null),
                                                                            reportRecord.getGameReferee2Id(),
                                                                            reportRecord.getGameReferee2Name(),
                                                                            Rank.of(reportRecord.getGameReferee2Rank()).orElse(null),
                                                                            reportRecord.getGameReferee3Id(),
                                                                            reportRecord.getGameReferee3Name(),
                                                                            Rank.of(reportRecord.getGameReferee3Rank()).orElse(null),
                                                                            reportRecord.getGameVideoUrl()),
                                                      YouTubeUtil.parseYouTubeId(reportRecord.getGameVideoUrl()).orElse(null),
                                                      AsportUtil.parseAsportEventId(reportRecord.getGameVideoUrl()).orElse(null),
                                                      reportRecord.getOverallScore(),
                                                      reportRecord.getInternal(),
                                                      reportRecord.getFinishedAt() != null,
                                                      user.id().equals(reportRecord.getReporteeId()),
                                                      comments,
                                                      videoComments);
                      });
    }

    // TODO findTrainerReport
    // TODO findGameDiscussion

    public ReportSearchResultDTO search(LocalDate from, LocalDate to, String filter, int page, int pageSize, String sortBy, String sortOrder, Long userId) {
        var stopWatch = new StopWatch();

        var user = userService.getById(userId);

        var condition = REPORT.GAME_DATE.ge(from).and(REPORT.GAME_DATE.le(to));
        if (isNotBlank(filter)) {
            condition = condition.and(REPORT.COACH_NAME.containsIgnoreCase(filter)
                                                       .or(REPORT.REPORTEE_NAME.containsIgnoreCase(filter))
                                                       .or(REPORT.GAME_HOME_TEAM.containsIgnoreCase(filter))
                                                       .or(REPORT.GAME_GUEST_TEAM.containsIgnoreCase(filter)));
        }
        condition = condition.and(getUserCondition(user));

        var items = jooqDsl.selectFrom(REPORT)
                           .where(condition)
                           .orderBy(getSortOrder(sortBy, sortOrder))
                           .offset(page * pageSize)
                           .limit(pageSize)
                           .fetch(it -> new ReportOverviewDTO(
                                   it.getExternalId(),
                                   ReportType.valueOf(it.getReportType()),
                                   it.getGameDate(),
                                   it.getGameNumber(),
                                   it.getGameCompetition(),
                                   "%s - %s".formatted(it.getGameHomeTeam(), it.getGameGuestTeam()),
                                   it.getCoachName(),
                                   it.getCoachId(),
                                   it.getReporteeName(),
                                   it.getReporteeId(),
                                   RefereeDTO.of(it.getGameReferee1Id(), it.getGameReferee1Name()).orElse(null),
                                   RefereeDTO.of(it.getGameReferee2Id(), it.getGameReferee2Name()).orElse(null),
                                   RefereeDTO.of(it.getGameReferee3Id(), it.getGameReferee3Name()).orElse(null),
                                   it.getInternal(),
                                   it.getFinishedAt() != null,
                                   Objects.equals(user.id(), it.getCoachId())
                           ));

        var count = jooqDsl.fetchCount(REPORT, condition);

        log.info("search for from={}, to={}, filter={}, page={}, pageSize={}, user={} took {}",
                 from, to, filter, page, pageSize, user.fullName(), stopWatch);

        return new ReportSearchResultDTO(items, count);
    }

    private static SortField<?>[] getSortOrder(String sortBy, String sortOrder) {
        boolean desc = "desc".equalsIgnoreCase(sortOrder);
        return switch (sortBy) {
            case "gameNumber" -> desc ? new SortField[]{REPORT.GAME_NUMBER.desc()} : new SortField[]{REPORT.GAME_NUMBER.asc()};
            case "coach" -> desc ? new SortField[]{REPORT.COACH_NAME.desc()} : new SortField[]{REPORT.COACH_NAME.asc()};
            case "reportee" -> desc ? new SortField[]{REPORT.REPORTEE_NAME.desc()} : new SortField[]{REPORT.REPORTEE_NAME.asc()};
            default -> desc ? new SortField[]{REPORT.GAME_DATE.desc(), REPORT.GAME_NUMBER.asc()} : new SortField[]{REPORT.GAME_DATE.asc(), REPORT.GAME_NUMBER.asc()};
        };
    }

    private Condition getUserCondition(UserDTO user) {
        if (user.admin()) {
            return DSL.trueCondition();
        }

        if (user.refereeCoach()) {
            // coach (that is not also a referee) sees all his/her reports and all other finished ones
            if (!user.referee() || user.refereeCoachPlus()) {
                return REPORT.COACH_ID.eq(user.id())
                                      .or(REPORT.REPORT_TYPE.in(REFEREE_COMMENT_REPORT.name(), REFEREE_VIDEO_REPORT.name()).and(REPORT.FINISHED_AT.isNotNull()));
            } else {
                // coach that is also referee sees only his/her reports and all finished ones as reportee, and all game-discussion where he/she is involved
                return DSL.or(REPORT.COACH_ID.eq(user.id()),
                              reporteeCondition(user),
                              gameDiscussionCondition(user));
            }
        }

        if (user.referee()) {
            return DSL.or(reporteeCondition(user),
                          gameDiscussionCondition(user));
        }

        if (user.trainerCoach()) {
            if (!user.trainer()) {
                return REPORT.COACH_ID.eq(user.id())
                                      .or(REPORT.REPORT_TYPE.eq(TRAINER_REPORT.name()).and(REPORT.FINISHED_AT.isNotNull()));
            } else {
                return DSL.or(REPORT.COACH_ID.eq(user.id()),
                              reporteeCondition(user));
            }
        }

        if (user.trainer()) {
            return reporteeCondition(user);
        }

        return DSL.falseCondition();
    }

    private static Condition reporteeCondition(UserDTO user) {
        return REPORT.REPORTEE_ID.eq(user.id()).and(REPORT.FINISHED_AT.isNotNull()).and(REPORT.INTERNAL.isFalse());
    }

    private static Condition gameDiscussionCondition(UserDTO user) {
        return REPORT.REPORT_TYPE.eq(GAME_DISCUSSION.name())
                                 .and(DSL.or(REPORT.GAME_REFEREE1_ID.eq(user.id()),
                                             REPORT.GAME_REFEREE2_ID.eq(user.id()),
                                             REPORT.GAME_REFEREE3_ID.eq(user.id())));
    }

    public Set<Long> findRefereeReportIdsWithMissingReplies() {
        var dateTime = DateUtil.now().minusDays(2);
        var reportIds1 = jooqDsl.selectDistinct(REPORT.ID)
                                .from(REPORT)
                                .join(REPORT_VIDEO_COMMENT).on(REPORT.ID.eq(REPORT_VIDEO_COMMENT.REPORT_ID))
                                .where(REPORT.REPORT_TYPE.eq(REFEREE_VIDEO_REPORT.name()),
                                       REPORT.FINISHED_AT.isNotNull(),
                                       REPORT.FINISHED_AT.lt(dateTime),
                                       REPORT.REMINDER_SENT.isFalse(),
                                       REPORT_VIDEO_COMMENT.REQUIRES_REPLY.isTrue(),
                                       notExists(selectOne().from(REPORT_VIDEO_COMMENT_REPLY)
                                                            .where(REPORT_VIDEO_COMMENT_REPLY.REPORT_VIDEO_COMMENT_ID.eq(REPORT_VIDEO_COMMENT.ID),
                                                                   REPORT_VIDEO_COMMENT_REPLY.CREATED_BY.eq(REPORT.REPORTEE_ID))))
                                .fetch(it -> it.get(REPORT.ID));

        // check also for reference-comments
        var reportIds2 = jooqDsl.selectDistinct(REPORT.ID)
                                .from(REPORT)
                                .join(REPORT_VIDEO_COMMENT_REF).on(REPORT_VIDEO_COMMENT_REF.REPORT_ID.eq(REPORT.ID))
                                .join(REPORT_VIDEO_COMMENT).on(REPORT_VIDEO_COMMENT_REF.REPORT_VIDEO_COMMENT_ID.eq(REPORT_VIDEO_COMMENT.ID))
                                .where(REPORT.REPORT_TYPE.eq(REFEREE_VIDEO_REPORT.name()),
                                       REPORT.FINISHED_AT.isNotNull(),
                                       REPORT.FINISHED_AT.lt(dateTime),
                                       REPORT.REMINDER_SENT.isFalse(),
                                       REPORT_VIDEO_COMMENT_REF.REQUIRES_REPLY.isTrue(),
                                       notExists(selectOne().from(REPORT_VIDEO_COMMENT_REPLY)
                                                            .where(REPORT_VIDEO_COMMENT_REPLY.REPORT_VIDEO_COMMENT_ID.eq(REPORT_VIDEO_COMMENT.ID),
                                                                   REPORT_VIDEO_COMMENT_REPLY.CREATED_BY.eq(REPORT.REPORTEE_ID))))
                                .fetch(it -> it.get(REPORT.ID));

        return Stream.concat(reportIds1.stream(), reportIds2.stream()).collect(toSet());
    }

    public List<Report> findReportsWithMissingResult() {
        return jooqDsl.selectFrom(REPORT)
                      .where(REPORT.FINISHED_AT.isNotNull(),
                             REPORT.GAME_RESULT.contains("?"))
                      .fetchInto(Report.class);
    }
}
