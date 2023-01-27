package com.arassec.artivact.creator.standalone.ui;

import com.arassec.artivact.creator.standalone.core.service.ConfigurationService;
import com.arassec.artivact.creator.standalone.ui.event.SceneConfig;
import com.arassec.artivact.creator.standalone.ui.event.SceneEvent;
import com.arassec.artivact.creator.standalone.ui.event.SceneEventType;
import com.arassec.artivact.creator.standalone.ui.util.MaximizedChangeListener;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

@Slf4j
@Component
public class SceneLoader implements ApplicationListener<SceneEvent> {

    private final String applicationTitle;

    private final ApplicationContext applicationContext;

    private final Image applicationLogo;

    private final ResourceBundle labelsResourceBundle;

    private final ConfigurationService configurationService;

    private final MaximizedChangeListener maximizedChangeListener;

    private final Label statusLabel;

    private Scene scene;

    private Stage stage;

    private Region opaqueLayer;

    public SceneLoader(@Value("${com.arassec.artivact.creator.standalone.title}") String applicationTitle,
                       @Value("classpath:misc/application-logo.png") Resource logoResource,
                       ApplicationContext applicationContext,
                       MessageSource messageSource,
                       ConfigurationService configurationService,
                       MaximizedChangeListener maximizedChangeListener) {
        this.applicationTitle = applicationTitle + " (v" + readVersion() + ")";
        this.applicationContext = applicationContext;
        this.labelsResourceBundle = new MessageSourceResourceBundle(messageSource, Locale.getDefault());
        this.configurationService = configurationService;
        this.maximizedChangeListener = maximizedChangeListener;
        try {
            this.applicationLogo = new Image(logoResource.getURL().openStream());
        } catch (IOException e) {
            throw new IllegalStateException("Could not load application logo!", e);
        }
        this.statusLabel = new Label();
    }

    @Override
    public void onApplicationEvent(SceneEvent event) {
        if (SceneEventType.LOAD_SCENE.equals(event.getType())) {
            loadScene(event.getConfig());
        } else if (SceneEventType.MODAL_OPENED.equals(event.getType())) {
            opaqueLayer.resizeRelocate(0, 0, scene.getWidth(), scene.getHeight());
            opaqueLayer.setVisible(true);
        } else if (SceneEventType.MODAL_CLOSED.equals(event.getType())) {
            opaqueLayer.setVisible(false);
        } else if (SceneEventType.STATUS_UPDATE.equals(event.getType())
                && StringUtils.hasText(event.getStatusKey())) {
            statusLabel.setText(labelsResourceBundle.getString(event.getStatusKey()));
        }
    }

    private void loadScene(SceneConfig sceneConfig) {

        boolean initialization = (stage == null);
        if (initialization) {
            stage = sceneConfig.getStage();
            stage.setTitle(applicationTitle);
            stage.getIcons().add(applicationLogo);
            stage.maximizedProperty().addListener(maximizedChangeListener);

            maximizedChangeListener.setStage(stage);
        }

        try {
            if (scene == null || !StringUtils.hasText(sceneConfig.getFxml())) {
                maximizedChangeListener.setInProject(false);

                scene = new Scene(createProjectChooserRoot());

                stage.setScene(scene);
                stage.setResizable(false);
                stage.setWidth(640);
                stage.setHeight(480);
            } else {
                maximizedChangeListener.setInProject(true);

                opaqueLayer = new Region();
                opaqueLayer.setStyle("-fx-background-color: #00000055;");
                opaqueLayer.setVisible(false);

                scene.setRoot(new StackPane(createSceneRoot(sceneConfig.getFxml()), opaqueLayer));

                stage.setResizable(true);
                if (!configurationService.isMaximized()) {
                    stage.setWidth(1024);
                    stage.setHeight(768);
                } else {
                    stage.setMaximized(true);
                }
            }

            scene.getRoot().requestFocus();
        } catch (IOException e) {
            throw new IllegalStateException("Could not load FXML scene!", e);
        }

        if (initialization) {
            stage.show();
        }
    }

    private VBox createProjectChooserRoot() throws IOException {
        var url = new ClassPathResource(SceneEvent.PROJECT_CHOOSER_FXML).getURL();

        var fxmlLoader = new FXMLLoader(url);
        fxmlLoader.setControllerFactory(applicationContext::getBean);
        fxmlLoader.setResources(labelsResourceBundle);

        Parent sceneRoot = fxmlLoader.load();

        return new VBox(sceneRoot);
    }

    private VBox createSceneRoot(String fxml) throws IOException {
        var url = new ClassPathResource(fxml).getURL();

        var fxmlLoader = new FXMLLoader(url);
        fxmlLoader.setControllerFactory(applicationContext::getBean);
        fxmlLoader.setResources(labelsResourceBundle);

        Parent sceneRoot = fxmlLoader.load();

        VBox.setVgrow(sceneRoot, Priority.ALWAYS);

        VBox result = new VBox();
        result.getChildren().add(sceneRoot);

        Pane footerPane = new Pane();
        footerPane.setMinHeight(20);
        footerPane.setMaxHeight(20);
        VBox.setMargin(footerPane, new Insets(0, 0, 0, 5));
        footerPane.getChildren().add(statusLabel);
        result.getChildren().add(footerPane);

        statusLabel.setText(labelsResourceBundle.getString("status.project-opened"));

        return result;
    }

    private String readVersion() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream("META-INF/MANIFEST.MF")) {
            return new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream))).lines()
                    .filter(line -> line.startsWith("Implementation-Version:"))
                    .map(line -> line.replace("Implementation-Version: ", ""))
                    .findFirst()
                    .orElse("0");
        } catch (IOException e) {
            log.error("Could not read MANIFEST.MF to determine application version!", e);
        }
        return "0";
    }
}
