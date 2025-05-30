package pe.edu.vallegrande.database.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class BasicServiceDTO {
    private Integer serviceId;
    private String waterService;
    private String servDrain;
    private String servLight;
    private String servCable;
    private String servGas;
    private String area;
    private String referenceLocation;
    private String residue;
    private String publicLighting;
    private String security;
    private String material;
    private String feeding;
    private String economic;
    private String spiritual;
    private String socialCompany;
    private String guideTip;
}
