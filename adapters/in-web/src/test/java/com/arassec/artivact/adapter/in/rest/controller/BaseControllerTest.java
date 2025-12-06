package com.arassec.artivact.adapter.in.rest.controller;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link BaseController}.
 */
class BaseControllerTest {

    /**
     * Concrete implementation for testing.
     */
    private static class TestableBaseController extends BaseController {
        // No additional methods needed - just exposing protected methods
        public String testCreateImageUrl(String itemId, String filename) {
            return createImageUrl(itemId, filename);
        }

        public String testCreateUrl(String itemId, String fileName, String fileType) {
            return createUrl(itemId, fileName, fileType);
        }
    }

    private final TestableBaseController controller = new TestableBaseController();

    /**
     * Tests the NO_CACHE constant.
     */
    @Test
    void testNoCacheConstant() {
        assertThat(BaseController.NO_CACHE).isEqualTo("no-cache");
    }

    /**
     * Tests the ATTACHMENT_PREFIX constant.
     */
    @Test
    void testAttachmentPrefixConstant() {
        assertThat(BaseController.ATTACHMENT_PREFIX).isEqualTo("attachment; filename=");
    }

    /**
     * Tests the EXPIRES_IMMEDIATELY constant.
     */
    @Test
    void testExpiresImmediatelyConstant() {
        assertThat(BaseController.EXPIRES_IMMEDIATELY).isEqualTo("0");
    }

    /**
     * Tests the TYPE_ZIP constant.
     */
    @Test
    void testTypeZipConstant() {
        assertThat(BaseController.TYPE_ZIP).isEqualTo("application/zip");
    }

    /**
     * Tests creating an image URL.
     */
    @Test
    void testCreateImageUrl() {
        String result = controller.testCreateImageUrl("item123", "photo.jpg");
        assertThat(result).isEqualTo("/api/item/item123/image/photo.jpg");
    }

    /**
     * Tests creating a URL with custom file type.
     */
    @Test
    void testCreateUrl() {
        String result = controller.testCreateUrl("item456", "model.glb", "model");
        assertThat(result).isEqualTo("/api/item/item456/model/model.glb");
    }

    /**
     * Tests creating a URL with different file types.
     */
    @Test
    void testCreateUrlWithDifferentFileTypes() {
        assertThat(controller.testCreateUrl("id1", "file.txt", "document"))
                .isEqualTo("/api/item/id1/document/file.txt");
        assertThat(controller.testCreateUrl("id2", "video.mp4", "media"))
                .isEqualTo("/api/item/id2/media/video.mp4");
    }

}
