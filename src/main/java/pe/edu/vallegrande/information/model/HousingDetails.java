package pe.edu.vallegrande.information.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
@Table("housing_details")
public class HousingDetails {
    @Id
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
