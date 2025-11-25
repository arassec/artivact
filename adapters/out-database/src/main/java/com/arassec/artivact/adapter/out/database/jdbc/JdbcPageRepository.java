package com.arassec.artivact.adapter.out.database.jdbc;

import com.arassec.artivact.adapter.out.database.jdbc.springdata.entity.PageEntity;
import com.arassec.artivact.adapter.out.database.jdbc.springdata.repository.PageEntityRepository;
import com.arassec.artivact.application.port.out.repository.PageRepository;
import com.arassec.artivact.domain.model.page.Page;
import com.arassec.artivact.domain.model.page.PageContent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * {@link PageRepository} implementation that uses JDBC.
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class JdbcPageRepository extends BaseJdbcRepository implements PageRepository {

    /**
     * Spring-Data's repository for {@link PageEntity}s.
     */
    private final PageEntityRepository pageEntityRepository;

    /**
     * The systems ObjectMapper.
     */
    @Getter
    private final JsonMapper jsonMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(Page page) {
        Optional<PageEntity> pageEntityOptional = pageEntityRepository.findById(page.getId());

        PageEntity pageEntity = new PageEntity();
        if (pageEntityOptional.isPresent()) {
            pageEntity = pageEntityOptional.get();
        } else {
            pageEntity.setId(page.getId());
        }

        pageEntity.setContentJson(toJson(page.getPageContent()));
        pageEntity.setWipContentJson(toJson(page.getWipPageContent()));
        pageEntity.setAlias(page.getAlias());

        pageEntityRepository.save(pageEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Page> deleteById(String pageId) {
        Optional<Page> pageOptional = findById(pageId);
        if (pageOptional.isPresent()) {
            pageEntityRepository.deleteById(pageId);
        }
        return pageOptional;
    }

    @Override
    public List<Page> findAll() {
        return StreamSupport.stream(pageEntityRepository.findAll().spliterator(), false)
                .map(this::convert)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Page> findById(String pageId) {
        Optional<PageEntity> pageEntityOptional = pageEntityRepository.findById(pageId);
        return pageEntityOptional.map(this::convert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Page> findByIdOrAlias(String pageIdOrAlias) {
        Optional<PageEntity> pageEntityOptional = pageEntityRepository.findFirstByAlias(pageIdOrAlias);
        if (pageEntityOptional.isEmpty()) {
            pageEntityOptional = pageEntityRepository.findById(pageIdOrAlias);
        }
        return pageEntityOptional.map(this::convert);
    }

    /**
     * Converts a page entity into a page.
     */
    @SuppressWarnings("java:S6204") // Widget list needs to be modifiable!
    private Page convert(PageEntity pageEntity) {
        Page page = new Page();
        page.setId(pageEntity.getId());
        page.setVersion(pageEntity.getVersion());
        page.setPageContent(fromJson(pageEntity.getContentJson(), PageContent.class));
        page.setWipPageContent(fromJson(pageEntity.getWipContentJson(), PageContent.class));
        page.setAlias(pageEntity.getAlias());

        // Cleanup Widgets...
        page.getPageContent().setWidgets(page.getPageContent().getWidgets().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        );

        return page;
    }

}
