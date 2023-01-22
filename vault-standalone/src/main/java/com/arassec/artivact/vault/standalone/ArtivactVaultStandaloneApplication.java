package com.arassec.artivact.vault.standalone;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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

    /**
     * Creates Open-API information.
     *
     * @return An {@link OpenAPI} specification.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Artivact-Vault Application API")
                        .license(new License().name("MIT").url("https://github.com/arassec/igor/blob/master/LICENSE")));
    }

}
