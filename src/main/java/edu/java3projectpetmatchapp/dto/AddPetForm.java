package edu.java3projectpetmatchapp.dto;

import edu.java3projectpetmatchapp.enums.PetType;
import edu.java3projectpetmatchapp.enums.Sociability;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Date;

@Data
public class AddPetForm {

    @NotBlank
    @Size(min = 2, max = 40,
            message="Name must be between 2 and 40 characters")
    private String petName;

    @NotNull(message = "Please select a pet type")
    private PetType petType;

    private String petBreed;

    @NotNull(message = "Please select sociability level")
    private Sociability sociability;

    private String specialNeeds;

    private String healthIssues;

    @Size(max = 1000,
            message="About must be less than 1000 characters")
    private String about;

    @Min(0)
    @Max(100)
    private int age;

    private Date datePetSheltered;
}
