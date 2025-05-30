package pe.edu.vallegrande.database.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class FamilyDTO {
    private Integer id;
    private String lastName;
    private String direction;
    private String reasibAdmission;
    private Integer numberMembers;
    private Integer numberChildren;
    private String familyType;
    private String socialProblems;
    private String weeklyFrequency;
    private String feedingType;
    private String safeType;
    private String familyDisease;
    private String treatment;
    private String diseaseHistory;
    private String medicalExam;
    private String tenure;
    private String status;
    private LocalDateTime created;
    private LocalDateTime deleted;

    // These will be populated from the other microservice
    private BasicServiceDTO basicService;
    private HousingDetailsDTO housingDetails;
}
