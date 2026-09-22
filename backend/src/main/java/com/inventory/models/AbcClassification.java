package com.inventory.models;

/**
 * Per-product result of ABC classification.
 * Class A = top 80% of cumulative revenue, B = 80-95%, C = 95-100%.
 */
public class AbcClassification {
    private int productId;
    private String productName;
    private String abcClass;           // "A", "B", or "C"
    private double revenueLast90Days;
    private int unitsLast90Days;
    private double revenuePercent;     // this product's share of total org revenue
    private double cumulativePercent;  // running cumulative % used to assign class

    public AbcClassification(int productId, String productName, String abcClass,
                             double revenueLast90Days, int unitsLast90Days,
                             double revenuePercent, double cumulativePercent) {
        this.productId = productId;
        this.productName = productName;
        this.abcClass = abcClass;
        this.revenueLast90Days = revenueLast90Days;
        this.unitsLast90Days = unitsLast90Days;
        this.revenuePercent = revenuePercent;
        this.cumulativePercent = cumulativePercent;
    }

    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getAbcClass() { return abcClass; }
    public double getRevenueLast90Days() { return revenueLast90Days; }
    public int getUnitsLast90Days() { return unitsLast90Days; }
    public double getRevenuePercent() { return revenuePercent; }
    public double getCumulativePercent() { return cumulativePercent; }
}
