package com.inventory.models;

import java.sql.Timestamp;

public class User {

    public enum Role {
        OWNER, MANAGER, STAFF, SUPPLIER
    }

    private int id;
    private String username;
    private String password;
    private Role role;
    private Timestamp createdAt;
    private int orgId;
    private Integer supplierId;

    public User(int id, String username, String password, Role role, Timestamp createdAt, int orgId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
        this.orgId = orgId;
    }

    public User(int id, String username, String password, Role role, Timestamp createdAt, int orgId, Integer supplierId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
        this.orgId = orgId;
        this.supplierId = supplierId;
    }

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getOrgId() { return orgId; }
    public void setOrgId(int orgId) { this.orgId = orgId; }

    public Integer getSupplierId() { return supplierId; }
    public void setSupplierId(Integer supplierId) { this.supplierId = supplierId; }

    @Override
    public String toString() {
        return String.format("User [ID=%d, Username=%s, Role=%s, OrgId=%d, SupplierId=%s, CreatedAt=%s]",
                id, username, role, orgId, supplierId, createdAt);
    }
}
