package pe.edu.vallegrande.database.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class FamilyServiceClient {

    private final WebClient webClient;

    @Value("${api.token}") // Asegúrate de que el token esté configurado en tu archivo de yml
    private String token;

    public FamilyServiceClient(WebClient.Builder webClientBuilder,
                               @Value("${spring.family.service-url}") String familyServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(familyServiceUrl).build();
    }

    public Mono<Boolean> familyExists(Integer familyId) {
        return webClient.get()
                .uri("/api/v1/families/{id}", familyId)
                .header("Authorization", "Bearer " + token) // Agrega el encabezado Authorization
                .retrieve()
                .bodyToMono(Object.class)
                .map(response -> true)
                .onErrorResume(error -> Mono.just(false));
    }
}
