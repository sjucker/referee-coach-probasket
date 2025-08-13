package ch.refereecoach.probasket.rest;

import ch.refereecoach.probasket.dto.report.CreateRefereeReportDTO;
import ch.refereecoach.probasket.dto.report.CreateRefereeReportResultDTO;
import ch.refereecoach.probasket.dto.report.RefereeReportDTO;
import ch.refereecoach.probasket.dto.report.ReportSearchResultDTO;
import ch.refereecoach.probasket.service.report.ReportSearchService;
import ch.refereecoach.probasket.service.report.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportEndpoint {

    private final ReportService reportService;
    private final ReportSearchService reportSearchService;

    @GetMapping
    public ResponseEntity<ReportSearchResultDTO> search(@AuthenticationPrincipal Jwt jwt,
                                                        @RequestParam @DateTimeFormat(iso = DATE) LocalDate from,
                                                        @RequestParam @DateTimeFormat(iso = DATE) LocalDate to,
                                                        @RequestParam String filter,
                                                        @RequestParam int page,
                                                        @RequestParam int pageSize) {
        log.info("GET /api/report?from={}&to={}&filter={}&page={}&pageSize={}", from, to, filter, page, pageSize);

        return ResponseEntity.ok(reportSearchService.search(from, to, filter, page, pageSize, jwt.getSubject()));
    }

    @GetMapping("/referee/{externalId}")
    public ResponseEntity<RefereeReportDTO> getReport(@AuthenticationPrincipal Jwt jwt,
                                                      @PathVariable String externalId) {
        log.info("GET /api/report/{} {}", externalId, jwt.getSubject());

        return ResponseEntity.of(reportSearchService.findRefereeReportByExternalId(externalId, jwt.getSubject()));
    }

    @PutMapping("/referee/{externalId}")
    public ResponseEntity<Void> updateReport(@AuthenticationPrincipal Jwt jwt,
                                             @PathVariable String externalId,
                                             @RequestBody @Valid RefereeReportDTO dto) {
        log.info("PUT /api/report/{} {}", externalId, jwt.getSubject());

        reportService.updateReport(externalId, dto, jwt.getSubject());
        
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/referee")
    @Secured({"REFEREE_COACH"})
    public ResponseEntity<CreateRefereeReportResultDTO> createReport(@AuthenticationPrincipal Jwt jwt,
                                                                     @RequestBody @Valid CreateRefereeReportDTO dto) {
        log.info("POST /api/report/referee {} {}", dto, jwt.getSubject());

        return ResponseEntity.ok(reportService.createRefereeReport(dto.gameNumber(), dto.videoUrl(), dto.reporteeId(), jwt.getSubject()));
    }
}
