package ch.refereecoach.probasket.service.report;

import ch.refereecoach.probasket.dto.report.TagDTO;
import ch.refereecoach.probasket.dto.search.TagOverviewDTO;
import ch.refereecoach.probasket.dto.search.TagSearchResultDTO;
import ch.refereecoach.probasket.jooq.tables.daos.TagDao;
import ch.refereecoach.probasket.util.YouTubeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;

import java.util.List;

import static ch.refereecoach.probasket.jooq.Tables.REPORT;
import static ch.refereecoach.probasket.jooq.Tables.TAG;
import static ch.refereecoach.probasket.jooq.tables.ReportVideoComment.REPORT_VIDEO_COMMENT;
import static ch.refereecoach.probasket.jooq.tables.ReportVideoCommentTag.REPORT_VIDEO_COMMENT_TAG;
import static java.util.stream.Collectors.toSet;
import static org.jooq.impl.DSL.listAgg;
import static org.jooq.impl.DSL.selectDistinct;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

    private final DSLContext jooqDsl;
    private final TagDao tagDao;

    public List<TagDTO> findAll() {
        return tagDao.findAll().stream()
                     .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                     .toList();
    }

    public TagSearchResultDTO findVideoCommentsForTags(List<TagDTO> tags, int page, int pageSize) {
        var stopWatch = new StopWatch();

        var tagsField = listAgg(TAG.NAME, ", ").withinGroupOrderBy(TAG.ID).as("tags");
        var items = jooqDsl.select(REPORT_VIDEO_COMMENT.ID,
                                   REPORT_VIDEO_COMMENT.TIMESTAMP_IN_SECONDS,
                                   REPORT_VIDEO_COMMENT.COMMENT,
                                   REPORT.GAME_NUMBER,
                                   REPORT.GAME_COMPETITION,
                                   REPORT.GAME_DATE,
                                   REPORT.GAME_VIDEO_URL,
                                   tagsField)
                           .from(REPORT_VIDEO_COMMENT)
                           .join(REPORT_VIDEO_COMMENT_TAG).on(REPORT_VIDEO_COMMENT.ID.eq(REPORT_VIDEO_COMMENT_TAG.REPORT_VIDEO_COMMENT_ID))
                           .join(TAG).on(REPORT_VIDEO_COMMENT_TAG.TAG_ID.eq(TAG.ID))
                           .join(REPORT).on(REPORT_VIDEO_COMMENT.REPORT_ID.eq(REPORT.ID))
                           .where(TAG.ID.in(tags.stream().map(TagDTO::id).collect(toSet())),
                                  REPORT.FINISHED_AT.isNotNull())
                           .groupBy(REPORT_VIDEO_COMMENT.ID,
                                    REPORT_VIDEO_COMMENT.TIMESTAMP_IN_SECONDS,
                                    REPORT_VIDEO_COMMENT.COMMENT,
                                    REPORT.GAME_NUMBER,
                                    REPORT.GAME_COMPETITION,
                                    REPORT.GAME_DATE,
                                    REPORT.GAME_VIDEO_URL)
                           .orderBy(REPORT_VIDEO_COMMENT.ID.desc())
                           .offset(page * pageSize)
                           .limit(pageSize)
                           .fetch(it -> new TagOverviewDTO(
                                   it.get(REPORT.GAME_NUMBER),
                                   it.get(REPORT.GAME_COMPETITION),
                                   it.get(REPORT.GAME_DATE),
                                   it.get(REPORT_VIDEO_COMMENT.TIMESTAMP_IN_SECONDS),
                                   it.get(REPORT_VIDEO_COMMENT.COMMENT),
                                   YouTubeUtil.parseYouTubeId(it.get(REPORT.GAME_VIDEO_URL)).orElse(null),
                                   it.get(tagsField)
                           ));

        var count = jooqDsl.fetchCount(selectDistinct(REPORT_VIDEO_COMMENT.ID)
                                               .from(REPORT_VIDEO_COMMENT)
                                               .join(REPORT_VIDEO_COMMENT_TAG).on(REPORT_VIDEO_COMMENT.ID.eq(REPORT_VIDEO_COMMENT_TAG.REPORT_VIDEO_COMMENT_ID))
                                               .join(REPORT).on(REPORT_VIDEO_COMMENT.REPORT_ID.eq(REPORT.ID))
                                               .where(REPORT_VIDEO_COMMENT_TAG.TAG_ID.in(tags.stream().map(TagDTO::id).collect(toSet())),
                                                      REPORT.FINISHED_AT.isNotNull()));

        log.info("tag search for tags={}, page={}, pageSize={} took {}",
                 tags, page, pageSize, stopWatch);

        return new TagSearchResultDTO(items, count);
    }

}
