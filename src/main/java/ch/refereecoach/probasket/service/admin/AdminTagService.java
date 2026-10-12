package ch.refereecoach.probasket.service.admin;

import ch.refereecoach.probasket.dto.admin.SaveTagDTO;
import ch.refereecoach.probasket.dto.admin.TagAdminDTO;
import ch.refereecoach.probasket.jooq.tables.daos.TagDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

import static ch.refereecoach.probasket.jooq.Tables.REPORT_VIDEO_COMMENT_TAG;
import static ch.refereecoach.probasket.jooq.Tables.TAG;
import static org.jooq.impl.DSL.count;
import static org.jooq.impl.DSL.lower;
import static org.jooq.impl.DSL.trim;
import static org.jooq.impl.DSL.val;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminTagService {

    private final DSLContext jooqDsl;
    private final TagDao tagDao;

    public List<TagAdminDTO> findAll() {
        var usageCount = count(REPORT_VIDEO_COMMENT_TAG.TAG_ID).as("usage_count");
        return jooqDsl.select(TAG.ID, TAG.NAME, usageCount)
                      .from(TAG)
                      .leftJoin(REPORT_VIDEO_COMMENT_TAG).on(REPORT_VIDEO_COMMENT_TAG.TAG_ID.eq(TAG.ID))
                      .groupBy(TAG.ID, TAG.NAME)
                      .orderBy(TAG.NAME.asc())
                      .fetch(it -> new TagAdminDTO(it.get(TAG.ID), it.get(TAG.NAME), it.get(usageCount)));
    }

    /**
     * @throws IllegalStateException if a tag with the same name already exists
     */
    public TagAdminDTO createTag(SaveTagDTO dto) {
        var name = dto.name().trim();
        assertNameAvailable(name, null);

        var id = jooqDsl.insertInto(TAG)
                        .set(TAG.NAME, name)
                        .returningResult(TAG.ID)
                        .fetchSingle(TAG.ID);

        log.info("created tag {} ({})", name, id);
        return new TagAdminDTO(id, name, 0);
    }

    /**
     * @throws IllegalArgumentException if the tag does not exist
     * @throws IllegalStateException    if another tag with the same name already exists
     */
    public TagAdminDTO renameTag(Long id, SaveTagDTO dto) {
        var tag = tagDao.fetchOptionalById(id).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + id));
        var name = dto.name().trim();
        assertNameAvailable(name, id);

        log.info("renaming tag {} from {} to {}", id, tag.getName(), name);
        tag.setName(name);
        tagDao.update(tag);

        return new TagAdminDTO(tag.getId(), tag.getName(), usageCount(id));
    }

    /**
     * @throws IllegalArgumentException if the tag does not exist
     * @throws IllegalStateException    if the tag is still referenced by a video comment
     */
    public void deleteTag(Long id) {
        var tag = tagDao.fetchOptionalById(id).orElseThrow(() -> new IllegalArgumentException("Tag not found: " + id));

        if (usageCount(id) > 0) {
            throw new IllegalStateException("Tag is still referenced: " + id);
        }

        try {
            tagDao.delete(tag);
        } catch (DataIntegrityViolationException e) {
            // the tag got referenced after the usage check
            throw new IllegalStateException("Tag is still referenced: " + id, e);
        }
        log.info("deleted tag {} ({})", tag.getName(), id);
    }

    private int usageCount(Long id) {
        return jooqDsl.fetchCount(REPORT_VIDEO_COMMENT_TAG, REPORT_VIDEO_COMMENT_TAG.TAG_ID.eq(id));
    }

    private void assertNameAvailable(String name, Long excludeId) {
        var condition = lower(trim(TAG.NAME)).eq(lower(val(name)));
        if (excludeId != null) {
            condition = condition.and(TAG.ID.ne(excludeId));
        }
        if (jooqDsl.fetchExists(TAG, condition)) {
            throw new IllegalStateException("Tag name already taken: " + name);
        }
    }
}
