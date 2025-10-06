package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.entity.Pet;
import edu.java3projectpetmatchapp.entity.User;
import edu.java3projectpetmatchapp.service.CustomUserDetailsService;
import edu.java3projectpetmatchapp.service.PetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
}
