package com.inventory.models;

public class ReorderRecommendation {
    private Product product;
    private String riskLevel;
    private int currentStock;
    private double dailyDemand;
    private int leadTimeDays;
    private int safetyStock;
    private int reorderPoint;
    private int recommendedReorderQuantity;
    private int orderDeadlineDays;
    private int expectedStockoutDays;
    private String reason;
    private boolean actionRequired;

    public ReorderRecommendation(Product product, String riskLevel, int currentStock, double dailyDemand,
                                 int leadTimeDays, int safetyStock, int reorderPoint,
                                 int recommendedReorderQuantity, int orderDeadlineDays,
                                 int expectedStockoutDays, String reason, boolean actionRequired) {
        this.product = product;
        this.riskLevel = riskLevel;
        this.currentStock = currentStock;
        this.dailyDemand = dailyDemand;
        this.leadTimeDays = leadTimeDays;
        this.safetyStock = safetyStock;
        this.reorderPoint = reorderPoint;
        this.recommendedReorderQuantity = recommendedReorderQuantity;
        this.orderDeadlineDays = orderDeadlineDays;
        this.expectedStockoutDays = expectedStockoutDays;
        this.reason = reason;
        this.actionRequired = actionRequired;
    }

    public Product getProduct() { return product; }
    public String getRiskLevel() { return riskLevel; }
    public int getCurrentStock() { return currentStock; }
    public double getDailyDemand() { return dailyDemand; }
    public int getLeadTimeDays() { return leadTimeDays; }
    public int getSafetyStock() { return safetyStock; }
    public int getReorderPoint() { return reorderPoint; }
    public int getRecommendedReorderQuantity() { return recommendedReorderQuantity; }
    public int getOrderDeadlineDays() { return orderDeadlineDays; }
    public int getExpectedStockoutDays() { return expectedStockoutDays; }
    public String getReason() { return reason; }
    public boolean isActionRequired() { return actionRequired; }
}
