package ch.refereecoach.probasket.service.report;

import ch.refereecoach.probasket.dto.report.TagDTO;
import ch.refereecoach.probasket.jooq.tables.daos.TagDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagDao tagDao;

    public List<TagDTO> findAll() {
        return tagDao.findAll().stream()
                     .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                     .toList();
    }

}
