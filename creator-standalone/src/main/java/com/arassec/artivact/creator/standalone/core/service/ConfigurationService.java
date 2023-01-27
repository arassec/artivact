package com.arassec.artivact.creator.standalone.core.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfigurationService {

    private static final String APP_PROPERTIES_FILE = "application.properties";

    private static final String KEY_RECENT_PROJECTS = "recent-projects";

    private static final String KEY_MAXIMIZED = "application.maximized";

    private static final String FALLBACK = "fallback";

    private Properties appProperties;

    @PostConstruct
    public void initialize() {
        var propFilePath = Path.of(APP_PROPERTIES_FILE);

        appProperties = new Properties() {
            @Override
            public Set<Map.Entry<Object, Object>> entrySet() {
                return Collections.synchronizedSet(
                        super.entrySet()
                                .stream()
                                .sorted(Comparator.comparing(e -> e.getKey().toString()))
                                .collect(Collectors.toCollection(LinkedHashSet::new)));
            }
        };

        if (!Files.exists(propFilePath)) {
            appProperties.setProperty("adapter.implementation.background", FALLBACK);
            appProperties.setProperty("adapter.implementation.background.executable", "");
            appProperties.setProperty("adapter.implementation.camera", FALLBACK);
            appProperties.setProperty("adapter.implementation.camera.executable", "");
            appProperties.setProperty("adapter.implementation.turntable", FALLBACK);
            appProperties.setProperty("adapter.implementation.model-creator", FALLBACK);
            appProperties.setProperty("adapter.implementation.model-creator.executable", "");
            appProperties.setProperty("adapter.implementation.model-editor", FALLBACK);
            appProperties.setProperty("adapter.implementation.model-editor.executable", "");

            appProperties.setProperty(KEY_RECENT_PROJECTS, "");
            appProperties.setProperty(KEY_MAXIMIZED, Boolean.FALSE.toString());

            writePropertiesFile();
        } else {
            try (InputStream input = new FileInputStream(propFilePath.toAbsolutePath().toString())) {
                // load a properties file
                appProperties.load(input);
            } catch (IOException e) {
                throw new IllegalStateException("Could not read properties file!", e);
            }
        }
    }

    @SuppressWarnings("java:S6204") // Stream.toList() results in an unmodifiable list...
    public List<String> getRecentProjects() {
        return Arrays.stream(appProperties.getProperty(KEY_RECENT_PROJECTS).split(","))
                .filter(Objects::nonNull)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    public void saveRecentProjects(List<String> recentProjects) {
        appProperties.setProperty(KEY_RECENT_PROJECTS, String.join(",", recentProjects));
        writePropertiesFile();
    }

    public void addRecentProject(String project) {
        List<String> recentProjects = getRecentProjects();
        if (!recentProjects.contains(project)) {
            recentProjects.add(project);
            saveRecentProjects(recentProjects);
        }
    }

    public void removeRecentProject(String project) {
        List<String> recentProjects = getRecentProjects();
        recentProjects.remove(project);
        saveRecentProjects(recentProjects);
    }

    public void saveMaximized(boolean fullscreen) {
        appProperties.setProperty(KEY_MAXIMIZED, String.valueOf(fullscreen));
        writePropertiesFile();
    }

    public boolean isMaximized() {
        return Boolean.parseBoolean(appProperties.getProperty(KEY_MAXIMIZED));
    }

    private void writePropertiesFile() {
        var propFilePath = Path.of(APP_PROPERTIES_FILE);
        try (OutputStream output = new FileOutputStream(propFilePath.toAbsolutePath().toString())) {
            appProperties.store(output, null);
        } catch (IOException e) {
            throw new IllegalStateException("Could not write initial properties file!", e);
        }
    }

}
