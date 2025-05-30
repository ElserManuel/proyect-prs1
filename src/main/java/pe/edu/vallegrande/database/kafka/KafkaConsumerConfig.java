package pe.edu.vallegrande.database.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KafkaConsumerConfig {

    public static final String FAMILY_TOPIC = "family-events";

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}