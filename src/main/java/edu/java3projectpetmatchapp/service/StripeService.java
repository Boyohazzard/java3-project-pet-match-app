package edu.java3projectpetmatchapp.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StripeService {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    private static final long ADOPTION_FEE_CENTS = 10000L; // $100.00
    private static final String CURRENCY = "usd";
    private static final String PRODUCT_NAME = "Pet Adoption Fee";

    public StripeService(@Value("${stripe.secret-key}") String stripeSecretKey) {
        Stripe.apiKey = stripeSecretKey;
    }

    public Map<String, String> createCheckoutSession(String adopterEmail, String petName, Long applicationId) throws StripeException {

        String YOUR_DOMAIN = "https://java3-project-pet-match-app-18dbe9455673.herokuapp.com";

        SessionCreateParams params = SessionCreateParams.builder()
                .setUiMode(SessionCreateParams.UiMode.EMBEDDED)
                .setMode(SessionCreateParams.Mode.PAYMENT)

                .putMetadata("application_id", String.valueOf(applicationId))
                .setReturnUrl(YOUR_DOMAIN + "/payment/return?session_id={CHECKOUT_SESSION_ID}&app_id=" + applicationId)
                .setCustomerEmail(adopterEmail)

                // Define the adoption fee
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(CURRENCY)
                                                .setUnitAmount(ADOPTION_FEE_CENTS)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(PRODUCT_NAME + " (" + petName + ")")
                                                                .build())
                                                .build())
                                .build())
                .build();

        Session session = Session.create(params);

        Map<String, String> map = new HashMap<>();
        map.put("clientSecret", session.getClientSecret());
        return map;
    }

    public Session retrieveSession(String sessionId) throws StripeException {
        return Session.retrieve(sessionId);
    }
}