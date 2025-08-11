package ch.refereecoach.probasket.service.report;

import ch.refereecoach.probasket.common.ReportType;
import ch.refereecoach.probasket.dto.report.ReportOverviewDTO;
import ch.refereecoach.probasket.dto.report.ReportSearchResultDTO;
import ch.refereecoach.probasket.jooq.tables.Report;
import ch.refereecoach.probasket.jooq.tables.daos.LoginDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static ch.refereecoach.probasket.jooq.tables.Report.REPORT;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportSearchService {

    private final DSLContext jooqDsl;
    private final LoginDao loginDao;

    public ReportSearchResultDTO search(LocalDate from, LocalDate to, String filter, int page, int pageSize, String username) {
        var user = loginDao.fetchOptionalByBasketplanUsername(username)
                           .orElseThrow(() -> new IllegalArgumentException("user %s not found".formatted(username)));

        var refereeCoach = user.getRefereeCoach();
        var trainerCoach = user.getTrainerCoach();

        var condition = DSL.trueCondition();

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
