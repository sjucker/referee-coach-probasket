package ch.refereecoach.probasket.service.report;

import ch.refereecoach.probasket.jooq.tables.daos.ReportDao;
import ch.refereecoach.probasket.service.basketplan.BasketplanGameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportBasketplanService {

    private final ReportSearchService reportSearchService;
    private final BasketplanGameService basketplanGameService;
    private final ReportDao reportDao;

    @Scheduled(initialDelay = 2, fixedRate = 60, timeUnit = MINUTES)
    public void updateMissingScores() {
        log.info("checking reports that are missing final score");
        for (var report : reportSearchService.findReportsWithMissingResult()) {
            basketplanGameService.findGameByNumber(report.getGameNumber()).ifPresent(game -> {
                log.info("update result of report {} to {}", report.getId(), game.result());
                report.setGameResult(game.result());
                reportDao.update(report);
            });
        }
    }
}
