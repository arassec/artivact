package com.arassec.artivact.vault.standalone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ArtivactVaultStandaloneApplication {

    /**
     * Starts the igor application.
     *
     * @param args Command-line arguments.
     */
    @SuppressWarnings("squid:S4823") // Spring will take care of command line parameters...
    public static void main(String[] args) {
        SpringApplication.run(ArtivactVaultStandaloneApplication.class, args);
    }

}
