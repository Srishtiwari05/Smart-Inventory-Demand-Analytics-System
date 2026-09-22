package com.inventory.services;

import com.inventory.models.Product;
import com.inventory.models.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TenantIsolationTest {

    @Test
    @DisplayName("User and Product models preserve organization identity")
    void testTenantScoping() {
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        User tenant1Admin = new User(1, "admin1", "pass", User.Role.OWNER, now, 1);
        User tenant2Admin = new User(2, "admin2", "pass", User.Role.OWNER, now, 2);

        assertEquals(1, tenant1Admin.getOrgId());
        assertEquals(2, tenant2Admin.getOrgId());
        assertNotEquals(tenant1Admin.getOrgId(), tenant2Admin.getOrgId());

        Product productTenant1 = new Product(101, "Org 1 Widget", "Electronics", 19.99, 100, null, 4.0);
        productTenant1.setOrgId(1);
        Product productTenant2 = new Product(102, "Org 2 Widget", "Electronics", 29.99, 50, null, 4.8);
        productTenant2.setOrgId(2);

        assertEquals(1, productTenant1.getOrgId());
        assertEquals(2, productTenant2.getOrgId());
        assertNotEquals(productTenant1.getOrgId(), productTenant2.getOrgId());
    }

    @Test
    @DisplayName("Product construction preserves default or assigned org_id")
    void testProductOrgIdIntegrity() {
        Product p = new Product(103, "Test Product", "General", 10.0, 5, null, 4.5);
        p.setOrgId(3);
        assertEquals(3, p.getOrgId());
    }
}
