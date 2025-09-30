package edu.java3projectpetmatchapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

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

    @GetMapping("/user/home")
    public String viewUserIndex() {
        return "user/home";
    }

    @GetMapping("/register")
    public String viewRegistration() {
        return "register";
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


