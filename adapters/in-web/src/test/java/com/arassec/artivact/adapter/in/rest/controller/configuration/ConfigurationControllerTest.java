package com.arassec.artivact.adapter.in.rest.controller.configuration;

import com.arassec.artivact.adapter.in.rest.model.ApplicationSettings;
import com.arassec.artivact.adapter.in.rest.model.UserData;
import com.arassec.artivact.application.port.in.ai.TestAiConfigurationUseCase;
import com.arassec.artivact.application.port.in.ai.TranslateTextUseCase;
import com.arassec.artivact.application.port.in.configuration.*;
import com.arassec.artivact.application.port.in.project.CleanupExportFilesUseCase;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.Roles;
import com.arassec.artivact.domain.model.appearance.ColorTheme;
import com.arassec.artivact.domain.model.appearance.License;
import com.arassec.artivact.domain.model.configuration.AiConfiguration;
import com.arassec.artivact.domain.model.configuration.AiModel;
import com.arassec.artivact.domain.model.configuration.AppearanceConfiguration;
import com.arassec.artivact.domain.model.configuration.ExchangeConfiguration;
import com.arassec.artivact.domain.model.misc.ExchangeDefinitions;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationControllerTest {

	@Mock
	private ExportPropertiesConfigurationUseCase exportPropertiesConfigurationUseCase;

	@Mock
	private ExportTagsConfigurationUseCase exportTagsConfigurationUseCase;

	@Mock
	private LoadAppearanceConfigurationUseCase loadAppearanceConfigurationUseCase;

	@Mock
	private LoadExchangeConfigurationUseCase loadExchangeConfigurationUseCase;

	@Mock
	private CheckRuntimeConfigurationUseCase checkRuntimeConfigurationUseCase;

	@Mock
	private SaveAppearanceConfigurationUseCase saveAppearanceConfigurationUseCase;

	@Mock
	private CleanupExportFilesUseCase cleanupExportFilesUseCase;

	@Mock
	private ImportPropertiesConfigurationUseCase importPropertiesConfigurationUseCase;

	@Mock
	private ImportTagsConfigurationUseCase importTagsConfigurationUseCase;

	@Mock
	private LoadAiConfigurationUseCase loadAiConfigurationUseCase;

	@Mock
	private TranslateTextUseCase translateTextUseCase;

	@Mock
	private TestAiConfigurationUseCase testAiConfigurationUseCase;

	@InjectMocks
	private ConfigurationController controller;

	@Test
	void returnsApplicationSettingsWithParsedLocalesProfilesAndEnabledAiFeatures() {
		AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();
		appearanceConfiguration.setApplicationTitle("Artivact");
		appearanceConfiguration.setAvailableLocales("en, de , fr");
		appearanceConfiguration.setApplicationLocale("de");
		appearanceConfiguration.setDefaultLocale("en");
		appearanceConfiguration.setColorTheme(new ColorTheme());
		appearanceConfiguration.setLicense(License.builder().licenseUrl("https://license.example").build());

		ExchangeConfiguration exchangeConfiguration = new ExchangeConfiguration("https://remote.example", "api-token");

		AiConfiguration aiConfiguration = new AiConfiguration();
		aiConfiguration.setTranslationModel(null);
		aiConfiguration.setTranslationApiKey("translation-key");
		aiConfiguration.setTtsApiKey("tts-key");

		when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(appearanceConfiguration);
		when(checkRuntimeConfigurationUseCase.isDesktopProfileEnabled()).thenReturn(true);
		when(checkRuntimeConfigurationUseCase.isE2eProfileEnabled()).thenReturn(false);
		when(loadExchangeConfigurationUseCase.loadExchangeConfiguration()).thenReturn(exchangeConfiguration);
		when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);

		ApplicationSettings result = controller.getApplicationSettings();

		assertThat(result.getApplicationTitle()).isEqualTo("Artivact");
		assertThat(result.getAvailableLocales()).containsExactly("en", "de", "fr");
		assertThat(result.getApplicationLocale()).isEqualTo("de");
		assertThat(result.getDefaultLocale()).isEqualTo("en");
		assertThat(result.getProfiles().isDesktop()).isTrue();
		assertThat(result.getProfiles().isE2e()).isFalse();
		assertThat(result.getAvailableRoles()).containsExactly(Roles.ROLE_ADMIN, Roles.ROLE_USER);
		assertThat(result.isSyncAvailable()).isTrue();
		assertThat(result.isTranslationEnabled()).isTrue();
		assertThat(result.isTtsEnabled()).isTrue();
		assertThat(result.getLicense().getLicenseUrl()).isEqualTo("https://license.example");
	}

	@Test
	void returnsApplicationSettingsWithEmptyLocalesAndDisabledOptionalFeaturesWhenConfigurationIsIncomplete() {
		AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();
		appearanceConfiguration.setAvailableLocales("   ");

		ExchangeConfiguration exchangeConfiguration = new ExchangeConfiguration("https://remote.example", " ");

		AiConfiguration aiConfiguration = new AiConfiguration();
		aiConfiguration.setTranslationModel(AiModel.ELEVENLABS);
		aiConfiguration.setTranslationApiKey("translation-key");
		aiConfiguration.setTtsApiKey(" ");

		when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(appearanceConfiguration);
		when(checkRuntimeConfigurationUseCase.isDesktopProfileEnabled()).thenReturn(false);
		when(checkRuntimeConfigurationUseCase.isE2eProfileEnabled()).thenReturn(true);
		when(loadExchangeConfigurationUseCase.loadExchangeConfiguration()).thenReturn(exchangeConfiguration);
		when(loadAiConfigurationUseCase.loadAiConfiguration()).thenReturn(aiConfiguration);

		ApplicationSettings result = controller.getApplicationSettings();

		assertThat(result.getAvailableLocales()).isEmpty();
		assertThat(result.getProfiles().isDesktop()).isFalse();
		assertThat(result.getProfiles().isE2e()).isTrue();
		assertThat(result.isSyncAvailable()).isFalse();
		assertThat(result.isTranslationEnabled()).isFalse();
		assertThat(result.isTtsEnabled()).isFalse();
	}

	@Test
	void returnsAuthenticatedUserDataForUserDetailsPrincipal() {
		UserDetails userDetails = User.withUsername("alice")
				.password("secret")
				.authorities(Roles.ROLE_ADMIN, Roles.ROLE_USER)
				.build();

		Authentication authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn(userDetails);

		UserData result = controller.getUserData(authentication);

		assertThat(result.isAuthenticated()).isTrue();
		assertThat(result.getUsername()).isEqualTo("alice");
		assertThat(result.getRoles()).containsExactlyInAnyOrder(Roles.ROLE_ADMIN, Roles.ROLE_USER);
	}

	@Test
	void returnsUnauthenticatedUserDataWhenAuthenticationIsMissing() {
		UserData result = controller.getUserData(null);

		assertThat(result.isAuthenticated()).isFalse();
		assertThat(result.getUsername()).isNull();
		assertThat(result.getRoles()).isEmpty();
	}

	@Test
	void returnsUnauthenticatedUserDataWhenPrincipalDoesNotImplementUserDetails() {
		Authentication authentication = mock(Authentication.class);
		when(authentication.getPrincipal()).thenReturn("anonymous");

		UserData result = controller.getUserData(authentication);

		assertThat(result.isAuthenticated()).isFalse();
		assertThat(result.getUsername()).isNull();
		assertThat(result.getRoles()).isEmpty();
	}

	@Test
	void returnsDecodedFaviconWithExpectedHeaders() {
		byte[] faviconBytes = "ico".getBytes(StandardCharsets.UTF_8);
		AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();
		appearanceConfiguration.setEncodedFavicon(Base64.getEncoder().encodeToString(faviconBytes));

		when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(appearanceConfiguration);

		HttpEntity<byte[]> result = controller.getFavicon();

		assertThat(result.getBody()).isEqualTo(faviconBytes);
		assertThat(result.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION)).isEqualTo("favicon.ico");
		assertThat(result.getHeaders().getFirst(HttpHeaders.PRAGMA)).isEqualTo(ConfigurationController.NO_CACHE);
		assertThat(result.getHeaders().getFirst(HttpHeaders.EXPIRES)).isEqualTo(ConfigurationController.EXPIRES_IMMEDIATELY);
		assertThat(result.getHeaders().getContentType()).isEqualTo(MediaType.valueOf("image/x-icon"));
	}

	@Test
	void savesUploadedFaviconAsBase64EncodedAppearanceConfiguration() {
		AppearanceConfiguration appearanceConfiguration = new AppearanceConfiguration();
		MockMultipartFile file = new MockMultipartFile("file", "favicon.ico", "image/x-icon", "ico".getBytes(StandardCharsets.UTF_8));
		ArgumentCaptor<AppearanceConfiguration> configurationCaptor = ArgumentCaptor.forClass(AppearanceConfiguration.class);

		when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(appearanceConfiguration);

		ResponseEntity<Void> result = controller.uploadFavicon(file);

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
		verify(saveAppearanceConfigurationUseCase).saveAppearanceConfiguration(configurationCaptor.capture());
		assertThat(configurationCaptor.getValue().getEncodedFavicon())
				.isEqualTo(Base64.getEncoder().encodeToString("ico".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	void wrapsFaviconUploadReadErrorsInArtivactException() throws IOException {
		MultipartFile file = mock(MultipartFile.class);
		when(loadAppearanceConfigurationUseCase.loadTranslatedAppearanceConfiguration()).thenReturn(new AppearanceConfiguration());
		when(file.getBytes()).thenThrow(new IOException("broken"));

		assertThatThrownBy(() -> controller.uploadFavicon(file))
				.isInstanceOf(ArtivactException.class)
				.hasMessageContaining("Could not Base64 encode favicon!");
	}

	@Test
	void streamsExportedPropertiesConfigurationAndCleansUpAfterWriting() throws Exception {
		String exportedConfiguration = "{\"categories\":[]}";
		ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getOutputStream()).thenReturn(createServletOutputStream(responseBytes));
		when(exportPropertiesConfigurationUseCase.exportPropertiesConfiguration()).thenReturn(exportedConfiguration);

		ResponseEntity<StreamingResponseBody> result = controller.exportPropertiesConfiguration(response);

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(result.getBody()).isNotNull();
		verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
		verify(response).setHeader(HttpHeaders.CONTENT_DISPOSITION,
				ConfigurationController.ATTACHMENT_PREFIX + LocalDate.now() + "." + ExchangeDefinitions.PROPERTIES_EXCHANGE_FILENAME_JSON);
		verify(response).addHeader(HttpHeaders.PRAGMA, ConfigurationController.NO_CACHE);
		verify(response).addHeader(HttpHeaders.EXPIRES, ConfigurationController.EXPIRES_IMMEDIATELY);

		result.getBody().writeTo(new ByteArrayOutputStream());

		assertThat(responseBytes.toString(StandardCharsets.UTF_8)).isEqualTo(exportedConfiguration);
		verify(response).setContentLength(exportedConfiguration.getBytes().length);
		verify(cleanupExportFilesUseCase).cleanupPropertiesConfigurationExport();
	}

	@Test
	void streamsExportedTagsConfigurationAndCleansUpAfterWriting() throws Exception {
		String exportedConfiguration = "{\"tags\":[]}";
		ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.getOutputStream()).thenReturn(createServletOutputStream(responseBytes));
		when(exportTagsConfigurationUseCase.exportTagsConfiguration()).thenReturn(exportedConfiguration);

		ResponseEntity<StreamingResponseBody> result = controller.exportTagsConfiguration(response);

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(result.getBody()).isNotNull();
		verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
		verify(response).setHeader(HttpHeaders.CONTENT_DISPOSITION,
				ConfigurationController.ATTACHMENT_PREFIX + LocalDate.now() + "." + ExchangeDefinitions.TAGS_EXCHANGE_FILENAME_JSON);
		verify(response).addHeader(HttpHeaders.PRAGMA, ConfigurationController.NO_CACHE);
		verify(response).addHeader(HttpHeaders.EXPIRES, ConfigurationController.EXPIRES_IMMEDIATELY);

		result.getBody().writeTo(new ByteArrayOutputStream());

		assertThat(responseBytes.toString(StandardCharsets.UTF_8)).isEqualTo(exportedConfiguration);
		verify(response).setContentLength(exportedConfiguration.getBytes().length);
		verify(cleanupExportFilesUseCase).cleanupTagsConfigurationExport();
	}

	@Test
	void returnsTranslatedTextForRequestedLocale() {
		when(translateTextUseCase.translateText("Hello world", "de")).thenReturn("Hallo Welt");

		ResponseEntity<String> result = controller.translateText("de", "Hello world");

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(result.getBody()).isEqualTo("Hallo Welt");
	}

	@Test
	void returnsGeneratedTestTtsAudio() {
		byte[] audio = new byte[]{1, 2, 3};
		when(testAiConfigurationUseCase.loadTestTtsAudio()).thenReturn(audio);

		ResponseEntity<byte[]> result = controller.getTestTtsAudio();

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(result.getBody()).isEqualTo(audio);
	}

	@Test
	void importsPropertiesConfigurationFromUploadedFile() {
		MockMultipartFile file = new MockMultipartFile(
				"file", "properties.json", MediaType.APPLICATION_JSON_VALUE, "{\"categories\":[]}".getBytes(StandardCharsets.UTF_8));

		ResponseEntity<String> result = controller.importPropertiesConfiguration(file);

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(result.getBody()).isEqualTo("Properties imported.");
		verify(importPropertiesConfigurationUseCase).importPropertiesConfiguration("{\"categories\":[]}");
	}

	@Test
	void wrapsPropertiesImportReadErrorsInArtivactException() throws IOException {
		MultipartFile file = mock(MultipartFile.class);
		when(file.getBytes()).thenThrow(new IOException("broken"));

		assertThatThrownBy(() -> controller.importPropertiesConfiguration(file))
				.isInstanceOf(ArtivactException.class)
				.hasMessageContaining("Could not read properties configuration file!");
	}

	@Test
	void importsTagsConfigurationFromUploadedFile() {
		MockMultipartFile file = new MockMultipartFile(
				"file", "tags.json", MediaType.APPLICATION_JSON_VALUE, "{\"tags\":[]}".getBytes(StandardCharsets.UTF_8));

		ResponseEntity<String> result = controller.importTagsConfiguration(file);

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(result.getBody()).isEqualTo("Tags imported.");
		verify(importTagsConfigurationUseCase).importTagsConfiguration("{\"tags\":[]}");
	}

	@Test
	void wrapsTagsImportReadErrorsInArtivactException() throws IOException {
		MultipartFile file = mock(MultipartFile.class);
		when(file.getBytes()).thenThrow(new IOException("broken"));

		assertThatThrownBy(() -> controller.importTagsConfiguration(file))
				.isInstanceOf(ArtivactException.class)
				.hasMessageContaining("Could not read tags configuration file!");
	}

	private ServletOutputStream createServletOutputStream(ByteArrayOutputStream outputStream) {
		return new ServletOutputStream() {
			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setWriteListener(WriteListener writeListener) {
				// not required
			}

			@Override
			public void write(int b) {
				outputStream.write(b);
			}
		};
	}

}
