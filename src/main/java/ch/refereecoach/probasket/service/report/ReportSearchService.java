package ch.refereecoach.probasket.service.report;

import ch.refereecoach.probasket.common.ReportType;
import ch.refereecoach.probasket.dto.auth.UserDTO;
import ch.refereecoach.probasket.dto.report.ReportOverviewDTO;
import ch.refereecoach.probasket.dto.report.ReportSearchResultDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static ch.refereecoach.probasket.common.ReportType.GAME_DISCUSSION;
import static ch.refereecoach.probasket.jooq.tables.Report.REPORT;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportSearchService {

    private final DSLContext jooqDsl;
    private final UserService userService;

    public ReportSearchResultDTO search(LocalDate from, LocalDate to, String filter, int page, int pageSize, String username) {
        var user = userService.getByBasketplanUsername(username);

        // TODO add text filter..how? referee/coach need to be joined... or store the names directly in the report...

        var condition = REPORT.GAME_DATE.ge(from).and(REPORT.GAME_DATE.le(to));
        condition.and(getUserCondition(user));

        // TODO add filter, etc.

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
                                   "TODO" + it.getCoachId(),
                                   "TODO" + it.getReporteeId(),
                                   it.getFinishedAt() != null
                           ));

        var count = jooqDsl.fetchCount(REPORT, condition);

        return new ReportSearchResultDTO(items, count);
    }

    private Condition getUserCondition(UserDTO user) {
        if (user.admin()) {
            return DSL.trueCondition();
        }

        if (user.refereeCoach() && !user.referee()) {

        }

        if (user.refereeCoach() && user.referee()) {

        }

        if (user.referee()) {
            return DSL.or(REPORT.REPORTEE_ID.eq(user.id()).and(REPORT.FINISHED_AT.isNotNull()),
                          REPORT.REPORT_TYPE.eq(GAME_DISCUSSION.name()).and(
                                  DSL.or(REPORT.GAME_REFEREE1_ID.eq(user.id()),
                                         REPORT.GAME_REFEREE2_ID.eq(user.id()),
                                         REPORT.GAME_REFEREE3_ID.eq(user.id()))));
        }

        return DSL.falseCondition();
    }

//    return Stream.concat(getCoachingsStream(from, to), getGameDiscussionsStream(from, to))
//            // coach sees everything, referee-coach only own coachings, referee only own reports
//            .filter(overview -> user.isCoach()
//            || (user.isRefereeCoach() && Objects.equals(overview.getCoachId().orElse(0L), user.getId()))
//            || overview.relevantRefereeIds().contains(user.getId()))
//            // referee only sees finished reports
//            .filter(overview -> user.isCoach()
//            || (Objects.equals(overview.getCoachId().orElse(0L), user.getId()))
//            || overview.isVisibleForReferee())
//            .toList();

}
