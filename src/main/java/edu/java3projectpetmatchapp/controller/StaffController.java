package edu.java3projectpetmatchapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaffController {

    @GetMapping( "/staff/home")
    public String viewStaffIndex() {
        return "staff/home";
    }
}
