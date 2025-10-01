package edu.java3projectpetmatchapp.repository;

import edu.java3projectpetmatchapp.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
}