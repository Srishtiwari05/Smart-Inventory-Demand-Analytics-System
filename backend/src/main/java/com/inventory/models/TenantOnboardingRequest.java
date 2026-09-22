package com.inventory.models;

public class TenantOnboardingRequest {
    private String organizationName;
    private String ownerUsername;
    private String ownerPassword;
    private String contactEmail;
    private String industryType; // "ELECTRONICS", "GROCERY", "HARDWARE", "APPAREL", "GENERAL"

    public TenantOnboardingRequest() {}

    public TenantOnboardingRequest(String organizationName, String ownerUsername, String ownerPassword, String contactEmail, String industryType) {
        this.organizationName = organizationName;
        this.ownerUsername = ownerUsername;
        this.ownerPassword = ownerPassword;
        this.contactEmail = contactEmail;
        this.industryType = industryType;
    }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }

    public String getOwnerPassword() { return ownerPassword; }
    public void setOwnerPassword(String ownerPassword) { this.ownerPassword = ownerPassword; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getIndustryType() { return industryType; }
    public void setIndustryType(String industryType) { this.industryType = industryType; }
}
