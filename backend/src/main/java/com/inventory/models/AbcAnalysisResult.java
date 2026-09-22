package com.inventory.models;

import java.util.List;

/**
 * Full ABC analysis report for an organization.
 * Contains the classified item list and aggregate counts per class.
 */
public class AbcAnalysisResult {
    private List<AbcClassification> items;
    private double totalRevenue;
    private int classACount;
    private int classBCount;
    private int classCCount;

    public AbcAnalysisResult(List<AbcClassification> items, double totalRevenue,
                             int classACount, int classBCount, int classCCount) {
        this.items = items;
        this.totalRevenue = totalRevenue;
        this.classACount = classACount;
        this.classBCount = classBCount;
        this.classCCount = classCCount;
    }

    public List<AbcClassification> getItems() { return items; }
    public double getTotalRevenue() { return totalRevenue; }
    public int getClassACount() { return classACount; }
    public int getClassBCount() { return classBCount; }
    public int getClassCCount() { return classCCount; }
}
