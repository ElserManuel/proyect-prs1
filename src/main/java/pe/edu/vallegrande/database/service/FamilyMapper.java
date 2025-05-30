package pe.edu.vallegrande.database.service;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.database.dto.FamilyDTO;
import pe.edu.vallegrande.database.model.Family;

@Component
public class FamilyMapper {

    /**
     * Convierte una entidad Family a un DTO
     */
    public FamilyDTO toDTO(Family family) {
        if (family == null) {
            return null;
        }
        
        FamilyDTO dto = new FamilyDTO();
        dto.setId(family.getId());
        dto.setLastName(family.getLastName());
        dto.setDirection(family.getDirection());
        dto.setReasibAdmission(family.getReasibAdmission());
        dto.setNumberMembers(family.getNumberMembers());
        dto.setNumberChildren(family.getNumberChildren());
        dto.setFamilyType(family.getFamilyType());
        dto.setSocialProblems(family.getSocialProblems());
        dto.setWeeklyFrequency(family.getWeeklyFrequency());
        dto.setFeedingType(family.getFeedingType());
        dto.setSafeType(family.getSafeType());
        dto.setFamilyDisease(family.getFamilyDisease());
        dto.setTreatment(family.getTreatment());
        dto.setDiseaseHistory(family.getDiseaseHistory());
        dto.setMedicalExam(family.getMedicalExam());
        dto.setTenure(family.getTenure());
        dto.setStatus(family.getStatus());
        dto.setCreated(family.getCreated());
        dto.setDeleted(family.getDeleted());
        return dto;
    }

    /**
     * Convierte un DTO a una entidad Family
     */
    public Family toEntity(FamilyDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Family family = new Family();
        updateEntityFromDTO(family, dto);
        return family;
    }

    /**
     * Actualiza una entidad existente con datos del DTO
     */
    public void updateEntityFromDTO(Family family, FamilyDTO dto) {
        if (family == null || dto == null) {
            return;
        }
        
        family.setLastName(dto.getLastName());
        family.setDirection(dto.getDirection());
        family.setReasibAdmission(dto.getReasibAdmission());
        family.setNumberMembers(dto.getNumberMembers());
        family.setNumberChildren(dto.getNumberChildren());
        family.setFamilyType(dto.getFamilyType());
        family.setSocialProblems(dto.getSocialProblems());
        family.setWeeklyFrequency(dto.getWeeklyFrequency());
        family.setFeedingType(dto.getFeedingType());
        family.setSafeType(dto.getSafeType());
        family.setFamilyDisease(dto.getFamilyDisease());
        family.setTreatment(dto.getTreatment());
        family.setDiseaseHistory(dto.getDiseaseHistory());
        family.setMedicalExam(dto.getMedicalExam());
        family.setTenure(dto.getTenure());
        family.setStatus(dto.getStatus());
        // We don't update created and deleted timestamps here
    }
}
