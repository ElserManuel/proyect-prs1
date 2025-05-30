package pe.edu.vallegrande.database.model;

import java.time.LocalDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("person")
public class Person {
    
    @Id
    private Integer idPerson;
    private String name;
    private String surname;
    private Integer age;
    private LocalDate birthdate;
    private String typeDocument;
    private String documentNumber;
    private String typeKinship;
    private String sponsored;
    private String state;
    private Integer familyIdFamily;

}
