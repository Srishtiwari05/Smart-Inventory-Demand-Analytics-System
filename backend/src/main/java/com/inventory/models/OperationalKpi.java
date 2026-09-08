package com.inventory.models;

public class OperationalKpi {

    // Inventory
    private double totalInventoryValue;
    private int    totalProducts;
    private int    lowStockCount;
    private int    criticalStockCount;
    private int    expectedStockoutsCount; // stockout within 7 days

    // Procurement
    private int    pendingPOCount;
    private int    overduePOCount;
    private double overduePOValue;
    private int    pendingApprovalsCount;

    // Alerts
    private int    unreadAlertsCount;

    // Sales (30-day window)
    private double totalRevenue30d;
    private int    totalOrders30d;

    public OperationalKpi() {}

    public OperationalKpi(double totalInventoryValue, int totalProducts, int lowStockCount,
                          int criticalStockCount, int expectedStockoutsCount,
                          int pendingPOCount, int overduePOCount, double overduePOValue,
                          int pendingApprovalsCount, int unreadAlertsCount,
                          double totalRevenue30d, int totalOrders30d) {
        this.totalInventoryValue  = totalInventoryValue;
        this.totalProducts        = totalProducts;
        this.lowStockCount        = lowStockCount;
        this.criticalStockCount   = criticalStockCount;
        this.expectedStockoutsCount = expectedStockoutsCount;
        this.pendingPOCount       = pendingPOCount;
        this.overduePOCount       = overduePOCount;
        this.overduePOValue       = overduePOValue;
        this.pendingApprovalsCount = pendingApprovalsCount;
        this.unreadAlertsCount    = unreadAlertsCount;
        this.totalRevenue30d      = totalRevenue30d;
        this.totalOrders30d       = totalOrders30d;
    }

    public double getTotalInventoryValue()      { return totalInventoryValue; }
    public int    getTotalProducts()            { return totalProducts; }
    public int    getLowStockCount()            { return lowStockCount; }
    public int    getCriticalStockCount()       { return criticalStockCount; }
    public int    getExpectedStockoutsCount()   { return expectedStockoutsCount; }
    public int    getPendingPOCount()           { return pendingPOCount; }
    public int    getOverduePOCount()           { return overduePOCount; }
    public double getOverduePOValue()           { return overduePOValue; }
    public int    getPendingApprovalsCount()    { return pendingApprovalsCount; }
    public int    getUnreadAlertsCount()        { return unreadAlertsCount; }
    public double getTotalRevenue30d()          { return totalRevenue30d; }
    public int    getTotalOrders30d()           { return totalOrders30d; }
}
