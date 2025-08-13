package ch.refereecoach.probasket.service.report;

import ch.refereecoach.probasket.common.CategoryType;
import ch.refereecoach.probasket.common.CrtiteriaType;
import ch.refereecoach.probasket.dto.report.CreateRefereeReportResultDTO;
import ch.refereecoach.probasket.jooq.tables.daos.ReportCommentDao;
import ch.refereecoach.probasket.jooq.tables.daos.ReportCriteriaDao;
import ch.refereecoach.probasket.jooq.tables.daos.ReportDao;
import ch.refereecoach.probasket.jooq.tables.pojos.Report;
import ch.refereecoach.probasket.jooq.tables.pojos.ReportComment;
import ch.refereecoach.probasket.jooq.tables.pojos.ReportCriteria;
import ch.refereecoach.probasket.service.basketplan.BasketplanService;
import ch.refereecoach.probasket.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static ch.refereecoach.probasket.common.ReportType.REFEREE_COMMENT_REPORT;
import static ch.refereecoach.probasket.common.ReportType.REFEREE_VIDEO_REPORT;
import static org.apache.commons.lang3.StringUtils.toRootLowerCase;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportDao reportDao;
    private final ReportCommentDao reportCommentDao;
    private final ReportCriteriaDao reportCriteriaDao;
    private final BasketplanService basketplanService;
    private final UserService userService;

    public CreateRefereeReportResultDTO createRefereeReport(String gameNumber, String videoUrl, Long reporteeId, String username) {
        var coach = userService.getByBasketplanUsername(username);
        var reportee = userService.getById(reporteeId);

        var reportType = videoUrl == null ? REFEREE_COMMENT_REPORT : REFEREE_VIDEO_REPORT;
        if (!coach.hasRequiredRole(reportType)) {
            throw new IllegalStateException("user %s is not a coach!".formatted(coach.username()));
        }
        var game = basketplanService.findGameByNumber(gameNumber)
                                    .orElseThrow(() -> new IllegalArgumentException("game %s not found".formatted(gameNumber)));

        var report = new Report();
        report.setExternalId(getExternalId());
        report.setReportType(reportType.name());
        report.setCoachId(coach.id());
        report.setCoachName(coach.fullName());
        report.setReporteeId(reportee.id());
        report.setReporteeName(reportee.fullName());
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
                  var reportComment = new ReportComment(null, report.getId(), categoryType.name(), null, null);
                  reportCommentDao.insert(reportComment);
                  CrtiteriaType.forCategory(categoryType)
                               .forEach(criteriaType -> reportCriteriaDao.insert(new ReportCriteria(null, reportComment.getId(), criteriaType.name(), null, null)));
              });

        return new CreateRefereeReportResultDTO(report.getExternalId());
    }

    private String getExternalId() {
        String uuid;
        do {
            // insecure is good enough for this use-case
            uuid = toRootLowerCase(RandomStringUtils.insecure().nextAlphabetic(10));
        } while (reportDao.fetchOptionalByExternalId(uuid).isPresent());

        return uuid;
    }
}
