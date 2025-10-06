package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.entity.Pet;
import edu.java3projectpetmatchapp.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PetController {

    @Autowired
    PetService petService;

    @GetMapping("/pet/{id}")
    public String viewPet(@PathVariable Long id, Model model) {
        Pet pet = petService.getPetById(id);
        model.addAttribute("pet", pet);
        System.out.println("Getting pet with ID: " + id);

        return "pet/detail";
    }
}
