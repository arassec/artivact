package com.arassec.artivact.domain.exchange.exporter;

import com.arassec.artivact.core.model.BaseTranslatableRestrictedObject;
import com.arassec.artivact.core.model.configuration.PropertiesConfiguration;
import com.arassec.artivact.core.model.exchange.ExportConfiguration;
import com.arassec.artivact.core.model.property.Property;
import com.arassec.artivact.core.model.property.PropertyCategory;
import com.arassec.artivact.domain.exchange.model.ExportContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

/**
 * Tests the {@link PropertiesExporter}.
 */
@ExtendWith(MockitoExtension.class)
class PropertiesExporterTest {

    /**
     * The exporter under test.
     */
    @InjectMocks
    private PropertiesExporter exporter;

    /**
     * The application's object mapper.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * The export context.
     */
    private ExportContext exportContext;

    /**
     * The configuration to export.
     */
    private PropertiesConfiguration propertiesConfiguration;

    /**
     * Initializes the test environment.
     */
    @BeforeEach
    void initialize() {
        exportContext = new ExportContext();
        exportContext.setExportDir(Path.of("export-dir"));
        exportContext.setExportConfiguration(ExportConfiguration.builder().build());

        propertiesConfiguration = PropertiesConfiguration.builder()
                .categories(List.of(
                                PropertyCategory.builder()
                                        .properties(List.of(
                                                Property.builder().value("prop-one").build(),
                                                Property.builder().value("prop-two").restrictions(Set.of("admin")).build(),
                                                Property.builder().value("prop-three").valueRange(List.of(
                                                                BaseTranslatableRestrictedObject.builder()
                                                                        .value("value-one")
                                                                        .restrictions(Set.of("admin"))
                                                                        .build(),
                                                                BaseTranslatableRestrictedObject.builder()
                                                                        .value("value-two")
                                                                        .build()
                                                        )
                                                ).build())
                                        ).build(),
                                PropertyCategory.builder()
                                        .restrictions(Set.of("user"))
                                        .build()
                        )
                )
                .build();
    }

    /**
     * Tests exporting the properties configuration with applied restrictions.
     */
    @Test
    @SneakyThrows
    void testExportPropertiesConfigurationWithRestrictions() {
        exportContext.getExportConfiguration().setApplyRestrictions(true);

        Path exportFile = exporter.exportPropertiesConfiguration(exportContext, propertiesConfiguration);

        assertThat(exportFile).isEqualTo(Path.of("export-dir/artivact.properties-configuration.json"));

        ArgumentCaptor<PropertiesConfiguration> argCap = ArgumentCaptor.forClass(PropertiesConfiguration.class);
        verify(objectMapper).writeValue(eq(exportFile.toFile()), argCap.capture());

        PropertiesConfiguration exportedPropertiesConfiguration = argCap.getValue();

        assertThat(exportedPropertiesConfiguration.getCategories()).hasSize(1);
        List<Property> properties = exportedPropertiesConfiguration.getCategories().getFirst().getProperties();
        assertThat(properties).hasSize(2);
        assertThat(properties.get(0).getValue()).isEqualTo("prop-one");
        assertThat(properties.get(1).getValue()).isEqualTo("prop-three");
        assertThat(properties.get(1).getValueRange()).hasSize(1);
        assertThat(properties.get(1).getValueRange().getFirst().getValue()).isEqualTo("value-two");
    }

    /**
     * Tests exporting the properties configuration without restrictions.
     */
    @Test
    @SneakyThrows
    void testExportPropertiesConfigurationWithoutRestrictions() {
        Path exportFile = exporter.exportPropertiesConfiguration(exportContext, propertiesConfiguration);

        assertThat(exportFile).isEqualTo(Path.of("export-dir/artivact.properties-configuration.json"));

        ArgumentCaptor<PropertiesConfiguration> argCap = ArgumentCaptor.forClass(PropertiesConfiguration.class);
        verify(objectMapper).writeValue(eq(exportFile.toFile()), argCap.capture());

        PropertiesConfiguration exportedPropertiesConfiguration = argCap.getValue();

        assertThat(exportedPropertiesConfiguration.getCategories()).hasSize(2);
        List<Property> properties = exportedPropertiesConfiguration.getCategories().getFirst().getProperties();
        assertThat(properties).hasSize(3);
        assertThat(properties.get(2).getValueRange()).hasSize(2);
    }

}
