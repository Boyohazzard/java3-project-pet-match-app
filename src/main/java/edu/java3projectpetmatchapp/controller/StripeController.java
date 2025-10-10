package edu.java3projectpetmatchapp.controller;

import edu.java3projectpetmatchapp.service.ApplicationService;
import edu.java3projectpetmatchapp.service.StripeService;
import com.stripe.model.checkout.Session;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.Map;

@Controller
@RequestMapping("/payment")
public class StripeController {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private edu.java3projectpetmatchapp.service.PetService petService;
    @Autowired
    private ApplicationService applicationService;

    private record SessionStatusResponse(String status, String customerEmail, Long applicationId) {}

    @GetMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public String showCheckoutPage(
            @RequestParam("appId") Long appId,
            @RequestParam("adopterEmail") String adopterEmail,
            @RequestParam("petName") String petName,
            Model model)
    {
        model.addAttribute("appId", appId);
        model.addAttribute("adopterEmail", adopterEmail);
        model.addAttribute("petName", petName);
        return "stripe/checkout";
    }

    @PostMapping("/create-checkout-session")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @RequestParam("adopterEmail") String adopterEmail,
            @RequestParam("petName") String petName,
            @RequestParam("applicationId") Long applicationId) { // Pass application ID here

        try {
            Map<String, String> response = stripeService.createCheckoutSession(adopterEmail, petName, applicationId);
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            System.err.println("Stripe API error creating session: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Payment service unavailable."));
        }
    }

    @GetMapping("/return")
    public String handleStripeReturn(
            @RequestParam("session_id") String sessionId,
            @RequestParam("app_id") Long applicationId,
            Model model)
    {
        try {
            Session session = stripeService.retrieveSession(sessionId);

            if ("complete".equals(session.getStatus())) {
                applicationService.updateApplicationStatus(applicationId, "PAID_ADOPTION_FEE");
            }

            model.addAttribute("status", session.getStatus());
            model.addAttribute("customerEmail", session.getCustomerDetails().getEmail());

            return "stripe/return";

        } catch (StripeException e) {
            System.err.println("Stripe API error on return: " + e.getMessage());
            return "error/stripe_error";
        }
    }

}