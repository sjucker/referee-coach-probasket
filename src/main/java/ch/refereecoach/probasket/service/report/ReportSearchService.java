package ch.refereecoach.probasket.service.report;

import ch.refereecoach.probasket.common.ReportType;
import ch.refereecoach.probasket.dto.auth.UserDTO;
import ch.refereecoach.probasket.dto.basketplan.BasketplanGameDTO;
import ch.refereecoach.probasket.dto.report.RefereeReportDTO;
import ch.refereecoach.probasket.dto.report.ReportCommentDTO;
import ch.refereecoach.probasket.dto.report.ReportCriteriaDTO;
import ch.refereecoach.probasket.dto.report.ReportOverviewDTO;
import ch.refereecoach.probasket.dto.report.ReportSearchResultDTO;
import ch.refereecoach.probasket.jooq.tables.daos.ReportDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

import static ch.refereecoach.probasket.common.OfficiatingMode.OFFICIATING_2PO;
import static ch.refereecoach.probasket.common.OfficiatingMode.OFFICIATING_3PO;
import static ch.refereecoach.probasket.common.ReportType.GAME_DISCUSSION;
import static ch.refereecoach.probasket.common.ReportType.REFEREE_COMMENT_REPORT;
import static ch.refereecoach.probasket.common.ReportType.REFEREE_VIDEO_REPORT;
import static ch.refereecoach.probasket.common.ReportType.TRAINER_REPORT;
import static ch.refereecoach.probasket.jooq.Tables.REPORT_COMMENT;
import static ch.refereecoach.probasket.jooq.Tables.REPORT_CRITERIA;
import static ch.refereecoach.probasket.jooq.tables.Report.REPORT;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.jooq.Records.mapping;
import static org.jooq.impl.DSL.multiset;
import static org.jooq.impl.DSL.select;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportSearchService {

    private final DSLContext jooqDsl;
    private final ReportDao reportDao;
    private final UserService userService;

    public Optional<RefereeReportDTO> findRefereeReportByExternalId(String externalId, String username) {
        var user = userService.getByBasketplanUsername(username);

        if (reportDao.fetchOptionalByExternalId(externalId).isEmpty()) {
            return Optional.empty();
        }

        if (jooqDsl.selectOne()
                   .from(REPORT)
                   .where(REPORT.EXTERNAL_ID.eq(externalId).and(getUserCondition(user)))
                   .fetchOptional()
                   .isEmpty()) {
            log.error("user {} tried to access report {} but is not allowed to!", username, externalId);
            throw new IllegalArgumentException("not allowed!");
        }

        return jooqDsl.select(REPORT,
                              multiset(
                                      select(REPORT_COMMENT.ID,
                                             REPORT_COMMENT.TYPE,
                                             REPORT_COMMENT.COMMENT,
                                             REPORT_COMMENT.SCORE,
                                             multiset(select(REPORT_CRITERIA.ID,
                                                             REPORT_CRITERIA.TYPE,
                                                             REPORT_CRITERIA.COMMENT,
                                                             REPORT_CRITERIA.STATE
                                                            )
                                                              .from(REPORT_CRITERIA)
                                                              .where(REPORT_CRITERIA.REPORT_COMMENT_ID.eq(REPORT_COMMENT.ID)))
                                                     .convertFrom(it -> it.map(mapping(ReportCriteriaDTO::of))))
                                              .from(REPORT_COMMENT)
                                              .where(REPORT_COMMENT.REPORT_ID.eq(REPORT.ID))
                                      ).convertFrom(it -> it.map(mapping(ReportCommentDTO::of))))
                      .from(REPORT)
                      .where(REPORT.EXTERNAL_ID.eq(externalId))
                      .fetchOptional(it -> {
                          var reportRecord = it.value1();
                          return new RefereeReportDTO(reportRecord.getId(),
                                                      reportRecord.getExternalId(),
                                                      reportRecord.getCoachId(),
                                                      reportRecord.getCoachName(),
                                                      reportRecord.getReporteeId(),
                                                      reportRecord.getReporteeName(),
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
                                                                            reportRecord.getGameReferee2Id(),
                                                                            reportRecord.getGameReferee2Name(),
                                                                            reportRecord.getGameReferee3Id(),
                                                                            reportRecord.getGameReferee3Name(),
                                                                            reportRecord.getGameVideoUrl()),
                                                      it.value2());
                      });
    }

    // TODO findTrainerReport
    // TODO findGameDiscussion

    public ReportSearchResultDTO search(LocalDate from, LocalDate to, String filter, int page, int pageSize, String username) {
        var stopWatch = new StopWatch();

        var user = userService.getByBasketplanUsername(username);

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
                           .orderBy(REPORT.GAME_DATE.desc(), REPORT.GAME_NUMBER.asc(), REPORT.REPORTEE_ID.asc())
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
                                   it.getReporteeName(),
                                   it.getFinishedAt() != null
                           ));

        var count = jooqDsl.fetchCount(REPORT, condition);

        log.info("search for from={}, to={}, filter={}, page={}, pageSize={}, username={} took {}",
                 from, to, filter, page, pageSize, username, stopWatch);

        return new ReportSearchResultDTO(items, count);
    }

    private Condition getUserCondition(UserDTO user) {
        if (user.admin()) {
            return DSL.trueCondition();
        }

        if (user.refereeCoach()) {
            // coach (that is not also a referee) sees all his/her reports and all other finished ones
            if (!user.referee()) {
                return REPORT.COACH_ID.eq(user.id())
                                      .or(REPORT.REPORT_TYPE.in(REFEREE_COMMENT_REPORT.name(), REFEREE_VIDEO_REPORT.name()).and(REPORT.FINISHED_AT.isNotNull()));
            } else {
                // referee-coach sees only his/her reports and all finished ones as reportee, and all game-discussion where he/she is involved
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
        return REPORT.REPORTEE_ID.eq(user.id()).and(REPORT.FINISHED_AT.isNotNull());
    }

    private static Condition gameDiscussionCondition(UserDTO user) {
        return REPORT.REPORT_TYPE.eq(GAME_DISCUSSION.name())
                                 .and(DSL.or(REPORT.GAME_REFEREE1_ID.eq(user.id()),
                                             REPORT.GAME_REFEREE2_ID.eq(user.id()),
                                             REPORT.GAME_REFEREE3_ID.eq(user.id())));
    }

}
