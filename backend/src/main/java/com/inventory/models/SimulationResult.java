package com.inventory.models;

public class SimulationResult {
    private Product product;
    
    // Scenario Inputs
    private double demandMultiplier;
    private int extraLeadTimeDays;
    private double priceAdjustmentPct;

    // Baseline Metrics
    private double baseDailyDemand;
    private int baseLeadTimeDays;
    private int baseSafetyStock;
    private int baseReorderPoint;
    private int baseExpectedStockoutDays;
    private int currentRequiredReorder;
    private InventoryRisk baseRiskLevel;

    // Simulated Metrics
    private double simDailyDemand;
    private int simLeadTimeDays;
    private int simSafetyStock;
    private int simReorderPoint;
    private int simExpectedStockoutDays;
    private int simulatedRequiredReorder;
    private InventoryRisk simRiskLevel;

    // Comparative Deltas
    private int reorderQtyDelta;
    private int stockoutDaysDelta;
    private double estimatedCostDelta;

    // Explanations
    private String scenarioDescription;
    private String explanation;

    public SimulationResult() {}

    // Constructor for full comprehensive scenario comparison
    public SimulationResult(Product product, double demandMultiplier, int extraLeadTimeDays, double priceAdjustmentPct,
                            double baseDailyDemand, int baseLeadTimeDays, int baseSafetyStock, int baseReorderPoint,
                            int baseExpectedStockoutDays, int currentRequiredReorder, InventoryRisk baseRiskLevel,
                            double simDailyDemand, int simLeadTimeDays, int simSafetyStock, int simReorderPoint,
                            int simExpectedStockoutDays, int simulatedRequiredReorder, InventoryRisk simRiskLevel,
                            int reorderQtyDelta, int stockoutDaysDelta, double estimatedCostDelta,
                            String scenarioDescription, String explanation) {
        this.product = product;
        this.demandMultiplier = demandMultiplier;
        this.extraLeadTimeDays = extraLeadTimeDays;
        this.priceAdjustmentPct = priceAdjustmentPct;
        this.baseDailyDemand = baseDailyDemand;
        this.baseLeadTimeDays = baseLeadTimeDays;
        this.baseSafetyStock = baseSafetyStock;
        this.baseReorderPoint = baseReorderPoint;
        this.baseExpectedStockoutDays = baseExpectedStockoutDays;
        this.currentRequiredReorder = currentRequiredReorder;
        this.baseRiskLevel = baseRiskLevel;
        this.simDailyDemand = simDailyDemand;
        this.simLeadTimeDays = simLeadTimeDays;
        this.simSafetyStock = simSafetyStock;
        this.simReorderPoint = simReorderPoint;
        this.simExpectedStockoutDays = simExpectedStockoutDays;
        this.simulatedRequiredReorder = simulatedRequiredReorder;
        this.simRiskLevel = simRiskLevel;
        this.reorderQtyDelta = reorderQtyDelta;
        this.stockoutDaysDelta = stockoutDaysDelta;
        this.estimatedCostDelta = estimatedCostDelta;
        this.scenarioDescription = scenarioDescription;
        this.explanation = explanation;
    }

    // Backward-compatible constructor
    public SimulationResult(Product product, int currentRequiredReorder, int simulatedRequiredReorder, String scenarioDescription) {
        this.product = product;
        this.currentRequiredReorder = currentRequiredReorder;
        this.simulatedRequiredReorder = simulatedRequiredReorder;
        this.scenarioDescription = scenarioDescription;
        this.explanation = scenarioDescription;
    }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public double getDemandMultiplier() { return demandMultiplier; }
    public void setDemandMultiplier(double demandMultiplier) { this.demandMultiplier = demandMultiplier; }

    public int getExtraLeadTimeDays() { return extraLeadTimeDays; }
    public void setExtraLeadTimeDays(int extraLeadTimeDays) { this.extraLeadTimeDays = extraLeadTimeDays; }

    public double getPriceAdjustmentPct() { return priceAdjustmentPct; }
    public void setPriceAdjustmentPct(double priceAdjustmentPct) { this.priceAdjustmentPct = priceAdjustmentPct; }

    public double getBaseDailyDemand() { return baseDailyDemand; }
    public void setBaseDailyDemand(double baseDailyDemand) { this.baseDailyDemand = baseDailyDemand; }

    public int getBaseLeadTimeDays() { return baseLeadTimeDays; }
    public void setBaseLeadTimeDays(int baseLeadTimeDays) { this.baseLeadTimeDays = baseLeadTimeDays; }

    public int getBaseSafetyStock() { return baseSafetyStock; }
    public void setBaseSafetyStock(int baseSafetyStock) { this.baseSafetyStock = baseSafetyStock; }

    public int getBaseReorderPoint() { return baseReorderPoint; }
    public void setBaseReorderPoint(int baseReorderPoint) { this.baseReorderPoint = baseReorderPoint; }

    public int getBaseExpectedStockoutDays() { return baseExpectedStockoutDays; }
    public void setBaseExpectedStockoutDays(int baseExpectedStockoutDays) { this.baseExpectedStockoutDays = baseExpectedStockoutDays; }

    public int getCurrentRequiredReorder() { return currentRequiredReorder; }
    public void setCurrentRequiredReorder(int currentRequiredReorder) { this.currentRequiredReorder = currentRequiredReorder; }

    public InventoryRisk getBaseRiskLevel() { return baseRiskLevel; }
    public void setBaseRiskLevel(InventoryRisk baseRiskLevel) { this.baseRiskLevel = baseRiskLevel; }

    public double getSimDailyDemand() { return simDailyDemand; }
    public void setSimDailyDemand(double simDailyDemand) { this.simDailyDemand = simDailyDemand; }

    public int getSimLeadTimeDays() { return simLeadTimeDays; }
    public void setSimLeadTimeDays(int simLeadTimeDays) { this.simLeadTimeDays = simLeadTimeDays; }

    public int getSimSafetyStock() { return simSafetyStock; }
    public void setSimSafetyStock(int simSafetyStock) { this.simSafetyStock = simSafetyStock; }

    public int getSimReorderPoint() { return simReorderPoint; }
    public void setSimReorderPoint(int simReorderPoint) { this.simReorderPoint = simReorderPoint; }

    public int getSimExpectedStockoutDays() { return simExpectedStockoutDays; }
    public void setSimExpectedStockoutDays(int simExpectedStockoutDays) { this.simExpectedStockoutDays = simExpectedStockoutDays; }

    public int getSimulatedRequiredReorder() { return simulatedRequiredReorder; }
    public void setSimulatedRequiredReorder(int simulatedRequiredReorder) { this.simulatedRequiredReorder = simulatedRequiredReorder; }

    public InventoryRisk getSimRiskLevel() { return simRiskLevel; }
    public void setSimRiskLevel(InventoryRisk simRiskLevel) { this.simRiskLevel = simRiskLevel; }

    public int getReorderQtyDelta() { return reorderQtyDelta; }
    public void setReorderQtyDelta(int reorderQtyDelta) { this.reorderQtyDelta = reorderQtyDelta; }

    public int getStockoutDaysDelta() { return stockoutDaysDelta; }
    public void setStockoutDaysDelta(int stockoutDaysDelta) { this.stockoutDaysDelta = stockoutDaysDelta; }

    public double getEstimatedCostDelta() { return estimatedCostDelta; }
    public void setEstimatedCostDelta(double estimatedCostDelta) { this.estimatedCostDelta = estimatedCostDelta; }

    public String getScenarioDescription() { return scenarioDescription; }
    public void setScenarioDescription(String scenarioDescription) { this.scenarioDescription = scenarioDescription; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
