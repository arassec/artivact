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

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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

        pageEntity.setVersion(page.getVersion());
        pageEntity.setIndexPage(Boolean.TRUE.equals(page.getPageContent().getIndexPage()));
        pageEntity.setContentJson(toJson(page.getPageContent()));

        pageEntityRepository.save(pageEntity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page deleteById(String pageId) {
        Page page = findById(pageId);
        pageEntityRepository.deleteById(pageId);
        return page;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("java:S6204") // Widget collection needs to be modifiable...
    public Page findById(String pageId) {
        PageEntity pageEntity = pageEntityRepository.findById(pageId).orElseThrow();
        return convert(pageEntity);
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
