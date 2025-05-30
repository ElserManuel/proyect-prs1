package pe.edu.vallegrande.database.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import pe.edu.vallegrande.database.model.Family;
import pe.edu.vallegrande.database.model.event.FamilyEvent;

@Service
public class FamilyEventService {

    private static final String TOPIC_NAME = "family-events";
    private final KafkaTemplate<String, FamilyEvent> kafkaTemplate;

    @Autowired
    public FamilyEventService(KafkaTemplate<String, FamilyEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishFamilyEvent(Family family, String eventType) {
        FamilyEvent familyEvent = new FamilyEvent(
                family.getId(),
                eventType,
                family.getLastName(),
                family.getStatus(),
                family.getServiceId(),
                family.getHousingId()
        );
        kafkaTemplate.send(TOPIC_NAME, String.valueOf(family.getId()), familyEvent);
    }
}