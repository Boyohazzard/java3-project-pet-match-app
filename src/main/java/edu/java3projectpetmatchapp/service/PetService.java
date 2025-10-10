package edu.java3projectpetmatchapp.service;

import edu.java3projectpetmatchapp.dto.AddPetForm;
import edu.java3projectpetmatchapp.dto.UpdatePetForm;
import edu.java3projectpetmatchapp.entity.Application;
import edu.java3projectpetmatchapp.entity.Pet;
import edu.java3projectpetmatchapp.enums.PetType;
import edu.java3projectpetmatchapp.repository.ApplicationRepository;
import edu.java3projectpetmatchapp.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class PetService {

    private final PetRepository petRepo;
    private final ApplicationRepository appRepo;
    private final PetCacheService petCacheService;
    private final S3StorageService s3Service;

    public Pet getPetById(long id) {
        System.out.println("Searching for pet in DB...");

        return petRepo.findPetById(id)
                .orElseThrow(() -> new NoSuchElementException("No pet found with ID: " + id));
    }

    public List<Pet> getAllPets() {
        return petRepo.findAll();
    }

    public List<Pet> getAllPetsSorted(String sortField, Sort.Direction direction) {
        List<Pet> pets = new ArrayList<>(petCacheService.getAllPets());
        if (!isValidSortField(sortField)) sortField = "id";
        pets.sort(getComparator(sortField, direction));
        return pets;
    }

    private boolean isValidSortField(String field) {
        return List.of("id", "petName", "datePetSheltered", "availability").contains(field);
    }

    private Comparator<Pet> getComparator(String field, Sort.Direction direction) {
        Comparator<Pet> comparator = switch (field) {
            case "petName" -> Comparator.comparing(Pet::getPetName, Comparator.nullsLast(String::compareToIgnoreCase));
            case "datePetSheltered" -> Comparator.comparing(Pet::getDatePetSheltered, Comparator.nullsLast(Comparator.naturalOrder()));
            case "availability" -> Comparator.comparing(Pet::getAvailability);
            default -> Comparator.comparing(Pet::getId);
        };

        return direction == Sort.Direction.DESC ? comparator.reversed() : comparator;
    }

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

        if (form.getNewPhoto() != null && !form.getNewPhoto().isEmpty()) {
            try {
                String newUrl = s3Service.uploadPetPhoto(form.getNewPhoto());
                pet.setPetPhotoUrl(newUrl);
            } catch (java.io.IOException e) {
                // Fallback to default if upload fails
                pet.setPetPhotoUrl(s3Service.getDefaultPetPhotoUrl());
            }
        } else {
            pet.setPetPhotoUrl(s3Service.getDefaultPetPhotoUrl());
        }

        petRepo.save(pet);
    }

    public void deletePet(Long id) {
        Pet pet = petRepo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Pet not found."));

        // Remove associated applications
        appRepo.deleteByPet(pet);

        // Delete photo if not default
        String currentPhotoUrl = pet.getPetPhotoUrl();
        if (currentPhotoUrl != null && !currentPhotoUrl.isEmpty() &&
                !currentPhotoUrl.equals(s3Service.getDefaultPetPhotoUrl())) {
            s3Service.deleteFileFromUrl(currentPhotoUrl);
        }

        petRepo.delete(pet);
    }

    public UpdatePetForm convertPetToForm(Pet pet) {
        UpdatePetForm form = new UpdatePetForm();
        form.setId(pet.getId());
        form.setPetName(pet.getPetName());
        form.setPetType(pet.getPetType());
        form.setPetBreed(pet.getPetBreed());
        form.setSociability(pet.getSociability());
        form.setSpecialNeeds(pet.getSpecialNeeds());
        form.setHealthIssues(pet.getHealthIssues());
        form.setAbout(pet.getAbout());
        form.setAge(pet.getAge());
        form.setDatePetSheltered(pet.getDatePetSheltered());
        form.setNewPhoto(null);
        form.setDeletePhoto(false);
        return form;
    }

    public void updatePet(UpdatePetForm form, Pet pet) throws Exception {
        pet.setPetName(form.getPetName());
        pet.setPetType(form.getPetType());
        pet.setPetBreed(form.getPetBreed());
        pet.setSociability(form.getSociability());
        pet.setSpecialNeeds(form.getSpecialNeeds());
        pet.setHealthIssues(form.getHealthIssues());
        pet.setAbout(form.getAbout());
        pet.setAge(form.getAge());
        pet.setDatePetSheltered(form.getDatePetSheltered());

        String currentPhotoUrl = pet.getPetPhotoUrl();
        String defaultUrl = s3Service.getDefaultPetPhotoUrl();

        // Photo Deletion or Replacement
        if (form.isDeletePhoto()) {
            if (currentPhotoUrl != null && !currentPhotoUrl.equals(defaultUrl)) {
                s3Service.deleteFileFromUrl(currentPhotoUrl);
            }
            pet.setPetPhotoUrl(defaultUrl);

        } else if (form.getNewPhoto() != null && !form.getNewPhoto().isEmpty()) {

            if (currentPhotoUrl != null && !currentPhotoUrl.equals(defaultUrl)) {
                s3Service.deleteFileFromUrl(currentPhotoUrl);
            }

            String newUrl = s3Service.uploadPetPhoto(form.getNewPhoto());
            pet.setPetPhotoUrl(newUrl);
        }
        petRepo.save(pet);
    }

    public List<Pet> getFilteredPets(String type, String age, String datePetSheltered) {
        List<Pet> pets = getAllPets();

        if (type != null && !type.isBlank() && !"ALL".equalsIgnoreCase(type)) {
            try {
                PetType typeEnum = PetType.valueOf(type.toUpperCase());
                pets = pets.stream()
                        .filter(p -> p.getPetType() == typeEnum)
                        .toList();
            } catch (IllegalArgumentException e) {
                return List.of();
            }
        }

        if (age != null && !age.isBlank()) {
            pets = switch (age.toLowerCase()) {
                case "younger" -> pets.stream()
                        .filter(p -> p.getAge() <= 2)
                        .toList();
                case "older" -> pets.stream()
                        .filter(p -> p.getAge() >= 6)
                        .toList();
                case "adult" -> pets.stream()
                        .filter(p -> p.getAge() > 2 && p.getAge() < 7)
                        .toList();
                default -> pets;
            };
        }

        if (datePetSheltered != null && !datePetSheltered.isBlank()) {
            Comparator<Pet> comparator = Comparator.comparing(Pet::getDatePetSheltered, Comparator.nullsLast(Comparator.naturalOrder()));

            if (datePetSheltered.equalsIgnoreCase("asc")) {
                comparator = comparator.reversed();
            }

            pets = pets.stream()
                    .sorted(comparator)
                    .toList();
        }
        return pets;
    }
}