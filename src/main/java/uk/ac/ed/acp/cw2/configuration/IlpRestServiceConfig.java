package uk.ac.ed.acp.cw2.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.URL;

@Configuration
@EnableScheduling
public class IlpRestServiceConfig {

    @Bean(name = "ilpEndpoint")
    public String ilpEndpoint() {
        // If ILP_ENDPOINT is not set, use the default ILP service URL
        return System.getenv().getOrDefault(
                "ILP_ENDPOINT",
                "https://ilp-rest-2025-bvh6e9hschfagrgy.ukwest-01.azurewebsites.net/"
        );
    }
}
