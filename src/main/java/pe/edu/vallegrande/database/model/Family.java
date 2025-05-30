package pe.edu.vallegrande.database.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Data;

@Data
@Table("family")
public class Family {
    @Id
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
    private Integer serviceId;
    private Integer housingId;
    private String status;

    @Column("created_at")
    private LocalDateTime created;

    @Column("deleted_at")
    private LocalDateTime deleted;
}