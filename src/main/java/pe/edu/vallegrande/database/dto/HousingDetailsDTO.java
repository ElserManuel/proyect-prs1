package pe.edu.vallegrande.database.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class HousingDetailsDTO {
    private Integer id;
    private String typeOfHousing;
    private String housingMaterial;
    private String housingSecurity;
    private Integer homeEnvironment;
    private Integer bedroomNumber;
    private String habitability;
    private Integer numberRooms;
    private Integer numberOfBedrooms;
    private String habitabilityBuilding;
}
