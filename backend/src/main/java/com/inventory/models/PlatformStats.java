package com.inventory.models;

public class PlatformStats {
    private int totalOrganizations;
    private int totalProductsTracked;
    private int totalTransactionsProcessed;
    private int totalOrdersFilled;
    private int totalForecastsGenerated;
    private double averageStockoutReductionPct;

    public PlatformStats() {}

    public PlatformStats(int totalOrganizations, int totalProductsTracked, int totalTransactionsProcessed,
                         int totalOrdersFilled, int totalForecastsGenerated, double averageStockoutReductionPct) {
        this.totalOrganizations = totalOrganizations;
        this.totalProductsTracked = totalProductsTracked;
        this.totalTransactionsProcessed = totalTransactionsProcessed;
        this.totalOrdersFilled = totalOrdersFilled;
        this.totalForecastsGenerated = totalForecastsGenerated;
        this.averageStockoutReductionPct = averageStockoutReductionPct;
    }

    public int getTotalOrganizations() { return totalOrganizations; }
    public void setTotalOrganizations(int totalOrganizations) { this.totalOrganizations = totalOrganizations; }

    public int getTotalProductsTracked() { return totalProductsTracked; }
    public void setTotalProductsTracked(int totalProductsTracked) { this.totalProductsTracked = totalProductsTracked; }

    public int getTotalTransactionsProcessed() { return totalTransactionsProcessed; }
    public void setTotalTransactionsProcessed(int totalTransactionsProcessed) { this.totalTransactionsProcessed = totalTransactionsProcessed; }

    public int getTotalOrdersFilled() { return totalOrdersFilled; }
    public void setTotalOrdersFilled(int totalOrdersFilled) { this.totalOrdersFilled = totalOrdersFilled; }

    public int getTotalForecastsGenerated() { return totalForecastsGenerated; }
    public void setTotalForecastsGenerated(int totalForecastsGenerated) { this.totalForecastsGenerated = totalForecastsGenerated; }

    public double getAverageStockoutReductionPct() { return averageStockoutReductionPct; }
    public void setAverageStockoutReductionPct(double averageStockoutReductionPct) { this.averageStockoutReductionPct = averageStockoutReductionPct; }
}
