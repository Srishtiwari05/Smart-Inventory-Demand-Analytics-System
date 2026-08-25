package com.inventory.models;

public class Supplier {
    private int id;
    private String name;
    private String contactInfo;
    private int leadTimeDays;

    public Supplier(int id, String name, String contactInfo, int leadTimeDays) {
        this.id = id;
        this.name = name;
        this.contactInfo = contactInfo;
        this.leadTimeDays = leadTimeDays;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public int getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(int leadTimeDays) { this.leadTimeDays = leadTimeDays; }

    @Override
    public String toString() {
        return "Supplier{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", contactInfo='" + contactInfo + '\'' +
                ", leadTimeDays=" + leadTimeDays +
                '}';
    }
}
