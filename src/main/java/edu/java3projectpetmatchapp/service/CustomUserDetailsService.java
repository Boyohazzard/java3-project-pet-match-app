package edu.java3projectpetmatchapp.service;

import edu.java3projectpetmatchapp.dto.ProfileData;
import edu.java3projectpetmatchapp.dto.RegistrationForm;
import edu.java3projectpetmatchapp.dto.UserProfileUpdateForm;
import edu.java3projectpetmatchapp.entity.Application;
import edu.java3projectpetmatchapp.entity.User;
import edu.java3projectpetmatchapp.enums.Role;
import edu.java3projectpetmatchapp.repository.ApplicationRepository;
import edu.java3projectpetmatchapp.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepo;
    private final ApplicationRepository appRepo;
    private final S3StorageService s3Service;

    public CustomUserDetailsService(
            PasswordEncoder passwordEncoder,
            UserRepository userRepo,
            ApplicationRepository appRepo,
            S3StorageService s3Service) {
        this.passwordEncoder = passwordEncoder;
        this.userRepo = userRepo;
        this.appRepo = appRepo;
        this.s3Service = s3Service;
    }

    public void registerNewUser(RegistrationForm form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
        if (userRepo.findUserByEmail(form.getEmail()).isPresent()) {
            throw new IllegalArgumentException("That Email address is already in use");
        }
        User user = new User();
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setEmail(form.getEmail());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(Role.USER);
        user.setUserPhotoUrl(s3Service.getDefaultProfilePhotoUrl());
        userRepo.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepo.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

    }
    public ProfileData getProfileData(String email) {
        User user = userRepo.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Application> applications = appRepo.findByUser(user);
        return new ProfileData(user, applications);
    }

    public void updateProfile(UserProfileUpdateForm form, String userEmail) throws Exception {
        User user = userRepo.findUserByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        // Update basic profile fields
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setBio(form.getBio());
        user.setPetPreference(form.getPetPreference());

        String currentPhotoUrl = user.getUserPhotoUrl();

        // Handle Photo Deletion or Replacement
        if (form.isDeletePhoto()) {

            // Only delete the existing file from S3 if it's NOT the default photo
            if (!currentPhotoUrl.equals(s3Service.getDefaultProfilePhotoUrl())) {
                s3Service.deleteFileFromUrl(currentPhotoUrl);
            }
            user.setUserPhotoUrl(s3Service.getDefaultProfilePhotoUrl());

        } else if (form.getNewPhoto() != null && !form.getNewPhoto().isEmpty()) {

            // Delete the old photo (if it exists and is not the default)
            if (currentPhotoUrl != null && !currentPhotoUrl.equals(s3Service.getDefaultProfilePhotoUrl())) {
                s3Service.deleteFileFromUrl(currentPhotoUrl);
            }

            // Upload the new photo and get its URL
            String newUrl = s3Service.uploadFile(form.getNewPhoto());
            user.setUserPhotoUrl(newUrl);
        }

        userRepo.save(user);
    }

    public Optional<User> getUserEntityByEmail(String email) {
        return userRepo.findUserByEmail(email);
    }

    // List of all users for admin dashboard
    public List<User> getAllUsers(){
        return userRepo.findAll();
    }

    public Optional<User> getUserEntityById(Long id){
        return userRepo.findById(id);
    }

    @Transactional
    public void updateUserRole(Long userId, Role newRole) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        user.setRole(newRole);
        userRepo.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        // Delete all associated Applications
        appRepo.deleteByUser(user);

        // Then delete the User itself
        userRepo.delete(user);
    }

}
