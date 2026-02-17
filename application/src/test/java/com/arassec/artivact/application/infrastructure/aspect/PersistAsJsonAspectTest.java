package com.arassec.artivact.application.infrastructure.aspect;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.menu.Menu;
import com.arassec.artivact.domain.model.page.PageContent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.json.JsonMapper;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersistAsJsonAspectTest {

    @TempDir
    Path tempDir;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    private PersistAsJsonAspect aspect;
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        jsonMapper = new JsonMapper();
        aspect = new PersistAsJsonAspect(jsonMapper, fileRepository, useProjectDirsUseCase);
        ReflectionTestUtils.setField(aspect, "jsonPersistenceEnabled", true);

        when(useProjectDirsUseCase.getProjectRoot()).thenReturn(tempDir);
        doAnswer(invocation -> {
            Path dir = invocation.getArgument(0);
            Files.createDirectories(dir);
            return null;
        }).when(fileRepository).createDirIfRequired(any(Path.class));
        when(fileRepository.list(any(Path.class))).thenReturn(List.of());
    }

    @Test
    void testPersistItemAsJson() throws Exception {
        // Given
        Item item = new Item();
        item.setId("123456789");
        item.setVersion(1);

        Method method = TestService.class.getMethod("saveItem", Item.class);
        PersistAsJson annotation = method.getAnnotation(PersistAsJson.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{item});

        // When
        aspect.persistAsJson(joinPoint, item);

        // Then
        Path jsonFile = tempDir.resolve("items/123/456/123456789.json");
        assertThat(jsonFile).exists();
        assertThat(Files.readString(jsonFile)).contains("\"id\" : \"123456789\"");
    }

    @Test
    void testPersistMenuAsJson() throws Exception {
        // Given
        Menu menu = new Menu();
        menu.setId("abc123def");
        menu.setRestrictions(Set.of("ROLE_USER"));

        Method method = TestService.class.getMethod("saveMenu", Menu.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{menu});

        // When
        aspect.persistAsJson(joinPoint, menu);

        // Then
        Path jsonFile = tempDir.resolve("menus/abc/123/abc123def.json");
        assertThat(jsonFile).exists();
        assertThat(Files.readString(jsonFile)).contains("\"id\" : \"abc123def\"");
    }

    @Test
    void testPersistPageContentAsJson() throws Exception {
        // Given
        PageContent pageContent = new PageContent();
        pageContent.setId("xyz789abc");
        pageContent.setEditable(true);

        Method method = TestService.class.getMethod("savePageContent", PageContent.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{pageContent});

        // When
        aspect.persistAsJson(joinPoint, pageContent);

        // Then
        Path jsonFile = tempDir.resolve("pages/xyz/789/xyz789abc.json");
        assertThat(jsonFile).exists();
        assertThat(Files.readString(jsonFile)).contains("\"id\" : \"xyz789abc\"");
    }

    @Test
    void testDoesNotPersistWhenIdTooShort() throws Exception {
        // Given
        Item item = new Item();
        item.setId("12345"); // Only 5 characters

        Method method = TestService.class.getMethod("saveItem", Item.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{item});

        // When
        aspect.persistAsJson(joinPoint, item);

        // Then
        assertThat(tempDir.resolve("items")).doesNotExist();
    }

    @Test
    void testDoesNotPersistWhenDisabled() throws Exception {
        // Given
        ReflectionTestUtils.setField(aspect, "jsonPersistenceEnabled", false);
        Item item = new Item();
        item.setId("123456789");

        Method method = TestService.class.getMethod("saveItem", Item.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{item});

        // When
        aspect.persistAsJson(joinPoint, item);

        // Then
        assertThat(tempDir.resolve("items")).doesNotExist();
    }

    @Test
    void testPersistCollectionAsJson() throws Exception {
        // Given
        Menu menu1 = new Menu();
        menu1.setId("menu001abc");
        Menu menu2 = new Menu();
        menu2.setId("menu002def");
        List<Menu> menus = List.of(menu1, menu2);

        Method method = TestService.class.getMethod("saveMenus", List.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{menus});

        // When
        aspect.persistAsJson(joinPoint, menus);

        // Then
        assertThat(tempDir.resolve("menus/men/u00/menu001abc.json")).exists();
        assertThat(tempDir.resolve("menus/men/u00/menu002def.json")).exists();
    }

    @Test
    void testDeleteEntityJson() throws Exception {
        // Given - create a JSON file first
        Item item = new Item();
        item.setId("delete123");

        Method saveMethod = TestService.class.getMethod("saveItem", Item.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(saveMethod);
        when(joinPoint.getArgs()).thenReturn(new Object[]{item});
        aspect.persistAsJson(joinPoint, item);

        Path jsonFile = tempDir.resolve("items/del/ete/delete123.json");
        assertThat(jsonFile).exists();

        // When - delete the entity
        Method deleteMethod = TestService.class.getMethod("deleteItem", String.class);
        when(methodSignature.getMethod()).thenReturn(deleteMethod);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"delete123"});
        aspect.deleteEntityJson(joinPoint);

        // Then
        assertThat(jsonFile).doesNotExist();
        // Parent directories should also be deleted if empty
        assertThat(jsonFile.getParent()).doesNotExist();
        assertThat(jsonFile.getParent().getParent()).doesNotExist();
    }

    @Test
    void testErrorHandlingDoesNotThrowException() throws Exception {
        // Given
        Item item = new Item();
        item.setId("error12345");

        Method method = TestService.class.getMethod("saveItem", Item.class);

        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{item});
        doThrow(new RuntimeException("Simulated error")).when(fileRepository).createDirIfRequired(any());

        // When/Then - should not throw exception
        aspect.persistAsJson(joinPoint, item);
    }

    // Test service with annotated methods
    static class TestService {
        @PersistAsJson("items")
        public Item saveItem(Item item) {
            return item;
        }

        @PersistAsJson("menus")
        public Menu saveMenu(Menu menu) {
            return menu;
        }

        @PersistAsJson("menus")
        public List<Menu> saveMenus(List<Menu> menus) {
            return menus;
        }

        @PersistAsJson("pages")
        public PageContent savePageContent(PageContent pageContent) {
            return pageContent;
        }

        @DeleteEntityJson("items")
        public void deleteItem(String itemId) {
        }
    }
}
