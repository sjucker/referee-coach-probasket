package ch.refereecoach.probasket.service.mail;

import ch.refereecoach.probasket.common.ReportType;
import ch.refereecoach.probasket.configuration.ApplicationProperties;
import ch.refereecoach.probasket.dto.auth.UserDTO;
import ch.refereecoach.probasket.jooq.tables.pojos.Report;
import ch.refereecoach.probasket.service.report.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final ApplicationProperties properties;
    private final Environment environment;
    private final UserService userService;

    public void sendFinishedReportMail(Report report) {
        var simpleMessage = new SimpleMailMessage();
        try {
            var coach = userService.findById(report.getCoachId()).orElseThrow(() -> new IllegalArgumentException("coach id not found: %d".formatted(report.getCoachId())));
            var reportee = userService.findById(report.getReporteeId()).orElseThrow(() -> new IllegalArgumentException("reportee not found: %d".formatted(report.getReporteeId())));

            log.info("finishing referee-report with ID {}, send email to {}", report.getId(), reportee.email());

            var reportType = ReportType.valueOf(report.getReportType());
            simpleMessage.setSubject(getSubject(reportType));
            simpleMessage.setFrom(environment.getRequiredProperty("spring.mail.username"));
            simpleMessage.setReplyTo(coach.email());
            simpleMessage.setBcc(properties.getBccMail());

            if (properties.isOverrideRecipient()) {
                var to = properties.getOverrideRecipientMail();
                if (isBlank(to)) {
                    to = coach.email();
                }
                simpleMessage.setTo(to);
                simpleMessage.setSubject(simpleMessage.getSubject() + " (%s)".formatted(reportee.email()));
                log.info("override recipient mail: {}", to);
            } else {
                simpleMessage.setTo(reportee.email());
                // make sure that copy-receiver does not receive mail twice when he is the coach
                simpleMessage.setCc(Stream.of(coach.email(), properties.getCcMail())
                                          .distinct()
                                          .toArray(String[]::new));
            }

            simpleMessage.setText(getText(reportType, reportee, report.getExternalId()));

            mailSender.send(simpleMessage);
        } catch (MailException e) {
            log.error("could not send email to: " + Arrays.toString(simpleMessage.getTo()), e);
        }
    }

    private String getText(ReportType reportType, UserDTO reportee, String externalId) {
        return switch (reportType) {
            case REFEREE_VIDEO_REPORT -> """
                    Hi %s
                    
                    Your video report is ready.
                    
                    Please visit: %s/#/view/%s
                    
                    For discussion of the comments, use the following: %s/#/discuss/%s"
                    """.formatted(reportee.firstName(),
                                  properties.getBaseUrl(),
                                  externalId,
                                  properties.getBaseUrl(),
                                  externalId);
            case REFEREE_COMMENT_REPORT -> """
                    Hi %s
                    
                    Your report is ready.
                    
                    Please visit: %s/#/view/%s
                    """.formatted(reportee.firstName(),
                                  properties.getBaseUrl(),
                                  externalId);
            case TRAINER_REPORT -> "TODO";
            case GAME_DISCUSSION -> "TODO";
        };
    }

    private String getSubject(ReportType reportType) {
        return switch (reportType) {
            case REFEREE_VIDEO_REPORT -> "New Video Report";
            case REFEREE_COMMENT_REPORT -> "New Report";
            case TRAINER_REPORT -> "New Trainer Report";
            case GAME_DISCUSSION -> "New Game Discussion";
        };
    }

    public void sendNewDiscussionMail(UserDTO commenter, UserDTO recipient, Report report) {
        var simpleMessage = new SimpleMailMessage();
        try {
            log.info("discussion replies for referee-report with ID {}, send email to {}", report.getId(), recipient.email());

            simpleMessage.setSubject("New Video Report Discussion");
            simpleMessage.setFrom(environment.getRequiredProperty("spring.mail.username"));
            simpleMessage.setBcc(properties.getBccMail());

            if (properties.isOverrideRecipient()) {
                var to = properties.getOverrideRecipientMail();
                if (isBlank(to)) {
                    to = commenter.email();
                }
                simpleMessage.setTo(to);
                simpleMessage.setSubject(simpleMessage.getSubject() + " (%s)".formatted(recipient.email()));
                log.info("override recipient mail: {}", properties.getOverrideRecipientMail());
            } else {
                simpleMessage.setTo(recipient.email());
            }

            simpleMessage.setText("""
                                          Hi %s
                                          
                                          %s added new replies to a video report.
                                          
                                          Please visit: %s/#/discuss/%s
                                          """.formatted(recipient.firstName(), commenter.fullName(), properties.getBaseUrl(), report.getExternalId()));

            mailSender.send(simpleMessage);
        } catch (MailException e) {
            log.error("could not send email to: " + Arrays.toString(simpleMessage.getTo()), e);
        }
    }
}
