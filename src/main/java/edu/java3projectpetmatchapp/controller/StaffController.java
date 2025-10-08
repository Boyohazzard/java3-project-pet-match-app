package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.dto.AddPetForm;
import edu.java3projectpetmatchapp.dto.UpdatePetForm;
import edu.java3projectpetmatchapp.entity.Application;
import edu.java3projectpetmatchapp.entity.Pet;
import edu.java3projectpetmatchapp.enums.PetType;
import edu.java3projectpetmatchapp.enums.Sociability;
import edu.java3projectpetmatchapp.service.ApplicationService;
import edu.java3projectpetmatchapp.service.CustomUserDetailsService;
import edu.java3projectpetmatchapp.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/staff")
public class StaffController {

    private final CustomUserDetailsService userService;
    private final PetService petService;
    private final ApplicationService appService;
    private final edu.java3projectpetmatchapp.service.S3StorageService s3Service;

    @GetMapping("/dashboard")
    public String showStaffDashboard(@RequestParam(defaultValue = "id") String sort,
                                     @RequestParam(defaultValue = "asc") String dir,
                                     Model model) {

        Sort.Direction direction = dir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        List<Pet> pets = petService.getAllPetsSorted(sort, direction);

        model.addAttribute("pets", pets);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        return "staff/dashboard";
    }

    @GetMapping("/applications")
    public String showStaffApplications(@RequestParam(defaultValue = "id") String sort,
                                        @RequestParam(defaultValue = "asc") String dir,
                                        Model model) {

        Sort.Direction direction = dir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        List<Application> applications = appService.getAllApplicationsSorted(sort, direction);

        model.addAttribute("applications", applications);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        return "staff/application_list";
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/applications/{id}")
    public String viewApplication(@PathVariable Long id, Model model) {
        Application application = appService.getAppById(id);
        model.addAttribute("application", application);
        model.addAttribute("pet", application.getPet());
        model.addAttribute("user", application.getUser());
        return "staff/application";
    }

    @PreAuthorize("hasRole('STAFF')")
    @GetMapping("/addpet")
    public String showAddPetForm(Model model) {
        model.addAttribute("addPetForm", new AddPetForm());
        model.addAttribute("petTypes", PetType.values());
        model.addAttribute("sociabilityOptions", Sociability.values());
        model.addAttribute("defaultPetPhotoUrl", s3Service.getDefaultPetPhotoUrl());
        return "staff/addpet";
    }

    @CacheEvict(value = "allPets", allEntries = true)
    @PreAuthorize("hasAnyRole('STAFF')")
    @PostMapping("/addpet")
    public String addPet(
            @ModelAttribute("addPetForm") @Valid AddPetForm form,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("petTypes", PetType.values());
            model.addAttribute("sociabilityOptions", Sociability.values());
            return "staff/addpet";
        }
        try {
            petService.registerNewPet(form);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("petTypes", PetType.values());
            model.addAttribute("sociabilityOptions", Sociability.values());
            model.addAttribute("error", "An error occurred while saving the pet.");
            return "staff/addpet";
        }
        return "redirect:dashboard";
    }

    @PreAuthorize("hasAnyRole('STAFF')")
    @GetMapping("/updatepet")
    public String showUpdatePetPage(@RequestParam("id") Long id, Model model) {
        Pet pet = petService.getPetById(id);
        UpdatePetForm form = petService.convertPetToForm(pet);

        model.addAttribute("updatePetForm", form);
        model.addAttribute("currentPetPhotoUrl", pet.getPetPhotoUrl());
        model.addAttribute("petTypes", PetType.values());
        model.addAttribute("sociabilityOptions", Sociability.values());
        return "staff/updatepet";
    }

    @CacheEvict(value = "allPets", allEntries = true)
    @PreAuthorize("hasAnyRole('STAFF')")
    @PostMapping("/updatepet")
    public String UpdatePet(
            @ModelAttribute("updatePetForm") @Valid UpdatePetForm form,
            BindingResult result,
            Model model) {

        Runnable reloadPhotoUrl = () -> {
            try {
                Pet pet = petService.getPetById(form.getId());
                model.addAttribute("currentPetPhotoUrl", pet.getPetPhotoUrl());
            } catch (NoSuchElementException ignored) {
            }
        };

        if (result.hasErrors()) {
            model.addAttribute("petTypes", PetType.values());
            model.addAttribute("sociabilityOptions", Sociability.values());
            reloadPhotoUrl.run();
            return "staff/updatepet";
        }
        try {
            Pet petToUpdate = petService.getPetById(form.getId());
            petService.updatePet(form, petToUpdate);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("petTypes", PetType.values());
            model.addAttribute("sociabilityOptions", Sociability.values());
            reloadPhotoUrl.run();
            model.addAttribute("error", "An error occurred while saving the pet.");
            return "staff/updatepet";
        }
        return "redirect:/staff/dashboard";
    }

    @CacheEvict(value = "allPets", allEntries = true)
    @PreAuthorize("hasAnyRole('STAFF')")
    @PostMapping("/pet/{id}/delete")
    public String deletePet(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            petService.deletePet(id);
            redirectAttributes.addFlashAttribute("successMessage", "Pet deleted successfully.");
        } catch (UsernameNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pet not found.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting pet and associated data: " + e.getMessage());
        }
        return "redirect:/staff/dashboard";
    }
}