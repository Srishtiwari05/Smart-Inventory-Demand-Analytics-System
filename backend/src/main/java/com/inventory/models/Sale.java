package com.inventory.models;

import java.sql.Timestamp;

public class Sale {

    private int id;
    private int orderId;
    private Timestamp saleDate;
    private double totalRevenue;

    public Sale(int id, int orderId, Timestamp saleDate, double totalRevenue) {
        this.id = id;
        this.orderId = orderId;
        this.saleDate = saleDate;
        this.totalRevenue = totalRevenue;
    }

    public Sale(int orderId, double totalRevenue) {
        this.orderId = orderId;
        this.totalRevenue = totalRevenue;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public Timestamp getSaleDate() { return saleDate; }
    public void setSaleDate(Timestamp saleDate) { this.saleDate = saleDate; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    @Override
    public String toString() {
        return String.format("Sale [ID=%d, OrderID=%d, Revenue=$%.2f, Date=%s]",
                id, orderId, totalRevenue, saleDate);
    }
}
