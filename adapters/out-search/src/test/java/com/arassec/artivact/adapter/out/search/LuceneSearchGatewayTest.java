package com.arassec.artivact.adapter.out.search;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.item.Item;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link LuceneSearchGateway}.
 */
class LuceneSearchGatewayTest {

    /**
     * Search gateway under test.
     */
    private LuceneSearchGateway searchGateway;

    /**
     * Repository for file access.
     */
    private final FileRepository fileRepository = mock(FileRepository.class);

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    @SneakyThrows
    void setUp() {
        Path indexDir = Path.of("target/dbdata-test");

        FileSystemUtils.deleteRecursively(indexDir.toFile());
        Files.createDirectories(indexDir);

        UseProjectDirsUseCase useProjectDirsUseCase = mock(UseProjectDirsUseCase.class);

        when(useProjectDirsUseCase.getSearchIndexDir()).thenReturn(indexDir);

        searchGateway = new LuceneSearchGateway(fileRepository, useProjectDirsUseCase);
    }

    /**
     * Tests the complete gateway...
     */
    @Test
    void testGateway() {
        // New search index:
        searchGateway.prepareIndexing(false);
        Item item = createTestItem();
        searchGateway.updateIndex(item, false);
        searchGateway.finalizeIndexing();

        // Search item by its ID:
        List<String> searchResult = searchGateway.search(item.getId(), 15);
        assertThat(searchResult).hasSize(1);
        assertThat(searchResult.getFirst()).isEqualTo(item.getId());

        // Search item by (yet unknown) title:
        searchResult = searchGateway.search("title2", 15);
        assertThat(searchResult).isEmpty();

        // Update search index:
        searchGateway.prepareIndexing(true);
        item.setTitle(new TranslatableString("title2"));
        searchGateway.updateIndex(item, true);
        searchGateway.finalizeIndexing();

        // Search again by the now indexed title:
        searchResult = searchGateway.search("title2", 15);
        assertThat(searchResult).hasSize(1);
        assertThat(searchResult.getFirst()).isEqualTo(item.getId());
    }

    private Item createTestItem() {
        UUID itemId = UUID.randomUUID();

        Item item = new Item();
        item.setId(itemId.toString());
        item.setTitle(new TranslatableString("title"));
        item.setDescription(new TranslatableString("description"));

        return item;
    }

}
