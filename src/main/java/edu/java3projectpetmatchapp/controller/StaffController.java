package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.dto.AddPetForm;
import edu.java3projectpetmatchapp.entity.Pet;
import edu.java3projectpetmatchapp.enums.PetType;
import edu.java3projectpetmatchapp.enums.Sociability;
import edu.java3projectpetmatchapp.service.CustomUserDetailsService;
import edu.java3projectpetmatchapp.service.PetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

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

    @GetMapping("/addPet")
    public String showAddPetForm(Model model) {
        model.addAttribute("addPetForm", new AddPetForm());
        model.addAttribute("petTypes", PetType.values());
        model.addAttribute("sociabilityOptions", Sociability.values());
        return "staff/addPet";
    }

    @PostMapping("/addPet")
    public String addPet(
            @ModelAttribute("addPetForm") @Valid AddPetForm form,
            BindingResult result) {
        if (result.hasErrors()) {
            return "staff/addPet";
        }
        return "redirect:/login";
    }
}
