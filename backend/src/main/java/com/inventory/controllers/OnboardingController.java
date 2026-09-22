package com.inventory.controllers;

import com.inventory.models.Product;
import com.inventory.models.TenantOnboardingRequest;
import com.inventory.models.User;
import com.inventory.services.AuthService;
import com.inventory.services.OnboardingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/onboarding")
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final AuthService authService = AuthService.getInstance();

    @Autowired
    public OnboardingController(OnboardingService onboardingService) {
        this.onboardingService = onboardingService;
    }

    /**
     * Public self-serve organization and owner account onboarding.
     */
    @PostMapping("/register-tenant")
    public ResponseEntity<?> registerTenant(@RequestBody TenantOnboardingRequest request) {
        try {
            Map<String, Object> result = onboardingService.registerTenant(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Protected endpoint: bulk import product catalog for the authenticated tenant.
     */
    @PostMapping("/import-catalog")
    public ResponseEntity<?> importCatalog(@RequestBody List<Product> products, HttpServletRequest request) {
        User user = (User) request.getAttribute("authenticatedUser");
        if (user == null || !authService.hasPermission(user, "API_ADD_PRODUCT")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Only OWNER or MANAGER can import products."));
        }

        try {
            int imported = onboardingService.importCatalog(user.getOrgId(), user.getId(), products);
            return ResponseEntity.ok(Map.of("message", "Successfully imported " + imported + " products into catalog.", "count", imported));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
