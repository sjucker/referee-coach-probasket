package ch.refereecoach.probasket.rest;

import ch.refereecoach.probasket.dto.report.TagDTO;
import ch.refereecoach.probasket.service.report.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tag")
@RequiredArgsConstructor
public class TagEndpoint {
    private final TagService tagService;

    @GetMapping
    public ResponseEntity<List<TagDTO>> tags() {
        log.info("GET /api/tag");

        return ResponseEntity.ok(tagService.findAll());
    }

}
