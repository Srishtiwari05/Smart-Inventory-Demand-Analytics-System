package com.inventory.models;

import java.util.Date;
import java.util.List;

public class Order {
    private int id;
    private Customer customer;
    private List<OrderItem> orderItems;
    private double totalAmount;
    private Date orderDate;

    public Order(int id, Customer customer, List<OrderItem> orderItems, Date orderDate) {
        this.id = id;
        this.customer = customer;
        this.orderItems = orderItems;
        this.orderDate = orderDate;
        this.totalAmount = calculateTotal();
    }

    private double calculateTotal() {
        double total = 0;
        for (OrderItem item : orderItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public List<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItem> orderItems) { 
        this.orderItems = orderItems; 
        this.totalAmount = calculateTotal();
    }

    public double getTotalAmount() { return totalAmount; }

    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", customer=" + customer.getName() +
                ", itemsCount=" + orderItems.size() +
                ", totalAmount=" + totalAmount +
                ", orderDate=" + orderDate +
                '}';
    }
}
