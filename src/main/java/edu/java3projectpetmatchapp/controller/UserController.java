package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.dto.PetApplicationForm;
import edu.java3projectpetmatchapp.dto.ProfileData;
import edu.java3projectpetmatchapp.dto.RegistrationForm;
import edu.java3projectpetmatchapp.dto.UserProfileUpdateForm;
import edu.java3projectpetmatchapp.entity.Pet;
import edu.java3projectpetmatchapp.entity.User;
import edu.java3projectpetmatchapp.enums.*;
import edu.java3projectpetmatchapp.service.ApplicationService;
import edu.java3projectpetmatchapp.service.CustomUserDetailsService;
import edu.java3projectpetmatchapp.service.PetService;
import edu.java3projectpetmatchapp.service.S3StorageService;
import jakarta.validation.Valid;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.Optional;

@Controller
public class UserController {

    private final CustomUserDetailsService userService;
    private final S3StorageService s3Service;
    private final PetService petService;
    private final ApplicationService appService;

    // Required for 'final' fields.
    public UserController(CustomUserDetailsService userService,
                          S3StorageService s3Service,
                          PetService petService,
                          ApplicationService appService) {
        this.userService = userService;
        this.s3Service = s3Service;
        this.petService = petService;
        this.appService = appService;
    }

    // routes for everyone \\

    @GetMapping({"/", "/index", "/home"})
    public String viewIndex(Model model) {
        model.addAttribute("pets", petService.getAllPets());
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

    @CacheEvict(value = "allUsers", allEntries = true)
    @PostMapping("/register")
    public String registerUser(
            @ModelAttribute("registrationForm") @Valid RegistrationForm form,
            BindingResult result) {

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

    @GetMapping({"/profile", "/profile/{id}"})
    public String showProfile(@PathVariable Optional<Long> id, Model model, Principal principal) {

        String targetEmail;

        if (id.isPresent()) {
            // Case 1: Admin is viewing another user's profile by ID.

            User targetUser = userService.getUserEntityById(id.get())
                    .orElseThrow(() -> new UsernameNotFoundException("Target user not found."));
            targetEmail = targetUser.getEmail();
        } else {
            // Case 2: Standard user or admin is viewing their own profile (self-view).
            targetEmail = principal.getName();
        }

        ProfileData profileData = userService.getProfileData(targetEmail);
        model.addAttribute("user", profileData.getUser());
        model.addAttribute("applications", profileData.getApplications());

        return "profile";
    }

    @GetMapping("/pet/{id}")
    public String viewPet(@PathVariable Long id, Model model) {
        Pet pet = petService.getPetById(id);
        model.addAttribute("pet", pet);
        System.out.println("Getting pet with ID: " + id);

        return "pet/detail";
    }

    // Profile editing
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/profile/edit")
    public String showProfileEditForm(Model model, Principal principal) {

        User userEntity = userService.getUserEntityByEmail(principal.getName());
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

    @CacheEvict(value = "allUsers", allEntries = true)
    @PostMapping("/profile/edit")
    public String handleProfileUpdate(
            @ModelAttribute("profileUpdateForm") @Valid UserProfileUpdateForm form,
            BindingResult result,
            Principal principal,
            Model model) {

        Runnable reloadModel = () -> {
            User userEntity = userService.getUserEntityByEmail(principal.getName());
            model.addAttribute("user", userEntity);
            model.addAttribute("currentPhotoUrl", userEntity.getUserPhotoUrl());
        };

        if (result.hasErrors()) {

            User userEntity = userService.getUserEntityByEmail(principal.getName());

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

    @GetMapping("/pet/{id}/apply")
    public String showPetApplicationForm(@PathVariable Long id,
                                         Model model,
                                         Principal principal) {
        Pet pet = petService.getPetById(id);
        User user = userService.getUserEntityByEmail(principal.getName());

        PetApplicationForm form = new PetApplicationForm();
        form.setPet(pet);
        form.setUser(user);

        model.addAttribute("petApplicationForm", form);
        model.addAttribute("homeType", HomeType.values());
        model.addAttribute("householdSituation", HouseholdSituation.values());
        model.addAttribute("otherPets", OtherPets.values());

        return "pet/apply";
    }

    @CacheEvict(value = "allApplications", allEntries = true)
    @PostMapping("/pet/{id}/apply")
    public String applyForPet(@PathVariable Long id,
                              @ModelAttribute("petApplicationForm")
                              @Valid PetApplicationForm form,
                              BindingResult result,
                              Model model) {

        if (result.hasErrors()) {
            model.addAttribute("homeType", HomeType.values());
            model.addAttribute("householdSituation", HouseholdSituation.values());
            model.addAttribute("otherPets", OtherPets.values());
            return "pet/apply";
        }
        try {
            appService.registerNewApplication(form);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("homeType", HomeType.values());
            model.addAttribute("householdSituation", HouseholdSituation.values());
            model.addAttribute("otherPets", OtherPets.values());
            model.addAttribute("error", "An error occurred while saving the application.");
            return "/pet/apply";
        }
        return "redirect:/pet/" + form.getPet().getId();
    }
}