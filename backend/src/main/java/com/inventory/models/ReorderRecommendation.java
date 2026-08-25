package com.inventory.models;

public class ReorderRecommendation {
    private Product product;
    private int recommendedReorderQuantity;
    private String reason;
    private boolean actionRequired;

    public ReorderRecommendation(Product product, int recommendedReorderQuantity, String reason, boolean actionRequired) {
        this.product = product;
        this.recommendedReorderQuantity = recommendedReorderQuantity;
        this.reason = reason;
        this.actionRequired = actionRequired;
    }

    public Product getProduct() { return product; }
    public int getRecommendedReorderQuantity() { return recommendedReorderQuantity; }
    public String getReason() { return reason; }
    public boolean isActionRequired() { return actionRequired; }
}
