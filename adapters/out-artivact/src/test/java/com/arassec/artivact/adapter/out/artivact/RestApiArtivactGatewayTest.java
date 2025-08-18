package com.arassec.artivact.adapter.out.artivact;

import com.arassec.artivact.domain.exception.ArtivactException;
import lombok.SneakyThrows;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Tests the Rest-API Artivact-Gateway.
 */
class RestApiArtivactGatewayTest {

    /**
     * Tests importing an item on a remote Artivact instance.
     */
    @Test
    @SneakyThrows
    void testImportItem() {
        RestApiArtivactGateway artivactGateway = new RestApiArtivactGateway();

        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);

        try (MockedStatic<HttpClients> httpClientsMock = Mockito.mockStatic(HttpClients.class)) {
            httpClientsMock.when(HttpClients::createDefault).thenReturn(httpClientMock);

            artivactGateway.importItem("removeServer", "apiKey", Path.of("item-export.zip"));

            verify(httpClientMock).execute(any(HttpPost.class), any(HttpClientResponseHandler.class));
        }
    }

    /**
     * Tests error handling during importing an item on a remote Artivact instance.
     */
    @Test
    @SneakyThrows
    void testImportItemFail() {
        RestApiArtivactGateway artivactGateway = new RestApiArtivactGateway();

        CloseableHttpClient httpClientMock = mock(CloseableHttpClient.class);

        try (MockedStatic<HttpClients> httpClientsMock = Mockito.mockStatic(HttpClients.class)) {
            httpClientsMock.when(HttpClients::createDefault).thenReturn(httpClientMock);
            when(httpClientMock.execute(any(HttpPost.class), any(HttpClientResponseHandler.class)))
                    .thenThrow(IOException.class);

            assertThrows(ArtivactException.class, () -> artivactGateway.importItem("removeServer", "apiKey", Path.of("item-export.zip")));
        }
    }

}
