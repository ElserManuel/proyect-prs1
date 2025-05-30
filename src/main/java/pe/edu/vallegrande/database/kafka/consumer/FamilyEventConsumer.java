package pe.edu.vallegrande.database.kafka.consumer;

import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.KafkaListener;

import pe.edu.vallegrande.database.kafka.KafkaConsumerConfig;
import pe.edu.vallegrande.database.model.event.FamilyEvent;
import pe.edu.vallegrande.database.service.PersonService;

@Component
public class FamilyEventConsumer {

    private final PersonService personService;

    public FamilyEventConsumer(PersonService personService) {
        this.personService = personService;
    }

    /**
     * Consume eventos de la familia y ejecuta acciones correspondientes según el tipo de evento
     */
    @KafkaListener(topics = KafkaConsumerConfig.FAMILY_TOPIC, groupId = "${spring.kafka.consumer.group-id}")
    public void consumeFamilyEvent(FamilyEvent event) {
        Integer familyId = event.getFamilyId();
        String eventType = event.getEventType();

        switch (eventType) {
            case "DELETED":
                // Si una familia es eliminada lógicamente, eliminar todas las personas asociadas
                personService.deletePersonsByFamilyId(familyId).subscribe();
                break;
            case "UPDATED":
                // Si el estado de la familia es "A", reactivar todas las personas asociadas
                if ("A".equals(event.getStatus())) {
                    personService.reactivatePersonsByFamilyId(familyId).subscribe();
                }
                break;
            default:
                // Para otros tipos de eventos, no hacer nada
                break;
        }
    }
}