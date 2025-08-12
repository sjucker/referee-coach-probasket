package ch.refereecoach.probasket.service.report;

import ch.refereecoach.probasket.common.ReportType;
import ch.refereecoach.probasket.dto.auth.UserDTO;
import ch.refereecoach.probasket.dto.report.ReportDTO;
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

import static ch.refereecoach.probasket.common.ReportType.GAME_DISCUSSION;
import static ch.refereecoach.probasket.common.ReportType.REFEREE_COMMENT_REPORT;
import static ch.refereecoach.probasket.common.ReportType.REFEREE_VIDEO_REPORT;
import static ch.refereecoach.probasket.common.ReportType.TRAINER_REPORT;
import static ch.refereecoach.probasket.jooq.tables.Report.REPORT;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportSearchService {

    private final DSLContext jooqDsl;
    private final ReportDao reportDao;
    private final UserService userService;

    public Optional<ReportDTO> findByExternalId(String externalId, String username) {
        var user = userService.getByBasketplanUsername(username);
        return reportDao.fetchOptionalByExternalId(externalId)
                        // check if user is allowed to open report
                        .filter(it -> jooqDsl.selectOne()
                                             .from(REPORT)
                                             .where(REPORT.ID.eq(it.getId()).and(getUserCondition(user)))
                                             .fetchOptional()
                                             .isPresent())
                        .map(it -> new ReportDTO(it.getId(), it.getExternalId()));
    }

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
