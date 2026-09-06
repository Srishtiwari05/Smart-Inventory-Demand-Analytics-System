package com.inventory.models;

public class SupplierIntelligence {
    private Supplier supplier;
    private double totalPurchaseValue;
    private int totalOrders;
    private double onTimeDeliveryRate;
    private double actualLeadTimeDays;
    private int delayCount;
    private double reliabilityScore;
    private String ratingCategory;

    public SupplierIntelligence() {}

    public SupplierIntelligence(Supplier supplier, double totalPurchaseValue, int totalOrders,
                                double onTimeDeliveryRate, double actualLeadTimeDays,
                                int delayCount, double reliabilityScore, String ratingCategory) {
        this.supplier = supplier;
        this.totalPurchaseValue = totalPurchaseValue;
        this.totalOrders = totalOrders;
        this.onTimeDeliveryRate = onTimeDeliveryRate;
        this.actualLeadTimeDays = actualLeadTimeDays;
        this.delayCount = delayCount;
        this.reliabilityScore = reliabilityScore;
        this.ratingCategory = ratingCategory;
    }

    public Supplier getSupplier() { return supplier; }
    public void setSupplier(Supplier supplier) { this.supplier = supplier; }

    public double getTotalPurchaseValue() { return totalPurchaseValue; }
    public void setTotalPurchaseValue(double totalPurchaseValue) { this.totalPurchaseValue = totalPurchaseValue; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public double getOnTimeDeliveryRate() { return onTimeDeliveryRate; }
    public void setOnTimeDeliveryRate(double onTimeDeliveryRate) { this.onTimeDeliveryRate = onTimeDeliveryRate; }

    public double getActualLeadTimeDays() { return actualLeadTimeDays; }
    public void setActualLeadTimeDays(double actualLeadTimeDays) { this.actualLeadTimeDays = actualLeadTimeDays; }

    public int getDelayCount() { return delayCount; }
    public void setDelayCount(int delayCount) { this.delayCount = delayCount; }

    public double getReliabilityScore() { return reliabilityScore; }
    public void setReliabilityScore(double reliabilityScore) { this.reliabilityScore = reliabilityScore; }

    public String getRatingCategory() { return ratingCategory; }
    public void setRatingCategory(String ratingCategory) { this.ratingCategory = ratingCategory; }
}
