package pe.edu.vallegrande.database.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import pe.edu.vallegrande.database.dto.BasicServiceDTO;
import pe.edu.vallegrande.database.dto.HousingDetailsDTO;
import reactor.core.publisher.Mono;

@Component
public class HousingServiceClient {

    private final WebClient webClient;

    public HousingServiceClient(@Value("${housing.service.url}") String housingServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(housingServiceUrl)
                .build();
    }

    public Mono<BasicServiceDTO> getBasicServiceById(Integer serviceId) {
        if (serviceId == null) {
            return Mono.empty();
        }
        
        return webClient.get()
                .uri("/api/v1/services/{id}", serviceId)
                .retrieve()
                .bodyToMono(BasicServiceDTO.class)
                .onErrorResume(e -> Mono.empty()); // Log error y retorna vacío
    }

    public Mono<HousingDetailsDTO> getHousingDetailsById(Integer housingId) {
        if (housingId == null) {
            return Mono.empty();
        }
        
        return webClient.get()
                .uri("/api/v1/housing/{id}", housingId)
                .retrieve()
                .bodyToMono(HousingDetailsDTO.class)
                .onErrorResume(e -> Mono.empty()); // Log error y retorna vacío
    }

    public Mono<BasicServiceDTO> createBasicService(BasicServiceDTO basicServiceDTO) {
        return webClient.post()
                .uri("/api/v1/services")
                .bodyValue(basicServiceDTO)
                .retrieve()
                .bodyToMono(BasicServiceDTO.class);
    }

    public Mono<HousingDetailsDTO> createHousingDetails(HousingDetailsDTO housingDetailsDTO) {
        return webClient.post()
                .uri("/api/v1/housing")
                .bodyValue(housingDetailsDTO)
                .retrieve()
                .bodyToMono(HousingDetailsDTO.class);
    }

    public Mono<BasicServiceDTO> updateBasicService(Integer serviceId, BasicServiceDTO basicServiceDTO) {
        return webClient.put()
                .uri("/api/v1/services/{id}", serviceId)
                .bodyValue(basicServiceDTO)
                .retrieve()
                .bodyToMono(BasicServiceDTO.class);
    }

    public Mono<HousingDetailsDTO> updateHousingDetails(Integer housingId, HousingDetailsDTO housingDetailsDTO) {
        return webClient.put()
                .uri("/api/v1/housing/{id}", housingId)
                .bodyValue(housingDetailsDTO)
                .retrieve()
                .bodyToMono(HousingDetailsDTO.class);
    }
}
