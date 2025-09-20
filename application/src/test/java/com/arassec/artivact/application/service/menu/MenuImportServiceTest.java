package com.arassec.artivact.application.service.menu;

import com.arassec.artivact.application.port.in.configuration.ImportPropertiesConfigurationUseCase;
import com.arassec.artivact.application.port.in.configuration.ImportTagsConfigurationUseCase;
import com.arassec.artivact.application.port.in.menu.SaveMenuUseCase;
import com.arassec.artivact.application.port.in.page.ImportPageUseCase;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.exchange.ExchangeMainData;
import com.arassec.artivact.domain.model.exchange.ImportContext;
import com.arassec.artivact.domain.model.menu.Menu;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuImportServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UseProjectDirsUseCase useProjectDirsUseCase;

    @Mock
    private ImportPageUseCase importPageUseCase;

    @Mock
    private SaveMenuUseCase saveMenuUseCase;

    @Mock
    private ImportPropertiesConfigurationUseCase importPropertiesConfigurationUseCase;

    @Mock
    private ImportTagsConfigurationUseCase importTagsConfigurationUseCase;

    @InjectMocks
    private MenuImportService menuImportService;

    @BeforeEach
    void setup() {
        objectMapper = mock(ObjectMapper.class);
        fileRepository = mock(FileRepository.class);
        useProjectDirsUseCase = mock(UseProjectDirsUseCase.class);
        importPageUseCase = mock(ImportPageUseCase.class);
        saveMenuUseCase = mock(SaveMenuUseCase.class);
        importPropertiesConfigurationUseCase = mock(ImportPropertiesConfigurationUseCase.class);
        importTagsConfigurationUseCase = mock(ImportTagsConfigurationUseCase.class);

        menuImportService = new MenuImportService(
                objectMapper,
                fileRepository,
                useProjectDirsUseCase,
                importPageUseCase,
                saveMenuUseCase,
                importPropertiesConfigurationUseCase,
                importTagsConfigurationUseCase
        );
    }

    @Test
    void testImportMenuFromPathSuccess() throws Exception {
        Path zipFile = Path.of("export.zip");
        ExchangeMainData exchangeMainData = new ExchangeMainData();
        exchangeMainData.setSourceId("menu123");

        when(fileRepository.read(Path.of("temp/export/menu123.artivact.menu.json"))).thenReturn("valid-json");

        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("temp"));
        when(objectMapper.readValue(any(File.class), eq(ExchangeMainData.class))).thenReturn(exchangeMainData);
        when(objectMapper.readValue("valid-json", Menu.class)).thenReturn(new Menu());

        menuImportService.importMenu(zipFile);

        verify(fileRepository).unpack(eq(zipFile), any());
        verify(importPropertiesConfigurationUseCase).importPropertiesConfiguration(any(ImportContext.class));
        verify(importTagsConfigurationUseCase).importTagsConfiguration(any(ImportContext.class));
        verify(fileRepository).delete(any(Path.class));
    }

    @Test
    void testImportMenuFromPathThrowsException() throws Exception {
        Path zipFile = Path.of("export.zip");

        when(useProjectDirsUseCase.getTempDir()).thenReturn(Path.of("temp"));
        when(objectMapper.readValue(anyString(), eq(ExchangeMainData.class))).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> menuImportService.importMenu(zipFile))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not import data!");
    }

    @Test
    void testImportMenuWithSaveMenuTrue() throws Exception {
        ImportContext ctx = ImportContext.builder().importDir(Path.of("importDir")).build();
        Menu menu = new Menu();
        menu.setId("menu123");
        menu.setParentId("parent");
        menu.setTargetPageId("page1");
        menu.setTargetPageAlias("alias1");
        menu.setMenuEntries(List.of());

        when(fileRepository.read(any())).thenReturn("json-content");
        when(objectMapper.readValue(anyString(), eq(Menu.class))).thenReturn(menu);

        menuImportService.importMenu(ctx, "menu123", true);

        verify(saveMenuUseCase).saveMenu(menu);
        verify(importPageUseCase).importPage(ctx, "page1", "alias1");

        assertThat(menu.getParentId()).isNull();
    }

    @Test
    void testImportMenuWithSubmenus() throws Exception {
        ImportContext ctx = ImportContext.builder().importDir(Path.of("importDir")).build();
        Menu submenu = new Menu();
        submenu.setId("submenu");
        submenu.setMenuEntries(List.of());

        Menu submenuEntry = new Menu();
        submenuEntry.setId("submenu");
        submenuEntry.setParentId(null);

        Menu menu = new Menu();
        menu.setId("menu123");
        menu.setMenuEntries(List.of(submenuEntry));

        when(fileRepository.read(any())).thenReturn("json-content");
        when(objectMapper.readValue(anyString(), eq(Menu.class)))
                .thenReturn(menu)
                .thenReturn(submenu);

        menuImportService.importMenu(ctx, "menu123", true);

        verify(saveMenuUseCase).saveMenu(menu);

        verify(fileRepository, times(2)).read(any());
    }

    @Test
    void testImportMenuThrowsJsonProcessingException() throws Exception {
        ImportContext ctx = ImportContext.builder().importDir(Path.of("importDir")).build();

        when(fileRepository.read(any())).thenReturn("invalid-json");
        when(objectMapper.readValue(anyString(), eq(Menu.class))).thenThrow(new JsonProcessingException("boom") {
        });

        assertThatThrownBy(() -> menuImportService.importMenu(ctx, "menu123", true))
                .isInstanceOf(ArtivactException.class)
                .hasMessageContaining("Could not import menu!");
    }
}
