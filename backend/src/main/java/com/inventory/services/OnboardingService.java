package com.inventory.services;

import com.inventory.config.DatabaseConnection;
import com.inventory.daos.ProductDao;
import com.inventory.daos.UserDao;
import com.inventory.models.Product;
import com.inventory.models.TenantOnboardingRequest;
import com.inventory.models.User;
import com.inventory.utils.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OnboardingService {

    private final UserDao userDao = new UserDao();
    private final ProductDao productDao;
    private final CacheService cacheService;
    private final AuditLogService auditLogService;

    public OnboardingService() {
        this.productDao = new ProductDao();
        this.cacheService = CacheService.getInstance();
        this.auditLogService = new AuditLogService();
    }

    @Autowired
    public OnboardingService(ProductDao productDao, CacheService cacheService, AuditLogService auditLogService) {
        this.productDao = productDao;
        this.cacheService = cacheService;
        this.auditLogService = auditLogService;
    }

    /**
     * Self-serve tenant registration: provisions organization, owner user, default supplier,
     * and industry-tailored categories in an atomic transaction.
     */
    public Map<String, Object> registerTenant(TenantOnboardingRequest request) {
        if (request == null || request.getOrganizationName() == null || request.getOrganizationName().trim().isEmpty()) {
            throw new IllegalArgumentException("Organization name is required");
        }
        if (request.getOwnerUsername() == null || request.getOwnerUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Owner username is required");
        }
        if (userDao.getUserByUsername(request.getOwnerUsername().trim()) != null) {
            throw new IllegalArgumentException("Username '" + request.getOwnerUsername() + "' is already taken");
        }
        if (!SecurityUtil.validatePasswordStrength(request.getOwnerPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters long and contain both letters and digits");
        }

        int newOrgId = 0;
        int newUserId = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Create Organization
            String insertOrgSql = "INSERT INTO organizations (name) VALUES (?)";
            try (PreparedStatement orgStmt = conn.prepareStatement(insertOrgSql, Statement.RETURN_GENERATED_KEYS)) {
                orgStmt.setString(1, request.getOrganizationName().trim());
                orgStmt.executeUpdate();
                try (ResultSet rs = orgStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        newOrgId = rs.getInt(1);
                    }
                }
            }

            if (newOrgId == 0) {
                conn.rollback();
                throw new RuntimeException("Failed to generate organization record");
            }

            // 2. Create Owner User
            String hashedPassword = SecurityUtil.hashPassword(request.getOwnerPassword());
            String insertUserSql = "INSERT INTO users (username, password, role, org_id) VALUES (?, ?, 'OWNER', ?)";
            try (PreparedStatement userStmt = conn.prepareStatement(insertUserSql, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, request.getOwnerUsername().trim());
                userStmt.setString(2, hashedPassword);
                userStmt.setInt(3, newOrgId);
                userStmt.executeUpdate();
                try (ResultSet rs = userStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        newUserId = rs.getInt(1);
                    }
                }
            }

            // 3. Seed Industry Specific Categories
            seedIndustryCategories(conn, request.getIndustryType());

            // 4. Seed Default Primary Supplier
            String insertSupplierSql = "INSERT INTO suppliers (name, contact_info, lead_time_days) VALUES (?, ?, ?)";
            try (PreparedStatement supStmt = conn.prepareStatement(insertSupplierSql)) {
                String supplierName = request.getOrganizationName().trim() + " Primary Logistics";
                String contact = request.getContactEmail() != null ? request.getContactEmail() : "orders@" + request.getOwnerUsername() + ".com";
                supStmt.setString(1, supplierName);
                supStmt.setString(2, contact);
                supStmt.setInt(3, 5);
                supStmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error during tenant onboarding: " + e.getMessage(), e);
        }

        // Generate authenticated login session token
        User user = userDao.getUserByUsername(request.getOwnerUsername().trim());
        String token = AuthService.getInstance().login(request.getOwnerUsername().trim(), request.getOwnerPassword());

        // Audit log
        if (user != null) {
            auditLogService.log(newOrgId, user.getId(), "TENANT_REGISTERED", "ORGANIZATION", newOrgId,
                    "Self-serve onboarding completed for: " + request.getOrganizationName() + " (Industry: " + request.getIndustryType() + ")");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        result.put("orgId", newOrgId);
        result.put("message", "Welcome to Smart Inventory! Workspace successfully provisioned.");
        return result;
    }

    private void seedIndustryCategories(Connection conn, String industryType) throws SQLException {
        String[] categories;
        if ("GROCERY".equalsIgnoreCase(industryType)) {
            categories = new String[]{"Fresh Produce", "Dairy & Eggs", "Beverages", "Pantry Essentials", "Snacks"};
        } else if ("HARDWARE".equalsIgnoreCase(industryType)) {
            categories = new String[]{"Hand Tools", "Power Tools", "Fasteners & Fixings", "Plumbing", "Electrical"};
        } else if ("APPAREL".equalsIgnoreCase(industryType)) {
            categories = new String[]{"Men's Wear", "Women's Wear", "Footwear", "Accessories", "Sportswear"};
        } else { // ELECTRONICS / GENERAL
            categories = new String[]{"Electronics", "Peripherals & Accessories", "Components", "Office Supplies", "Storage"};
        }

        String insertCatSql = "INSERT INTO categories (name, description) VALUES (?, ?) ON DUPLICATE KEY UPDATE name=VALUES(name)";
        try (PreparedStatement catStmt = conn.prepareStatement(insertCatSql)) {
            for (String cat : categories) {
                catStmt.setString(1, cat);
                catStmt.setString(2, "Default category for " + cat);
                catStmt.addBatch();
            }
            catStmt.executeBatch();
        }
    }

    /**
     * Imports a bulk list of products for a tenant from CSV/JSON.
     */
    public int importCatalog(int orgId, int userId, List<Product> products) {
        if (products == null || products.isEmpty()) {
            return 0;
        }

        int importedCount = 0;
        for (Product p : products) {
            if (p.getName() == null || p.getName().trim().isEmpty()) continue;
            p.setOrgId(orgId);
            productDao.addProduct(p);
            importedCount++;
        }

        cacheService.invalidateTenant(orgId);
        auditLogService.log(orgId, userId, "CATALOG_BULK_IMPORTED", "PRODUCT", 0,
                "Imported " + importedCount + " SKUs into catalog via bulk onboarding tool.");

        return importedCount;
    }
}
