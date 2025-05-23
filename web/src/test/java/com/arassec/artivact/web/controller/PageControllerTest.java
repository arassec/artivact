package com.arassec.artivact.web.controller;

import com.arassec.artivact.core.model.item.ImageSize;
import com.arassec.artivact.core.model.page.PageContent;
import com.arassec.artivact.core.repository.PageIdAndAlias;
import com.arassec.artivact.domain.service.PageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link PageController}.
 */
@ExtendWith(MockitoExtension.class)
class PageControllerTest {

    /**
     * The controller under test.
     */
    @InjectMocks
    private PageController controller;

    /**
     * Service mock.
     */
    @Mock
    private PageService pageService;

    /**
     * Spring-Security Authentication mock.
     */
    private final Authentication authentication = mock(Authentication.class);

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void setUp() {
        UserDetails userDetails = new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Set.of(new SimpleGrantedAuthority("ROLE_USER"));
            }

            @Override
            public String getPassword() {
                return "password";
            }

            @Override
            public String getUsername() {
                return "username";
            }
        };
        when(authentication.getPrincipal()).thenReturn(userDetails);
    }

    /**
     * Tests loading the index page's alias or ID.
     */
    @Test
    void testLoadIndexPageIdOrAlias() {
        when(pageService.loadIndexPageIdAndAlias()).thenReturn(Optional.empty());
        assertThat(controller.loadIndexPageIdOrAlias()).isEqualTo("");

        when(pageService.loadIndexPageIdAndAlias()).thenReturn(Optional.of(new PageIdAndAlias() {
            @Override
            public String getId() {
                return "id";
            }

            @Override
            public String getAlias() {
                return "alias";
            }
        }));
        assertThat(controller.loadIndexPageIdOrAlias()).isEqualTo("alias");

        when(pageService.loadIndexPageIdAndAlias()).thenReturn(Optional.of(new PageIdAndAlias() {
            @Override
            public String getId() {
                return "id";
            }

            @Override
            public String getAlias() {
                return null;
            }
        }));
        assertThat(controller.loadIndexPageIdOrAlias()).isEqualTo("id");
    }

    /**
     * Tests loading translated page content.
     */
    @Test
    void testLoadTranslatedPageContent() {
        controller.loadTranslatedPageContent("page-id", authentication);
        verify(pageService, times(1)).loadTranslatedRestrictedPageContent("page-id", Set.of("ROLE_USER"));
    }

    /**
     * Tests saving page content.
     */
    @Test
    void testSavePageContent() {
        PageContent pageContent = new PageContent();
        controller.savePageContent("page-id", pageContent, authentication);
        verify(pageService, times(1)).savePageContent("page-id", Set.of("ROLE_USER"), pageContent);
    }

    /**
     * Tests saving a file.
     */
    @Test
    void testSaveFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(pageService.saveFile("page-id", "widget-id", file)).thenReturn("file.txt");

        ResponseEntity<String> responseEntity = controller.saveFile("page-id", "widget-id", file);

        assertEquals("file.txt", responseEntity.getBody());
    }

    /**
     * Tests loading a file.
     */
    @Test
    void testLoadFile() {
        byte[] file = "file-content".getBytes();
        when(pageService.loadFile("widget-id", "file.jpg", ImageSize.ITEM_CARD)).thenReturn(file);

        HttpEntity<byte[]> response = controller.loadFile("widget-id", "file.jpg", ImageSize.ITEM_CARD);

        byte[] body = response.getBody();

        List<String> contentDispositionHeader = response.getHeaders().get("Content-Disposition");
        assertNotNull(contentDispositionHeader);
        assertEquals("inline; filename=\"file.jpg\"", contentDispositionHeader.getFirst());

        assertEquals("image/jpeg", response.getHeaders().getFirst("Content-Type"));

        assertNotNull(body);
        assertEquals("file-content", new String(body));
    }

}
