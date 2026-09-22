package com.inventory.models;

/**
 * Result of a demand anomaly check for a single product.
 * SPIKE = demand significantly above historical average.
 * CRASH = demand significantly below historical average.
 */
public class DemandAnomaly {
    private int productId;
    private String productName;
    private String type;                  // "SPIKE", "CRASH", or "NORMAL"
    private double currentAvgDailyDemand; // avg over last 7 days
    private double historicalAvgDailyDemand; // avg over last 60 days
    private double zScore;
    private double deviationPct;          // % deviation from historical mean

    public DemandAnomaly(int productId, String productName, String type,
                         double currentAvgDailyDemand, double historicalAvgDailyDemand,
                         double zScore, double deviationPct) {
        this.productId = productId;
        this.productName = productName;
        this.type = type;
        this.currentAvgDailyDemand = currentAvgDailyDemand;
        this.historicalAvgDailyDemand = historicalAvgDailyDemand;
        this.zScore = zScore;
        this.deviationPct = deviationPct;
    }

    public int getProductId() { return productId; }
    public String getProductName() { return productName; }
    public String getType() { return type; }
    public double getCurrentAvgDailyDemand() { return currentAvgDailyDemand; }
    public double getHistoricalAvgDailyDemand() { return historicalAvgDailyDemand; }
    public double getZScore() { return zScore; }
    public double getDeviationPct() { return deviationPct; }
}
