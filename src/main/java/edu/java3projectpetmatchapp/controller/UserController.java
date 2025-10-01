package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.dto.ProfileData;
import edu.java3projectpetmatchapp.dto.RegistrationForm;
import edu.java3projectpetmatchapp.dto.UserProfileUpdateForm;
import edu.java3projectpetmatchapp.entity.User;
import edu.java3projectpetmatchapp.service.CustomUserDetailsService;
import edu.java3projectpetmatchapp.service.S3StorageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final CustomUserDetailsService userService;
    private final S3StorageService s3Service;

    // Required for 'final' fields.
    public UserController(CustomUserDetailsService userService, S3StorageService s3Service) {
        this.userService = userService;
        this.s3Service = s3Service;
    }

    // routes for everyone \\

    @GetMapping({"/", "/index", "/home"})
    public String viewIndex() {
        return "index";
    }

    @GetMapping("/login")
    public String viewLogin() {
        return "login";
    }
    //I have this here so there can be a popup or warning before logging out. Maybe it doesn't need to be its own page though
    @GetMapping("/logout")
    public String viewLogout() {
        return "logout";
    }

    // routes for USER \\

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

    // Profile editing
    @GetMapping("/profile/edit")
    public String showProfileEditForm(Model model, Principal principal) {

        User userEntity = userService.getUserEntityByEmail(principal.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found for profile edit."));

        UserProfileUpdateForm form = new UserProfileUpdateForm();
        form.setFirstName(userEntity.getFirstName());
        form.setLastName(userEntity.getLastName());
        form.setBio(userEntity.getBio());
        form.setPetPreference(userEntity.getPetPreference());

        model.addAttribute("user", userEntity);

        model.addAttribute("profileUpdateForm", form);

        model.addAttribute("currentPhotoUrl", userEntity.getUserPhotoUrl());

        return "profile_edit";
    }

    @PostMapping("/profile/edit")
    public String handleProfileUpdate(
            @ModelAttribute("profileUpdateForm") @Valid UserProfileUpdateForm form,
            BindingResult result,
            Principal principal,
            Model model) {

        Runnable reloadModel = () -> {
            User userEntity = userService.getUserEntityByEmail(principal.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found after error."));
            model.addAttribute("user", userEntity);
            model.addAttribute("currentPhotoUrl", userEntity.getUserPhotoUrl());
        };


        if (result.hasErrors()) {

            User userEntity = userService.getUserEntityByEmail(principal.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found after failed edit."));

            model.addAttribute("user", userEntity);
            model.addAttribute("currentPhotoUrl", userEntity.getUserPhotoUrl());

            return "profile_edit";
        }

        try {
            userService.updateProfile(form, principal.getName());
        } catch (Exception e) {

            // Log the exception for debugging
            System.err.println("Error updating profile: " + e.getMessage());
            result.reject("globalError", "Could not save profile due to a system error. Please try again.");
            reloadModel.run();

            return "profile_edit";
        }

        return "redirect:/profile";
    }

    // routes for STAFF \\

    @GetMapping("/staff/dashboard")
    public String staffDashboard(Model model, Principal principal) {
        String email = principal.getName();
        ProfileData profileData = userService.getProfileData(email);
        model.addAttribute("user", profileData.getUser());
        return "staff/dashboard";
    }

    // routes for ADMIN \\

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, Principal principal) {
        String email = principal.getName();
        ProfileData profileData = userService.getProfileData(email);
        model.addAttribute("user", profileData.getUser());
        return "admin/dashboard";
    }
}