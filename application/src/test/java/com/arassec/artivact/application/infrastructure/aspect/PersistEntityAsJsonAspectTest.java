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
    void testDoesNothingWhenDisabled() throws Exception {
        aspect.persistEntityAsJson(joinPoint);
        verifyNoInteractions(jsonMapper, fileRepository, useProjectDirsUseCase);
    }

    @Test
    void testPersistsItemAsJson() throws Exception {
        enableAspect();

        Item item = new Item();
        item.setId("abc123def456");

        Method method = TestAnnotatedMethods.class.getMethod("saveItem", Item.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{item});
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("testRoot"));
        when(fileRepository.getSubDir("abc123def456", 0)).thenReturn("abc");
        when(fileRepository.getSubDir("abc123def456", 1)).thenReturn("123");
        when(jsonMapper.writeValueAsBytes(item)).thenReturn("{}".getBytes());

        aspect.persistEntityAsJson(joinPoint);

        Path expectedDir = Path.of("testRoot", "items", "abc", "123");
        Path expectedFile = expectedDir.resolve("abc123def456.json");
        verify(fileRepository).createDirIfRequired(expectedDir);
        verify(fileRepository).write(expectedFile, "{}".getBytes());
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
        when(fileRepository.getSubDir("menu12345678", 0)).thenReturn("men");
        when(fileRepository.getSubDir("menu12345678", 1)).thenReturn("u12");
        when(jsonMapper.writeValueAsBytes(menu)).thenReturn("{}".getBytes());

        aspect.persistEntityAsJson(joinPoint);

        Path expectedDir = Path.of("testRoot", "menus", "men", "u12");
        verify(fileRepository).createDirIfRequired(expectedDir);
        verify(fileRepository).write(expectedDir.resolve("menu12345678.json"), "{}".getBytes());
    }

    @Test
    void testPersistsPageContentAsJson() throws Exception {
        enableAspect();

        PageContent pageContent = new PageContent();
        pageContent.setId("page12345678");

        Method method = TestAnnotatedMethods.class.getMethod("savePageContent", String.class, PageContent.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"someAlias", pageContent});
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("testRoot"));
        when(fileRepository.getSubDir("page12345678", 0)).thenReturn("pag");
        when(fileRepository.getSubDir("page12345678", 1)).thenReturn("e12");
        when(jsonMapper.writeValueAsBytes(pageContent)).thenReturn("{}".getBytes());

        aspect.persistEntityAsJson(joinPoint);

        Path expectedDir = Path.of("testRoot", "pages", "pag", "e12");
        verify(fileRepository).createDirIfRequired(expectedDir);
        verify(fileRepository).write(expectedDir.resolve("page12345678.json"), "{}".getBytes());
    }

    @Test
    void testDeletesItemJsonFile() throws Exception {
        enableAspect();

        String itemId = "abc123def456";

        Method method = TestAnnotatedMethods.class.getMethod("deleteItem", String.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{itemId});
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("testRoot"));
        when(fileRepository.getSubDir(itemId, 0)).thenReturn("abc");
        when(fileRepository.getSubDir(itemId, 1)).thenReturn("123");

        Path expectedFile = Path.of("testRoot", "items", "abc", "123", "abc123def456.json");
        when(fileRepository.exists(expectedFile)).thenReturn(true);

        Path secondSubDir = Path.of("testRoot", "items", "abc", "123");
        when(fileRepository.exists(secondSubDir)).thenReturn(true);
        when(fileRepository.isDir(secondSubDir)).thenReturn(true);
        when(fileRepository.list(secondSubDir)).thenReturn(List.of());

        Path firstSubDir = Path.of("testRoot", "items", "abc");
        when(fileRepository.exists(firstSubDir)).thenReturn(true);
        when(fileRepository.isDir(firstSubDir)).thenReturn(true);
        when(fileRepository.list(firstSubDir)).thenReturn(List.of());

        aspect.persistEntityAsJson(joinPoint);

        verify(fileRepository).delete(expectedFile);
        verify(fileRepository).delete(secondSubDir);
        verify(fileRepository).delete(firstSubDir);
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
    void testHandlesCollectionOfMenus() throws Exception {
        enableAspect();

        Menu menu1 = new Menu();
        menu1.setId("menu11111111");
        Menu menu2 = new Menu();
        menu2.setId("menu22222222");
        List<Menu> menus = List.of(menu1, menu2);

        Method method = TestAnnotatedMethods.class.getMethod("saveMenus", List.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{menus});
        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(Path.of("testRoot"));
        when(fileRepository.getSubDir("menu11111111", 0)).thenReturn("men");
        when(fileRepository.getSubDir("menu11111111", 1)).thenReturn("u11");
        when(fileRepository.getSubDir("menu22222222", 0)).thenReturn("men");
        when(fileRepository.getSubDir("menu22222222", 1)).thenReturn("u22");
        when(jsonMapper.writeValueAsBytes(menu1)).thenReturn("{}".getBytes());
        when(jsonMapper.writeValueAsBytes(menu2)).thenReturn("{}".getBytes());

        aspect.persistEntityAsJson(joinPoint);

        verify(fileRepository).write(Path.of("testRoot", "menus", "men", "u11", "menu11111111.json"), "{}".getBytes());
        verify(fileRepository).write(Path.of("testRoot", "menus", "men", "u22", "menu22222222.json"), "{}".getBytes());
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
        when(fileRepository.getSubDir(itemId, 0)).thenReturn("abc");
        when(fileRepository.getSubDir(itemId, 1)).thenReturn("123");

        Path expectedFile = Path.of("testRoot", "items", "abc", "123", "abc123def456.json");
        when(fileRepository.exists(expectedFile)).thenReturn(true);

        Path secondSubDir = Path.of("testRoot", "items", "abc", "123");
        when(fileRepository.exists(secondSubDir)).thenReturn(true);
        when(fileRepository.isDir(secondSubDir)).thenReturn(true);
        when(fileRepository.list(secondSubDir)).thenReturn(List.of(Path.of("otherFile.json")));

        aspect.persistEntityAsJson(joinPoint);

        verify(fileRepository).delete(expectedFile);
        verify(fileRepository, never()).delete(secondSubDir);
    }

    // Helper class with annotated methods for testing
    @SuppressWarnings("unused")
    static class TestAnnotatedMethods {
        @PersistEntityAsJson(entityDir = "items", entityType = Item.class)
        public void saveItem(Item item) {
        }

        @PersistEntityAsJson(entityDir = "items", entityType = Item.class, delete = true)
        public void deleteItem(String itemId) {
        }

        @PersistEntityAsJson(entityDir = "menus", entityType = Menu.class)
        public void saveMenu(Menu menu) {
        }

        @PersistEntityAsJson(entityDir = "menus", entityType = Menu.class)
        public void saveMenus(List<Menu> menus) {
        }

        @PersistEntityAsJson(entityDir = "pages", entityType = PageContent.class)
        public void savePageContent(String pageIdOrAlias, PageContent pageContent) {
        }
    }

}
