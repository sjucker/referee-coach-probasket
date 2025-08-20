package ch.refereecoach.probasket.rest;

import ch.refereecoach.probasket.dto.basketplan.BasketplanGameDTO;
import ch.refereecoach.probasket.service.basketplan.BasketplanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static ch.refereecoach.probasket.common.OfficiatingMode.OFFICIATING_2PO;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/basketplan")
@RequiredArgsConstructor
public class BasketplanEndpoint {

    private final BasketplanService basketplanService;

    @GetMapping("/{gameNumber}")
    @Secured({"REFEREE_COACH", "TRAINER_COACH"})
    public ResponseEntity<BasketplanGameDTO> game(@PathVariable String gameNumber) {
        log.info("GET /api/basketplan/{}", gameNumber);

        // TODO remove mock data
        return ResponseEntity.ok(new BasketplanGameDTO(
                "24-06531", "H1LRA Saison 24/25", LocalDate.parse("2024-10-05"),
                "67:61", "BC Winterthur 2 H1", 1213, "Frauenfeld Herren 1 H1", 506, OFFICIATING_2PO,
                4L, "Ljubanic Jovan", 3L, "Castro Nicolas", null, null, null));

//        return ResponseEntity.of(basketplanService.findGameByNumber(gameNumber));
    }
}

