package com.inventory.models;

public class InventoryRiskReport {
    private Product product;
    private InventoryRisk riskLevel;
    private String explanation;

    public InventoryRiskReport(Product product, InventoryRisk riskLevel, String explanation) {
        this.product = product;
        this.riskLevel = riskLevel;
        this.explanation = explanation;
    }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public InventoryRisk getRiskLevel() { return riskLevel; }
    public void setRiskLevel(InventoryRisk riskLevel) { this.riskLevel = riskLevel; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
