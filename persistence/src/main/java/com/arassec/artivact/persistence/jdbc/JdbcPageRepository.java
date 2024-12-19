package com.arassec.artivact.persistence.jdbc;

import com.arassec.artivact.core.model.page.Page;
import com.arassec.artivact.core.model.page.PageContent;
import com.arassec.artivact.core.repository.PageRepository;
import com.arassec.artivact.persistence.jdbc.springdata.entity.PageEntity;
import com.arassec.artivact.persistence.jdbc.springdata.repository.PageEntityRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final ObjectMapper objectMapper;

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

        pageEntity.setIndexPage(Boolean.TRUE.equals(page.getPageContent().getIndexPage()));
        pageEntity.setContentJson(toJson(page.getPageContent()));

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
    @SuppressWarnings("java:S6204") // Widget collection needs to be modifiable...
    public Optional<Page> findById(String pageId) {
        Optional<PageEntity> pageEntityOptional = pageEntityRepository.findById(pageId);
        return pageEntityOptional.map(this::convert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page findIndexPage() {
        Optional<PageEntity> pageEntityOptional = pageEntityRepository.findFirstByIndexPage(true);
        Page page = new Page();
        if (pageEntityOptional.isPresent()) {
            return convert(pageEntityOptional.get());
        } else {
            page.setPageContent(new PageContent());
        }
        return page;
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

        // Cleanup Widgets...
        page.getPageContent().setWidgets(page.getPageContent().getWidgets().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList())
        );

        return page;
    }

}
