package ch.refereecoach.probasket.service.report;

import ch.refereecoach.probasket.common.ReportType;
import ch.refereecoach.probasket.dto.report.CreateRefereeReportResultDTO;
import ch.refereecoach.probasket.jooq.tables.daos.LoginDao;
import ch.refereecoach.probasket.jooq.tables.daos.ReportDao;
import ch.refereecoach.probasket.jooq.tables.pojos.Login;
import ch.refereecoach.probasket.jooq.tables.pojos.Report;
import ch.refereecoach.probasket.service.basketplan.BasketplanService;
import ch.refereecoach.probasket.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import static ch.refereecoach.probasket.common.ReportType.REFEREE_COMMENT_REPORT;
import static ch.refereecoach.probasket.common.ReportType.REFEREE_VIDEO_REPORT;
import static org.apache.commons.lang3.StringUtils.toRootLowerCase;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final LoginDao loginDao;
    private final ReportDao reportDao;
    private final BasketplanService basketplanService;

    public CreateRefereeReportResultDTO createRefereeReport(String gameNumber, String videoUrl, Long reporteeId, String username) {
        var coach = loginDao.fetchOptionalByBasketplanUsername(username)
                            .orElseThrow(() -> new IllegalArgumentException("user %s not found".formatted(username)));

        var reportType = videoUrl == null ? REFEREE_COMMENT_REPORT : REFEREE_VIDEO_REPORT;
        if (!hasRequiredRole(reportType, coach)) {
            throw new IllegalStateException("user %s is not a coach!".formatted(coach.getBasketplanUsername()));
        }
        var game = basketplanService.findGameByNumber(gameNumber)
                                    .orElseThrow(() -> new IllegalArgumentException("game %s not found".formatted(gameNumber)));

        var report = new Report();
        report.setExternalId(getExternalId());
        report.setReportType(reportType.name());
        report.setCoachId(coach.getId());
        report.setReporteeId(reporteeId);
        report.setGameNumber(game.gameNumber());
        report.setGameCompetition(game.competition());
        report.setGameDate(game.date());
        report.setGameResult(game.result());
        report.setGameHomeTeam(game.homeTeam());
        report.setGameHomeTeamId(game.homeTeamId());
        report.setGameGuestTeam(game.guestTeam());
        report.setGameGuestTeamId(game.guestTeamId());
        report.setGameReferee1Id(game.referee1Id());
        report.setGameReferee2Id(game.referee2Id());
        report.setGameReferee3Id(game.referee3Id());
        report.setGameVideoUrl(videoUrl);

        var now = DateUtil.now();
        report.setCreatedAt(now);
        report.setCreatedBy(coach.getId());
        report.setUpdatedAt(now);
        report.setUpdatedBy(coach.getId());
        report.setFinishedAt(null);
        report.setFinishedBy(null);
        report.setReminderSent(false);

        reportDao.insert(report);

        return new CreateRefereeReportResultDTO(report.getExternalId());
    }

    private boolean hasRequiredRole(ReportType reportType, Login login) {
        return switch (reportType) {
            case REFEREE_VIDEO_REPORT, REFEREE_COMMENT_REPORT -> login.getRefereeCoach();
            case TRAINER_REPORT -> login.getTrainer();
        };
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
