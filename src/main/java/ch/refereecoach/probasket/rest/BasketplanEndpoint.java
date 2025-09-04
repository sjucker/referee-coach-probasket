package ch.refereecoach.probasket.rest;

import ch.refereecoach.probasket.dto.basketplan.BasketplanGameDTO;
import ch.refereecoach.probasket.service.basketplan.BasketplanGameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/basketplan")
@RequiredArgsConstructor
public class BasketplanEndpoint {

    private final BasketplanGameService basketplanGameService;

    @GetMapping("/{gameNumber}")
    @Secured({"REFEREE_COACH", "TRAINER_COACH"})
    public ResponseEntity<BasketplanGameDTO> game(@PathVariable String gameNumber) {
        log.info("GET /api/basketplan/{}", gameNumber);

        return ResponseEntity.of(basketplanGameService.findGameByNumber(StringUtils.strip(gameNumber)));
    }
}

