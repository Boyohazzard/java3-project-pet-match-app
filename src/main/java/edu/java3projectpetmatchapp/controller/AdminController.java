package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.dto.UserRoleUpdateDto;
import edu.java3projectpetmatchapp.entity.User;
import edu.java3projectpetmatchapp.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/users/{id}/edit")
    public String showEditRoleForm(@PathVariable Long id, Model model){

        User userEntity = userService.getUserEntityById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + id));

        UserRoleUpdateDto form = new UserRoleUpdateDto();
        form.setId(userEntity.getId());
        form.setFirstName(userEntity.getFirstName());
        form.setLastName(userEntity.getLastName());
        form.setRole(userEntity.getRole());

        model.addAttribute("usr", userEntity);
        model.addAttribute("userRoleUpdateDto", form);
        return "admin_user_edit";
    }

    @PostMapping("users/{id}/edit")
    public String handleRoleUpdate(
            @PathVariable Long id,
            @ModelAttribute("userRoleUpdateDto") @Valid UserRoleUpdateDto form,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "admin_user_edit";
        }

        try {
            userService.updateUserRole(id, form.getRole());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Role for " + form.getFirstName() + " updated successfully!");
        } catch (UsernameNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "User not found.");
        }
        return "redirect:/admin/dashboard";
    }

}
