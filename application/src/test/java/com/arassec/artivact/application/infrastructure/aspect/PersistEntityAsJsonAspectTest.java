package com.arassec.artivact.application.infrastructure.aspect;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.page.PageContent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersistEntityAsJsonAspectTest {

    @Mock
    private JsonMapper jsonMapper;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @InjectMocks
    private PersistEntityAsJsonAspect aspect;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private void enableAspect() throws Exception {
        Field enabledField = PersistEntityAsJsonAspect.class.getDeclaredField("enabled");
        enabledField.setAccessible(true);
        enabledField.set(aspect, true);
    }

    @Test
    void testDoesNothingWhenDisabled() {
        aspect.persistEntityAsJson(joinPoint);
        verifyNoInteractions(jsonMapper, fileRepository, useProjectDirsUseCase);
    }

    @Test
    void testPersistsMenuAsJson() throws Exception {
        enableAspect();

        Menu menu = new Menu();
        menu.setId("menu12345678");

        Method method = TestAnnotatedMethods.class.getMethod("saveMenu", Menu.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{menu});
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("testRoot"));
        when(jsonMapper.writeValueAsBytes(menu)).thenReturn("{}".getBytes());
        when(fileRepository.getSubdirFilePath(eq(Path.of("testRoot/menus")), eq("menu12345678"), eq("menu.json")))
                .thenReturn(Path.of("testRoot", "menus", "men", "u12", "menu12345678", "menu.json"));
        aspect.persistEntityAsJson(joinPoint);

        Path expectedDir = Path.of("testRoot", "menus", "men", "u12", "menu12345678");
        verify(fileRepository).createDirIfRequired(expectedDir);
        verify(fileRepository).write(expectedDir.resolve("menu.json"), "{}".getBytes());
    }

    @Test
    void testSkipsEntityWithShortId() throws Exception {
        enableAspect();

        Item item = new Item();
        item.setId("abc");

        Method method = TestAnnotatedMethods.class.getMethod("saveItem", Item.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{item});

        aspect.persistEntityAsJson(joinPoint);

        verify(fileRepository, never()).write(any(), any(byte[].class));
    }

    @Test
    void testSkipsEntityWithNullId() throws Exception {
        enableAspect();

        Item item = new Item();
        item.setId(null);

        Method method = TestAnnotatedMethods.class.getMethod("saveItem", Item.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{item});

        aspect.persistEntityAsJson(joinPoint);

        verify(fileRepository, never()).write(any(), any(byte[].class));
    }

    @Test
    void testDoesNotFailOnException() throws Exception {
        enableAspect();

        when(joinPoint.getSignature()).thenThrow(new RuntimeException("test error"));

        // Should not throw
        aspect.persistEntityAsJson(joinPoint);
    }

    @Test
    void testDeleteSkipsShortId() throws Exception {
        enableAspect();

        String shortId = "ab";

        Method method = TestAnnotatedMethods.class.getMethod("deleteItem", String.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{shortId});

        aspect.persistEntityAsJson(joinPoint);

        verify(fileRepository, never()).delete(any());
    }

    @Test
    void testDeleteDoesNotRemoveNonEmptyParentDirs() throws Exception {
        enableAspect();

        String itemId = "abc123def456";

        Method method = TestAnnotatedMethods.class.getMethod("deleteItem", String.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{itemId});
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("testRoot"));

        Path expectedFile = Path.of("testRoot", "items", "abc", "123", "abc123def456", "item.json");
        Path secondSubDir = Path.of("testRoot", "items", "abc", "123");

        when(fileRepository.getSubdirFilePath(eq(Path.of("testRoot/items")), eq("abc123def456"), eq("item.json")))
                .thenReturn(expectedFile);

        aspect.persistEntityAsJson(joinPoint);

        verify(fileRepository).deleteAndPruneEmptyParents(expectedFile);
        verify(fileRepository, never()).delete(secondSubDir);
    }

    @Test
    void testPersistsItemAsJson() throws Exception {
        enableAspect();

        Item item = new Item();
        item.setId("item12345678");

        Method method = TestAnnotatedMethods.class.getMethod("saveItem", Item.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{item});
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("testRoot"));
        when(jsonMapper.writeValueAsBytes(item)).thenReturn("{\"id\":\"item12345678\"}".getBytes());
        when(fileRepository.getSubdirFilePath(eq(Path.of("testRoot/items")), eq("item12345678"), eq("item.json")))
                .thenReturn(Path.of("testRoot", "items", "ite", "m12", "item12345678", "item.json"));

        aspect.persistEntityAsJson(joinPoint);

        Path expectedDir = Path.of("testRoot", "items", "ite", "m12", "item12345678");
        verify(fileRepository).createDirIfRequired(expectedDir);
        verify(fileRepository).write(expectedDir.resolve("item.json"), "{\"id\":\"item12345678\"}".getBytes());
    }

    @Test
    void testPersistsPageContentFromSecondParameter() throws Exception {
        enableAspect();

        PageContent pageContent = new PageContent();
        pageContent.setId("page12345678");

        Method method = TestAnnotatedMethods.class.getMethod("savePageContent", String.class, PageContent.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"some-alias", pageContent});
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("testRoot"));
        when(jsonMapper.writeValueAsBytes(pageContent)).thenReturn("{}".getBytes());
        when(fileRepository.getSubdirFilePath(eq(Path.of("testRoot/pages")), eq("page12345678"), eq("properties.json")))
                .thenReturn(Path.of("testRoot", "pages", "pag", "e12", "page12345678", "properties.json"));

        aspect.persistEntityAsJson(joinPoint);

        Path expectedDir = Path.of("testRoot", "pages", "pag", "e12", "page12345678");
        verify(fileRepository).createDirIfRequired(expectedDir);
        verify(fileRepository).write(expectedDir.resolve("properties.json"), "{}".getBytes());
    }

    @Test
    void testDeleteWithValidId() throws Exception {
        enableAspect();

        String itemId = "item12345678";

        Method method = TestAnnotatedMethods.class.getMethod("deleteItem", String.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{itemId});
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("testRoot"));

        Path expectedFile = Path.of("testRoot", "items", "ite", "m12", "item12345678", "item.json");
        when(fileRepository.getSubdirFilePath(eq(Path.of("testRoot/items")), eq("item12345678"), eq("item.json")))
                .thenReturn(expectedFile);

        aspect.persistEntityAsJson(joinPoint);

        verify(fileRepository).deleteAndPruneEmptyParents(expectedFile);
        verify(fileRepository, never()).write(any(), any(byte[].class));
    }

    @Test
    void testDeleteSkipsNullId() throws Exception {
        enableAspect();

        Method method = TestAnnotatedMethods.class.getMethod("deleteItem", String.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{null});

        aspect.persistEntityAsJson(joinPoint);

        verify(fileRepository, never()).delete(any());
        verify(fileRepository, never()).deleteAndPruneEmptyParents(any());
    }

    @Test
    void testSkipsNullEntityParameter() throws Exception {
        enableAspect();

        Method method = TestAnnotatedMethods.class.getMethod("saveItem", Item.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{null});

        aspect.persistEntityAsJson(joinPoint);

        verify(fileRepository, never()).write(any(), any(byte[].class));
        verify(fileRepository, never()).createDirIfRequired(any());
    }

    // Helper class with annotated methods for testing
    @SuppressWarnings("unused")
    static class TestAnnotatedMethods {
        @PersistEntityAsJson(entityDir = "items", entityType = Item.class, filename = "item.json")
        public void saveItem(Item item) {
        }

        @PersistEntityAsJson(entityDir = "items", entityType = Item.class, delete = true, filename = "item.json")
        public void deleteItem(String itemId) {
        }

        @PersistEntityAsJson(entityDir = "menus", entityType = Menu.class, filename = "menu.json")
        public void saveMenu(Menu menu) {
        }

        @PersistEntityAsJson(entityDir = "menus", entityType = Menu.class, filename = "menu.json")
        public void saveMenus(List<Menu> menus) {
        }

        @PersistEntityAsJson(entityDir = "pages", entityType = PageContent.class, filename = "properties.json")
        public void savePageContent(String pageIdOrAlias, PageContent pageContent) {
        }
    }

}
