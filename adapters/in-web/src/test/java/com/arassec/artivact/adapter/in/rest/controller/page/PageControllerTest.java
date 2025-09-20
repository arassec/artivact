package com.arassec.artivact.adapter.in.rest.controller.page;

import com.arassec.artivact.application.port.in.page.*;
import com.arassec.artivact.domain.model.item.ImageSize;
import com.arassec.artivact.domain.model.page.PageContent;
import com.arassec.artivact.domain.model.page.PageIdAndAlias;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PageControllerTest {

    @Mock
    private LoadPageContentUseCase loadPageContentUseCase;

    @Mock
    private SavePageContentUseCase savePageContentUseCase;

    @Mock
    private ManagePageMediaUseCase managePageMediaUseCase;

    @Mock
    private ResetWipPageContentUseCase resetWipPageContentUseCase;

    @Mock
    private PublishWipPageContentUseCase publishWipPageContentUseCase;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private PageController controller;

    @Test
    void testLoadIndexPageIdOrAliasReturnsAlias() {
        PageIdAndAlias page = new PageIdAndAlias("123", "home");
        when(loadPageContentUseCase.loadIndexPageIdAndAlias()).thenReturn(Optional.of(page));

        String result = controller.loadIndexPageIdOrAlias();

        assertThat(result).isEqualTo("home");
    }

    @Test
    void testLoadIndexPageIdOrAliasReturnsIdIfAliasEmpty() {
        PageIdAndAlias page = new PageIdAndAlias("123", "");
        when(loadPageContentUseCase.loadIndexPageIdAndAlias()).thenReturn(Optional.of(page));

        String result = controller.loadIndexPageIdOrAlias();

        assertThat(result).isEqualTo("123");
    }

    @Test
    void testLoadIndexPageIdOrAliasReturnsEmptyStringIfNone() {
        when(loadPageContentUseCase.loadIndexPageIdAndAlias()).thenReturn(Optional.empty());

        String result = controller.loadIndexPageIdOrAlias();

        assertThat(result).isEmpty();
    }

    @Test
    void testLoadPageContentDelegatesToUseCase() {
        PageContent expected = new PageContent();
        mockRoles("ROLE_USER");

        when(loadPageContentUseCase.loadTranslatedRestrictedPageContent(eq("alias"), any())).thenReturn(expected);

        PageContent result = controller.loadPageContent("alias", authentication);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void testLoadWipPageContentDelegatesToUseCase() {
        PageContent expected = new PageContent();
        mockRoles("ROLE_ADMIN");

        when(loadPageContentUseCase.loadTranslatedRestrictedWipPageContent(eq("page1"), any())).thenReturn(expected);

        PageContent result = controller.loadWipPageContent("page1", authentication);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void testSavePageContentDelegatesToUseCase() {
        PageContent input = new PageContent();
        PageContent saved = new PageContent();
        mockRoles("ROLE_EDITOR");

        when(savePageContentUseCase.savePageContent(eq("page2"), any(), eq(input))).thenReturn(saved);

        PageContent result = controller.savePageContent("page2", input, authentication);

        assertThat(result).isSameAs(saved);
    }

    @Test
    void testSaveFileDelegatesToUseCase() {
        MultipartFile file = mock(MultipartFile.class);
        when(managePageMediaUseCase.saveFile("p1", "w1", file)).thenReturn("saved.png");

        ResponseEntity<String> response = controller.saveFile("p1", "w1", file);

        assertThat(response.getBody()).isEqualTo("saved.png");
    }

    @Test
    void testDeleteFileDelegatesToUseCase() {
        PageContent updated = new PageContent();
        when(managePageMediaUseCase.deleteFile("p2", "w2", "f2.png")).thenReturn(updated);

        PageContent result = controller.deleteFile("p2", "w2", "f2.png");

        assertThat(result).isSameAs(updated);
    }

    @Test
    void testLoadFileReturnsHeadersAndData() {
        byte[] data = "hello".getBytes();
        when(managePageMediaUseCase.loadFile("w1", "test.txt", null, false)).thenReturn(data);

        HttpEntity<byte[]> response = controller.loadFile("w1", "test.txt", null);

        assertThat(response.getBody()).isEqualTo(data);
        assertThat(Objects.requireNonNull(response.getHeaders().getContentType())).hasToString("text/plain");
    }

    @Test
    void testLoadFileReturnsEmptyArrayIfFilenameBlank() {
        HttpEntity<byte[]> response = controller.loadFile("w1", "", null);

        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void testLoadWipFileDelegatesAndSetsHeaders() {
        byte[] data = "wipdata".getBytes();
        when(managePageMediaUseCase.loadFile("w2", "image.png", ImageSize.ITEM_CARD, true)).thenReturn(data);

        HttpEntity<byte[]> response = controller.loadWipFile("w2", "image.png", ImageSize.ITEM_CARD);

        assertThat(response.getBody()).isEqualTo(data);
        assertThat(Objects.requireNonNull(response.getHeaders().getContentType())).hasToString("image/png");
    }

    @Test
    void testLoadWipFileReturnsEmptyArrayIfFilenameBlank() {
        HttpEntity<byte[]> response = controller.loadWipFile("w3", "  ", null);

        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void testResetWipPageContentDelegatesToUseCase() {
        PageContent reset = new PageContent();
        when(resetWipPageContentUseCase.resetWipPageContent("page3")).thenReturn(reset);

        PageContent result = controller.resetWipPageContent("page3");

        assertThat(result).isSameAs(reset);
    }

    @Test
    void testPublishWipPageContentDelegatesToUseCase() {
        PageContent published = new PageContent();
        when(publishWipPageContentUseCase.publishWipPageContent("page4")).thenReturn(published);

        PageContent result = controller.publishWipPageContent("page4");

        assertThat(result).isSameAs(published);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void mockRoles(String... roles) {
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getAuthorities()).thenReturn(
                (Collection) Stream.of(roles).map(r -> (GrantedAuthority) () -> r).toList()
        );
    }

}
