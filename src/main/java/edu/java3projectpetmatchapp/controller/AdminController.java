package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.entity.User;
import edu.java3projectpetmatchapp.service.CustomUserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CustomUserDetailsService userService;

    public AdminController(CustomUserDetailsService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String showAdminDashboard(Model model) {

        List<User> users = userService.getAllUsers();

        model.addAttribute("users", users);
        return "admin_dashboard";
    }
}
