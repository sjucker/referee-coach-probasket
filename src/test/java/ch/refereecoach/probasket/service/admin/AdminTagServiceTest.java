package ch.refereecoach.probasket.service.admin;

import ch.refereecoach.probasket.dto.admin.SaveTagDTO;
import ch.refereecoach.probasket.jooq.tables.daos.TagDao;
import ch.refereecoach.probasket.jooq.tables.pojos.Tag;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminTagServiceTest {

    private final DSLContext jooqDsl = mock(DSLContext.class);
    private final TagDao tagDao = mock(TagDao.class);
    private final AdminTagService service = new AdminTagService(jooqDsl, tagDao);

    @Test
    void deletesUnreferencedTag() {
        var tag = tag(7L, "LEAD");
        when(tagDao.fetchOptionalById(7L)).thenReturn(Optional.of(tag));
        when(jooqDsl.fetchCount(any(Table.class), any(Condition.class))).thenReturn(0);

        service.deleteTag(7L);

        verify(tagDao).delete(tag);
    }

    @Test
    void rejectsDeletionOfReferencedTag() {
        when(tagDao.fetchOptionalById(7L)).thenReturn(Optional.of(tag(7L, "LEAD")));
        when(jooqDsl.fetchCount(any(Table.class), any(Condition.class))).thenReturn(3);

        assertThatThrownBy(() -> service.deleteTag(7L)).isInstanceOf(IllegalStateException.class);
        verify(tagDao, never()).delete(any(Tag.class));
    }

    @Test
    void rejectsDeletionOfUnknownTag() {
        when(tagDao.fetchOptionalById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteTag(7L)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void renamesTagAndTrimsName() {
        var tag = tag(7L, "LEAD");
        when(tagDao.fetchOptionalById(7L)).thenReturn(Optional.of(tag));
        when(jooqDsl.fetchExists(any(Table.class), any(Condition.class))).thenReturn(false);
        when(jooqDsl.fetchCount(any(Table.class), any(Condition.class))).thenReturn(5);

        var result = service.renameTag(7L, new SaveTagDTO("  LEAD - Lead official "));

        assertThat(result.name()).isEqualTo("LEAD - Lead official");
        assertThat(result.usageCount()).isEqualTo(5);
        verify(tagDao).update(tag);
    }

    @Test
    void rejectsRenameToExistingName() {
        when(tagDao.fetchOptionalById(7L)).thenReturn(Optional.of(tag(7L, "LEAD")));
        when(jooqDsl.fetchExists(any(Table.class), any(Condition.class))).thenReturn(true);

        assertThatThrownBy(() -> service.renameTag(7L, new SaveTagDTO("TRAIL"))).isInstanceOf(IllegalStateException.class);
        verify(tagDao, never()).update(any(Tag.class));
    }

    private static Tag tag(Long id, String name) {
        var tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tag;
    }
}
