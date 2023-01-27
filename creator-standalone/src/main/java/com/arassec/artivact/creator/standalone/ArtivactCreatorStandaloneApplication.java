package com.arassec.artivact.creator.standalone;

import javafx.application.Application;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ArtivactCreatorStandaloneApplication {

    public static void main(String[] args) {
        Application.launch(ArtivactCreatorStandaloneFxApplication.class, args);
    }

}

