package com.arassec.artivact.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The Artivact server application.
 */
@SpringBootApplication
public class ArtivactServerApplication {

    /**
     * Starts the application.
     *
     * @param args Command-line arguments.
     */
    @SuppressWarnings("squid:S4823") // Spring will take care of command line parameters...
    public static void main(String[] args) {
        SpringApplication.run(ArtivactServerApplication.class, args);
    }

}
