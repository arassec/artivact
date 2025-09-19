package com.arassec.artivact.application.service.project;

import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectDirServiceTest {

    private ProjectDirService service;

    private final String projectRootStr = "testRoot";

    @BeforeEach
    void setUp() {
        service = new ProjectDirService(projectRootStr);
    }

    @Test
    void testProjectRoot() {
        assertThat(service.getProjectRoot()).isEqualTo(Path.of(projectRootStr));
    }

    @Test
    void testGetTempDir() {
        assertThat(service.getTempDir())
                .isEqualTo(Path.of(projectRootStr).resolve(DirectoryDefinitions.TEMP_DIR));
    }

    @Test
    void testGetExportsDir() {
        assertThat(service.getExportsDir())
                .isEqualTo(Path.of(projectRootStr).resolve(DirectoryDefinitions.EXPORT_DIR));
    }

    @Test
    void testGetItemsDir() {
        assertThat(service.getItemsDir())
                .isEqualTo(Path.of(projectRootStr).resolve(DirectoryDefinitions.ITEMS_DIR));
    }

    @Test
    void testGetWidgetsDir() {
        assertThat(service.getWidgetsDir())
                .isEqualTo(Path.of(projectRootStr).resolve(DirectoryDefinitions.WIDGETS_DIR));
    }

    @Test
    void testGetSearchIndexDir() {
        assertThat(service.getSearchIndexDir())
                .isEqualTo(Path.of(projectRootStr).resolve(DirectoryDefinitions.SEARCH_INDEX_DIR));
    }

    @Test
    void testGetImagesDir() {
        String itemId = "abcdef123";
        Path expected = Path.of(projectRootStr)
                .resolve(DirectoryDefinitions.ITEMS_DIR)
                .resolve("abc")
                .resolve("def")
                .resolve(itemId)
                .resolve(DirectoryDefinitions.IMAGES_DIR);

        assertThat(service.getImagesDir(itemId)).isEqualTo(expected);
    }

    @Test
    void testGetModelsDir() {
        String itemId = "xyz123456";
        Path expected = Path.of(projectRootStr)
                .resolve(DirectoryDefinitions.ITEMS_DIR)
                .resolve("xyz")
                .resolve("123")
                .resolve(itemId)
                .resolve(DirectoryDefinitions.MODELS_DIR);

        assertThat(service.getModelsDir(itemId)).isEqualTo(expected);
    }

}
