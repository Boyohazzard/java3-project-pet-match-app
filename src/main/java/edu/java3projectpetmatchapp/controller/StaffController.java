package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.dto.AddPetForm;
import edu.java3projectpetmatchapp.dto.UpdatePetForm;
import edu.java3projectpetmatchapp.entity.Pet;
import edu.java3projectpetmatchapp.enums.PetType;
import edu.java3projectpetmatchapp.enums.Sociability;
import edu.java3projectpetmatchapp.service.CustomUserDetailsService;
import edu.java3projectpetmatchapp.service.PetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private CustomUserDetailsService userService;
    @Autowired
    private PetService petService;

    @GetMapping("/dashboard")
    public String showStaffDashboard(Model model) {

        List<Pet> pets = petService.getAllPets();

        model.addAttribute("pets", pets);
        return "staff/dashboard";
    }

    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
    @GetMapping("/addpet")
    public String showAddPetForm(Model model) {
        model.addAttribute("addPetForm", new AddPetForm());
        model.addAttribute("petTypes", PetType.values());
        model.addAttribute("sociabilityOptions", Sociability.values());
        return "staff/addpet";
    }

    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
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

    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
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

    @PreAuthorize("hasAnyRole('STAFF','ADMIN')")
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
}