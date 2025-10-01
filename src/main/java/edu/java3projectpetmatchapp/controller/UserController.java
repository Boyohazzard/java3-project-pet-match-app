package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.dto.ProfileData;
import edu.java3projectpetmatchapp.dto.RegistrationForm;
import edu.java3projectpetmatchapp.entity.User;
import edu.java3projectpetmatchapp.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class UserController {

    @Autowired
    private CustomUserDetailsService userService;

    // routes for everyone
    @GetMapping({"/", "/index", "/home"})
    public String viewIndex() {
        return "index";
    }

    @GetMapping("/login")
    public String viewLogin() {
        return "login";
    }

    @GetMapping("/logout")
    public String viewLogout() {
        return "logout";
    }

    // routes for USER
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("registrationForm") @Valid RegistrationForm form,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerNewUser(form);
        } catch (IllegalArgumentException e) {
            result.rejectValue("confirmPassword", "error.confirmPassword", e.getMessage());
            return "register";
        }

        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        ProfileData profileData = userService.getProfileData(principal.getName());
        model.addAttribute("user", profileData.getUser());
        model.addAttribute("applications", profileData.getApplications());
        return "profile";
    }


}