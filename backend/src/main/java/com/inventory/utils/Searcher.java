package com.inventory.utils;

import com.inventory.models.Product;
import java.util.List;

public class Searcher {

    // Linear Search by Name
    public static Product linearSearchByName(List<Product> products, String targetName) {
        for (Product product : products) {
            if (product.getName().equalsIgnoreCase(targetName)) {
                return product;
            }
        }
        return null; // Not found
    }

    // Binary Search by Price (Assuming array is sorted by price ascending)
    public static Product binarySearchByExactPrice(List<Product> sortedProducts, double targetPrice) {
        int left = 0;
        int right = sortedProducts.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            double midPrice = sortedProducts.get(mid).getPrice();

            if (Math.abs(midPrice - targetPrice) < 0.001) {
                return sortedProducts.get(mid);
            }
            if (midPrice < targetPrice) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return null;
    }

    // Lower Bound for Price (First product with price >= target)
    public static int lowerBoundPrice(List<Product> sortedProducts, double targetPrice) {
        int left = 0;
        int right = sortedProducts.size();

        while (left < right) {
            int mid = left + (right - left) / 2;
            if (sortedProducts.get(mid).getPrice() < targetPrice) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return left;
    }

    // Upper Bound for Price (First product with price > target)
    public static int upperBoundPrice(List<Product> sortedProducts, double targetPrice) {
        int left = 0;
        int right = sortedProducts.size();

        while (left < right) {
            int mid = left + (right - left) / 2;
            if (sortedProducts.get(mid).getPrice() <= targetPrice) {
                left = mid + 1;
            } else {
                right = mid;
            }
        }
        return left;
    }
}
