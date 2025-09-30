package edu.java3projectpetmatchapp.service;

import edu.java3projectpetmatchapp.dto.RegistrationForm;
import edu.java3projectpetmatchapp.entity.User;
import edu.java3projectpetmatchapp.enums.Role;
import edu.java3projectpetmatchapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepo;

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
        userRepo.save(user);
    }
}