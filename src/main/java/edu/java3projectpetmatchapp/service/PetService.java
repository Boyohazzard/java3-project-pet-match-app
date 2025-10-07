package edu.java3projectpetmatchapp.service;

import edu.java3projectpetmatchapp.dto.AddPetForm;
import edu.java3projectpetmatchapp.entity.Application;
import edu.java3projectpetmatchapp.entity.Pet;
import edu.java3projectpetmatchapp.repository.ApplicationRepository;
import edu.java3projectpetmatchapp.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PetService {

    @Autowired
    private PetRepository petRepo;
    @Autowired
    private ApplicationRepository appRepo;

    public Pet getPetById(long id) {
        System.out.println("Searching for pet in DB...");

        return petRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No pet found with ID: " + id));
    }

    public List<Pet> getAllPets() { return petRepo.findAll(); }

    public List<Application> getAllPetApplications(Pet pet) {
        return appRepo.findByPet(pet);
    }


    public void registerNewPet(AddPetForm form) {
        Pet pet = new Pet();
        pet.setPetName(form.getPetName());
        pet.setPetType(form.getPetType());
        pet.setPetBreed(form.getPetBreed());
        pet.setSociability(form.getSociability());
        pet.setSpecialNeeds(form.getSpecialNeeds());
        pet.setHealthIssues(form.getHealthIssues());
        pet.setAbout(form.getAbout());
        pet.setAge(form.getAge());
        if (form.getDatePetSheltered() == null) {
            pet.setDatePetSheltered(LocalDate.now());
        } else {
            pet.setDatePetSheltered(form.getDatePetSheltered());
        }

        //pet.setPetPhotoUrl(s3Service.getDefaultProfilePhotoUrl());
        System.out.println("Saving pet: " + pet);

        petRepo.save(pet);
    }
}