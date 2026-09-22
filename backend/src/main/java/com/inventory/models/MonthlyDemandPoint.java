package com.inventory.models;

/**
 * One data point in a seasonal trend series: units sold in a given month.
 */
public class MonthlyDemandPoint {
    private int productId;
    private String productName;
    private String yearMonth;    // e.g. "2026-08"
    private int totalUnitsSold;

    public MonthlyDemandPoint(int productId, String productName, String yearMonth, int totalUnitsSold) {
        this.productId = productId;
        this.productName = productName;
        this.yearMonth = yearMonth;
        this.totalUnitsSold = totalUnitsSold;
    }

    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getYearMonth() { return yearMonth; }
    public int getTotalUnitsSold() { return totalUnitsSold; }
}
