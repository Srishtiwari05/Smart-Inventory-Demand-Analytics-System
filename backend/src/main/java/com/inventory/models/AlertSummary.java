package com.inventory.models;

public class AlertSummary {
    private int totalUnread;
    private int criticalCount;
    private int highCount;
    private int mediumCount;
    private int lowCount;

    public AlertSummary() {}

    public AlertSummary(int totalUnread, int criticalCount, int highCount, int mediumCount, int lowCount) {
        this.totalUnread = totalUnread;
        this.criticalCount = criticalCount;
        this.highCount = highCount;
        this.mediumCount = mediumCount;
        this.lowCount = lowCount;
    }

    public int getTotalUnread() { return totalUnread; }
    public void setTotalUnread(int totalUnread) { this.totalUnread = totalUnread; }

    public int getCriticalCount() { return criticalCount; }
    public void setCriticalCount(int criticalCount) { this.criticalCount = criticalCount; }

    public int getHighCount() { return highCount; }
    public void setHighCount(int highCount) { this.highCount = highCount; }

    public int getMediumCount() { return mediumCount; }
    public void setMediumCount(int mediumCount) { this.mediumCount = mediumCount; }

    public int getLowCount() { return lowCount; }
    public void setLowCount(int lowCount) { this.lowCount = lowCount; }
}
