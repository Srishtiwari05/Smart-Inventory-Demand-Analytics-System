package com.inventory.models;

public class ProductAccuracyMetric {
    private int productId;
    private String productName;
    private int predictedDemand;
    private int actualDemand;
    private double errorPct;
    private double accuracyPct;
    private int deviationQty;

    public ProductAccuracyMetric() {}

    public ProductAccuracyMetric(int productId, String productName, int predictedDemand, int actualDemand, double errorPct) {
        this.productId = productId;
        this.productName = productName;
        this.predictedDemand = predictedDemand;
        this.actualDemand = actualDemand;
        this.errorPct = errorPct;
        this.accuracyPct = Math.max(0.0, +(100.0 - errorPct));
        this.deviationQty = predictedDemand - actualDemand;
    }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getPredictedDemand() { return predictedDemand; }
    public void setPredictedDemand(int predictedDemand) { this.predictedDemand = predictedDemand; }

    public int getActualDemand() { return actualDemand; }
    public void setActualDemand(int actualDemand) { this.actualDemand = actualDemand; }

    public double getErrorPct() { return errorPct; }
    public void setErrorPct(double errorPct) { this.errorPct = errorPct; }

    public double getAccuracyPct() { return accuracyPct; }
    public void setAccuracyPct(double accuracyPct) { this.accuracyPct = accuracyPct; }

    public int getDeviationQty() { return deviationQty; }
    public void setDeviationQty(int deviationQty) { this.deviationQty = deviationQty; }
}
