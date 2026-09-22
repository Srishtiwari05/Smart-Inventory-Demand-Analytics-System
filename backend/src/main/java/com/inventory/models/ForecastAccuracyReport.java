package com.inventory.models;

import java.util.ArrayList;
import java.util.List;

public class ForecastAccuracyReport {
    private int orgId;
    private double overallAccuracyPct;
    private double meanAbsolutePercentageError;
    private double meanAbsoluteError;
    private double stockoutPreventionRatePct;
    private double recommendationAcceptanceRatePct;
    private int totalPredictionsEvaluated;
    private List<ProductAccuracyMetric> productMetrics = new ArrayList<>();

    public ForecastAccuracyReport() {}

    public ForecastAccuracyReport(int orgId, double overallAccuracyPct, double meanAbsolutePercentageError,
                                  double meanAbsoluteError, double stockoutPreventionRatePct,
                                  double recommendationAcceptanceRatePct, int totalPredictionsEvaluated,
                                  List<ProductAccuracyMetric> productMetrics) {
        this.orgId = orgId;
        this.overallAccuracyPct = overallAccuracyPct;
        this.meanAbsolutePercentageError = meanAbsolutePercentageError;
        this.meanAbsoluteError = meanAbsoluteError;
        this.stockoutPreventionRatePct = stockoutPreventionRatePct;
        this.recommendationAcceptanceRatePct = recommendationAcceptanceRatePct;
        this.totalPredictionsEvaluated = totalPredictionsEvaluated;
        this.productMetrics = productMetrics != null ? productMetrics : new ArrayList<>();
    }

    public int getOrgId() { return orgId; }
    public void setOrgId(int orgId) { this.orgId = orgId; }

    public double getOverallAccuracyPct() { return overallAccuracyPct; }
    public void setOverallAccuracyPct(double overallAccuracyPct) { this.overallAccuracyPct = overallAccuracyPct; }

    public double getMeanAbsolutePercentageError() { return meanAbsolutePercentageError; }
    public void setMeanAbsolutePercentageError(double meanAbsolutePercentageError) { this.meanAbsolutePercentageError = meanAbsolutePercentageError; }

    public double getMeanAbsoluteError() { return meanAbsoluteError; }
    public void setMeanAbsoluteError(double meanAbsoluteError) { this.meanAbsoluteError = meanAbsoluteError; }

    public double getStockoutPreventionRatePct() { return stockoutPreventionRatePct; }
    public void setStockoutPreventionRatePct(double stockoutPreventionRatePct) { this.stockoutPreventionRatePct = stockoutPreventionRatePct; }

    public double getRecommendationAcceptanceRatePct() { return recommendationAcceptanceRatePct; }
    public void setRecommendationAcceptanceRatePct(double recommendationAcceptanceRatePct) { this.recommendationAcceptanceRatePct = recommendationAcceptanceRatePct; }

    public int getTotalPredictionsEvaluated() { return totalPredictionsEvaluated; }
    public void setTotalPredictionsEvaluated(int totalPredictionsEvaluated) { this.totalPredictionsEvaluated = totalPredictionsEvaluated; }

    public List<ProductAccuracyMetric> getProductMetrics() { return productMetrics; }
    public void setProductMetrics(List<ProductAccuracyMetric> productMetrics) { this.productMetrics = productMetrics; }
}
