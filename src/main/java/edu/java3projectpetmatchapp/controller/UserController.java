package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.dto.RegistrationForm;
import edu.java3projectpetmatchapp.entity.User;
import edu.java3projectpetmatchapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    private UserService userService;

    @GetMapping({"/", "/index", "/home"})
    public String viewIndex() {
        return "index";
    }

    @GetMapping( "/staff/home")
    public String viewStaffIndex() {
        return "staff/home";
    }

    @GetMapping("/admin/home")
    public String viewAdminIndex() {
        return "admin/home";
    }

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


    @GetMapping("/login")
    public String viewLogin() {
        return "login";

    }

    @GetMapping("/profile")
    public String viewProfile() {
        return "profile";
    }

    @PostMapping("/logout")
    public String logout() {
        return "index";
    }
}


