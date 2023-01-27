package com.arassec.artivact.creator.standalone;

import com.arassec.artivact.creator.standalone.ui.event.SceneConfig;
import com.arassec.artivact.creator.standalone.ui.event.SceneEvent;
import com.arassec.artivact.creator.standalone.ui.event.SceneEventType;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

public class ArtivactCreatorStandaloneFxApplication extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        ApplicationContextInitializer<GenericApplicationContext> initializer =
                applicationContext -> {
                    applicationContext.registerBean(Application.class, () -> ArtivactCreatorStandaloneFxApplication.this);
                    applicationContext.registerBean(Parameters.class, this::getParameters);
                    applicationContext.registerBean(HostServices.class, this::getHostServices);
                };

        this.context = new SpringApplicationBuilder()
                .sources(ArtivactCreatorStandaloneApplication.class)
                .initializers(initializer)
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.context.publishEvent(new SceneEvent(SceneEventType.LOAD_SCENE, new SceneConfig(null, primaryStage), null));
    }

    @Override
    public void stop() {
        this.context.close();
        Platform.exit();
    }

}
