package edu.java3projectpetmatchapp.service;

import edu.java3projectpetmatchapp.dto.ProfileData;
import edu.java3projectpetmatchapp.dto.RegistrationForm;
import edu.java3projectpetmatchapp.entity.Application;
import edu.java3projectpetmatchapp.entity.User;
import edu.java3projectpetmatchapp.enums.Role;
import edu.java3projectpetmatchapp.repository.ApplicationRepository;
import edu.java3projectpetmatchapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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
        System.out.println("Assigning role: " + user.getRole().name());

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
}
