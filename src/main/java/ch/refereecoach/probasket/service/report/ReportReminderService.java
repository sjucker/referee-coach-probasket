package ch.refereecoach.probasket.service.report;

import ch.refereecoach.probasket.jooq.tables.daos.ReportDao;
import ch.refereecoach.probasket.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportReminderService {

    private final ReportSearchService reportSearchService;
    private final ReportDao reportDao;
    private final UserService userService;
    private final MailService mailService;

    @Scheduled(initialDelay = 1, fixedRate = 60, timeUnit = MINUTES)
    public void sendReminderEmails() {
        for (var reportId : reportSearchService.findRefereeReportIdsWithMissingReplies()) {

            var report = reportDao.findOptionalById(reportId).orElseThrow();
            var reportee = userService.getById(report.getReporteeId());

            mailService.sendReminderMail(reportee, report);

            report.setReminderSent(true);
            reportDao.update(report);
        }
    }
}
