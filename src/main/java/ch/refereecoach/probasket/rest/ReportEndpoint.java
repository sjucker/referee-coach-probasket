package ch.refereecoach.probasket.rest;

import ch.refereecoach.probasket.dto.report.CreateRefereeReportDTO;
import ch.refereecoach.probasket.dto.report.CreateRefereeReportResultDTO;
import ch.refereecoach.probasket.service.report.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportEndpoint {

    private final ReportService reportService;

    @PostMapping(value = "/referee")
    @Secured({"REFEREE_COACH"})
    public ResponseEntity<CreateRefereeReportResultDTO> createReport(@AuthenticationPrincipal Jwt jwt,
                                                                     @RequestBody @Valid CreateRefereeReportDTO dto) {
        log.info("POST /api/report/referee {} {}", dto, jwt.getSubject());

        return ResponseEntity.ok(reportService.createRefereeReport(dto.gameNumber(), dto.videoUrl(), dto.reporteeId(), jwt.getSubject()));
    }
}
