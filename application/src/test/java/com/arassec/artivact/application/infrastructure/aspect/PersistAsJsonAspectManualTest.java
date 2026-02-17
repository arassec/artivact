package com.arassec.artivact.application.infrastructure.aspect;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.page.PageContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Manual integration test for JSON persistence aspect.
 * Tests the aspect without Spring context by calling methods directly.
 */
class PersistAsJsonAspectManualTest {

    @TempDir
    Path tempDir;

    private PersistAsJsonAspect aspect;
    private JsonMapper jsonMapper;
    private FileRepository fileRepository;
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @BeforeEach
    void setUp() {
        jsonMapper = new JsonMapper();
        
        // Create mock implementations
        fileRepository = new FileRepository() {
            @Override
            public void createDirIfRequired(Path directory) {
                try {
                    Files.createDirectories(directory);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public List<Path> list(Path dir) {
                try {
                    if (!Files.exists(dir)) {
                        return List.of();
                    }
                    return Files.list(dir).toList();
                } catch (Exception e) {
                    return List.of();
                }
            }

            // Other methods not needed for this test
            @Override
            public void updateProjectDirectory(Path projectRoot, Path projectSetupDir, Path projectSetupDirFallback) {}
            @Override
            public void emptyDir(Path directory) {}
            @Override
            public void delete(Path path) {}
            @Override
            public void openDirInOs(Path directory) {}
            @Override
            public void copy(java.io.InputStream source, Path target, java.nio.file.CopyOption... copyOptions) {}
            @Override
            public long copy(Path source, java.io.OutputStream target) { return 0; }
            @Override
            public void copy(Path source, Path target, java.nio.file.CopyOption... copyOptions) {}
            @Override
            public Path getDirFromId(Path root, String id) { return null; }
            @Override
            public Path getSubdirFilePath(Path root, String id, String dirOrFilename) { return null; }
            @Override
            public String getSubDir(String id, int index) { return null; }
            @Override
            public List<String> listNamesWithoutScaledImages(Path path) { return null; }
            @Override
            public boolean exists(Path path) { return false; }
            @Override
            public boolean isDir(Path path) { return false; }
            @Override
            public void deleteDirAndEmptyParents(Path directory) {}
            @Override
            public void copyProjectResource(Path source, Path target) {}
            @Override
            public byte[] readBytes(Path path) { return null; }
            @Override
            public String readString(Path path) { return null; }
            @Override
            public void write(Path path, byte[] content) {}
            @Override
            public org.springframework.core.io.FileSystemResource loadImage(Path root, String id, String filename, com.arassec.artivact.domain.model.item.ImageSize targetSize, String subDir) { return null; }
            @Override
            public void zipDir(Path sourceDir, Path targetZip) {}
            @Override
            public void unzipFile(Path zipFile, Path targetDir) {}
        };

        useProjectDirsUseCase = new UseProjectDirsUseCase() {
            @Override
            public Path getProjectRoot() { return tempDir; }
            @Override
            public Path getTempDir() { return null; }
            @Override
            public Path getExportsDir() { return null; }
            @Override
            public Path getItemsDir() { return null; }
            @Override
            public Path getWidgetsDir() { return null; }
            @Override
            public Path getSearchIndexDir() { return null; }
            @Override
            public Path getImagesDir(String itemId) { return null; }
            @Override
            public Path getModelsDir(String itemId) { return null; }
        };

        aspect = new PersistAsJsonAspect(jsonMapper, fileRepository, useProjectDirsUseCase);
        ReflectionTestUtils.setField(aspect, "jsonPersistenceEnabled", true);
    }

    @Test
    void testPersistItemDirectly() throws Exception {
        // Given
        Item item = new Item();
        item.setId("item12345");
        item.setVersion(1);

        // When - directly call persistEntity via reflection
        java.lang.reflect.Method method = PersistAsJsonAspect.class.getDeclaredMethod("persistEntity", String.class, com.arassec.artivact.domain.model.IdentifiedObject.class);
        method.setAccessible(true);
        method.invoke(aspect, "items", item);

        // Then
        Path jsonFile = tempDir.resolve("items/ite/m12/item12345.json");
        assertThat(jsonFile).exists();
        String content = Files.readString(jsonFile);
        assertThat(content).contains("\"id\" : \"item12345\"");
        assertThat(content).contains("\"version\" : 1");
    }

    @Test
    void testPersistMenuDirectly() throws Exception {
        // Given
        Menu menu = new Menu();
        menu.setId("menu67890");

        // When
        java.lang.reflect.Method method = PersistAsJsonAspect.class.getDeclaredMethod("persistEntity", String.class, com.arassec.artivact.domain.model.IdentifiedObject.class);
        method.setAccessible(true);
        method.invoke(aspect, "menus", menu);

        // Then
        Path jsonFile = tempDir.resolve("menus/men/u67/menu67890.json");
        assertThat(jsonFile).exists();
        assertThat(Files.readString(jsonFile)).contains("\"id\" : \"menu67890\"");
    }

    @Test
    void testPersistPageContentDirectly() throws Exception {
        // Given
        PageContent pageContent = new PageContent();
        pageContent.setId("page12345");

        // When
        java.lang.reflect.Method method = PersistAsJsonAspect.class.getDeclaredMethod("persistEntity", String.class, com.arassec.artivact.domain.model.IdentifiedObject.class);
        method.setAccessible(true);
        method.invoke(aspect, "pages", pageContent);

        // Then
        Path jsonFile = tempDir.resolve("pages/pag/e12/page12345.json");
        assertThat(jsonFile).exists();
        assertThat(Files.readString(jsonFile)).contains("\"id\" : \"page12345\"");
    }

    @Test
    void testDeleteEntityJsonFileDirectly() throws Exception {
        // Given - create a JSON file first
        Item item = new Item();
        item.setId("delete123");

        java.lang.reflect.Method persistMethod = PersistAsJsonAspect.class.getDeclaredMethod("persistEntity", String.class, com.arassec.artivact.domain.model.IdentifiedObject.class);
        persistMethod.setAccessible(true);
        persistMethod.invoke(aspect, "items", item);

        Path jsonFile = tempDir.resolve("items/del/ete/delete123.json");
        assertThat(jsonFile).exists();

        // When - delete the file
        java.lang.reflect.Method deleteMethod = PersistAsJsonAspect.class.getDeclaredMethod("deleteEntityJsonFile", String.class, String.class);
        deleteMethod.setAccessible(true);
        deleteMethod.invoke(aspect, "items", "delete123");

        // Then
        assertThat(jsonFile).doesNotExist();
        // Parent directories should be cleaned up too
        assertThat(jsonFile.getParent()).doesNotExist();
        assertThat(jsonFile.getParent().getParent()).doesNotExist();
    }

    @Test
    void testDirectoryStructure() throws Exception {
        // Given
        Item item1 = new Item();
        item1.setId("abc123def");
        
        Item item2 = new Item();
        item2.setId("abc456ghi");

        // When
        java.lang.reflect.Method method = PersistAsJsonAspect.class.getDeclaredMethod("persistEntity", String.class, com.arassec.artivact.domain.model.IdentifiedObject.class);
        method.setAccessible(true);
        method.invoke(aspect, "items", item1);
        method.invoke(aspect, "items", item2);

        // Then - verify correct directory structure
        assertThat(tempDir.resolve("items/abc/123/abc123def.json")).exists();
        assertThat(tempDir.resolve("items/abc/456/abc456ghi.json")).exists();
    }

    @Test
    void testSkipsShortIds() throws Exception {
        // Given
        Item item = new Item();
        item.setId("short");  // Only 5 characters

        // When
        java.lang.reflect.Method method = PersistAsJsonAspect.class.getDeclaredMethod("persistEntity", String.class, com.arassec.artivact.domain.model.IdentifiedObject.class);
        method.setAccessible(true);
        method.invoke(aspect, "items", item);

        // Then
        assertThat(tempDir.resolve("items")).doesNotExist();
    }
}
