package edu.java3projectpetmatchapp.service;

import edu.java3projectpetmatchapp.entity.Pet;
import edu.java3projectpetmatchapp.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class PetService {

    @Autowired
    private PetRepository petRepo;

    public Pet getPetById(long id) {
        System.out.println("Searching for pet in DB...");

        return petRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No pet found with ID: " + id));
    }
}