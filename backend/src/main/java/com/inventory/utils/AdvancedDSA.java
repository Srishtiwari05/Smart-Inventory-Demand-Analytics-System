package com.inventory.utils;

import com.inventory.models.Product;
import com.inventory.models.Order;
import com.inventory.models.OrderItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AdvancedDSA {

    // Two Pointers: Find two products whose combined price is closest to a specified budget
    // Requires sorted list of products by price
    public static Product[] findTwoProductsClosestToBudget(List<Product> sortedProducts, double budget) {
        if (sortedProducts == null || sortedProducts.size() < 2) return null;

        int left = 0;
        int right = sortedProducts.size() - 1;

        Product[] closestPair = new Product[2];
        double minDiff = Double.MAX_VALUE;

        while (left < right) {
            double currentSum = sortedProducts.get(left).getPrice() + sortedProducts.get(right).getPrice();
            double diff = Math.abs(budget - currentSum);

            if (diff < minDiff) {
                minDiff = diff;
                closestPair[0] = sortedProducts.get(left);
                closestPair[1] = sortedProducts.get(right);
            }

            if (currentSum < budget) {
                left++;
            } else if (currentSum > budget) {
                right--;
            } else {
                // Exact match found
                break;
            }
        }

        return closestPair;
    }

    // Sliding Window: Find the highest-selling period (total units sold) over a fixed number of days
    // Simplification: We assume the list of orders is sorted by date and we group by day (or just consecutive orders)
    // To make it educational, let's find the max revenue in a sliding window of size K consecutive orders.
    public static double findMaxRevenueInSlidingWindow(List<Order> orders, int k) {
        if (orders == null || orders.isEmpty() || k <= 0) return 0;
        
        int windowSize = Math.min(k, orders.size());
        double maxRevenue = 0;
        double currentWindowRevenue = 0;

        // Initialize first window
        for (int i = 0; i < windowSize; i++) {
            currentWindowRevenue += orders.get(i).getTotalAmount();
        }
        maxRevenue = currentWindowRevenue;

        // Slide the window
        for (int i = windowSize; i < orders.size(); i++) {
            currentWindowRevenue += orders.get(i).getTotalAmount() - orders.get(i - windowSize).getTotalAmount();
            if (currentWindowRevenue > maxRevenue) {
                maxRevenue = currentWindowRevenue;
            }
        }

        return maxRevenue;
    }
}
