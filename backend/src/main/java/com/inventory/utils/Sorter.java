package com.inventory.utils;

import com.inventory.models.Product;
import java.util.List;

public class Sorter {

    // Bubble Sort by Price (Ascending)
    public static void bubbleSortByPrice(List<Product> products) {
        int n = products.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (products.get(j).getPrice() > products.get(j + 1).getPrice()) {
                    Product temp = products.get(j);
                    products.set(j, products.get(j + 1));
                    products.set(j + 1, temp);
                }
            }
        }
    }

    // Selection Sort by Rating (Descending)
    public static void selectionSortByRatingDesc(List<Product> products) {
        int n = products.size();
        for (int i = 0; i < n - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (products.get(j).getRating() > products.get(maxIdx).getRating()) {
                    maxIdx = j;
                }
            }
            Product temp = products.get(maxIdx);
            products.set(maxIdx, products.get(i));
            products.set(i, temp);
        }
    }

    // Insertion Sort by Name (Alphabetical)
    public static void insertionSortByName(List<Product> products) {
        int n = products.size();
        for (int i = 1; i < n; i++) {
            Product key = products.get(i);
            int j = i - 1;

            while (j >= 0 && products.get(j).getName().compareToIgnoreCase(key.getName()) > 0) {
                products.set(j + 1, products.get(j));
                j = j - 1;
            }
            products.set(j + 1, key);
        }
    }
}
