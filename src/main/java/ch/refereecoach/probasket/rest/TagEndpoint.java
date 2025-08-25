package ch.refereecoach.probasket.rest;

import ch.refereecoach.probasket.dto.report.TagDTO;
import ch.refereecoach.probasket.dto.search.TagSearchResultDTO;
import ch.refereecoach.probasket.service.report.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/tag")
@RequiredArgsConstructor
public class TagEndpoint {
    private final TagService tagService;

    @GetMapping
    @Secured({"REFEREE_COACH", "REFEREE"})
    public ResponseEntity<List<TagDTO>> tags() {
        log.info("GET /api/tag");

        return ResponseEntity.ok(tagService.findAll());
    }

    @PostMapping("/search")
    @Secured({"REFEREE_COACH", "REFEREE"})
    public ResponseEntity<TagSearchResultDTO> findVideoCommentsForTags(@RequestBody List<TagDTO> tags,
                                                                       @RequestParam int page,
                                                                       @RequestParam int pageSize) {
        log.info("POST /api/tag/search {}", tags);

        return ResponseEntity.ok(tagService.findVideoCommentsForTags(tags, page, pageSize));
    }

}
