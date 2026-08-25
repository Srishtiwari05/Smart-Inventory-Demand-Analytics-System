package com.inventory.models;

public class SimulationResult {
    private Product product;
    private int currentRequiredReorder;
    private int simulatedRequiredReorder;
    private String scenarioDescription;

    public SimulationResult(Product product, int currentRequiredReorder, int simulatedRequiredReorder, String scenarioDescription) {
        this.product = product;
        this.currentRequiredReorder = currentRequiredReorder;
        this.simulatedRequiredReorder = simulatedRequiredReorder;
        this.scenarioDescription = scenarioDescription;
    }

    public Product getProduct() { return product; }
    public int getCurrentRequiredReorder() { return currentRequiredReorder; }
    public int getSimulatedRequiredReorder() { return simulatedRequiredReorder; }
    public String getScenarioDescription() { return scenarioDescription; }
}
