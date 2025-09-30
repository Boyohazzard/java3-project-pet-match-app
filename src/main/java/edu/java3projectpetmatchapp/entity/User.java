package edu.java3projectpetmatchapp.entity;

import edu.java3projectpetmatchapp.enums.PetPreference;
import edu.java3projectpetmatchapp.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="user_table")
@Entity
public class User {

    @PrePersist
    public void assignDefaults() {
        if (this.role == null) {
            this.role = Role.USER;
        }
        if (this.petPreference == null) {
            this.petPreference = PetPreference.NONE;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private long id;

    @NotBlank
    @Size(min = 2, max = 40,
            message="Name must be between 1 and 40 characters")
    @Column(name = "first_name", nullable = false, length = 40)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 40,
            message="Name must be between 1 and 40 characters")
    @Column(name = "last_name", nullable = false, length = 40)
    private String lastName;

    @NotBlank
    @Email
    @Column(name = "email", nullable = false, length = 360, unique = true)
    private String email;

    @NotBlank
    @Column(length = 80, name = "password", nullable = false)
    private String password;

    @Transient
    private String confirmPassword;

    @Column(length = 500, name = "user_bio")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_preference", length = 20)
    private PetPreference petPreference;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", length = 10, nullable = false)
    private Role role;
}