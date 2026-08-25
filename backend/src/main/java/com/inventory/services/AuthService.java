package com.inventory.services;

import com.inventory.daos.UserDao;
import com.inventory.models.User;

import java.util.List;

public class AuthService {
    private UserDao userDao;

    public AuthService() {
        this.userDao = new UserDao();
    }

    /**
     * Authenticates a user by username and plain-text password.
     * Returns the User object on success, null on failure.
     */
    public User login(String username, String password) {
        User user = userDao.getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * Checks if a user has permission for a given action.
     * Role hierarchy:
     *   ADMIN   — full access
     *   MANAGER — manage products, orders, view reports (no user management)
     *   STAFF   — view products, place orders only
     */
    public boolean hasPermission(User user, String action) {
        if (user == null) return false;

        User.Role role = user.getRole();

        switch (action) {
            case "VIEW_PRODUCTS":
            case "SEARCH_PRODUCT":
            case "PLACE_ORDER":
            case "VIEW_ORDERS":
            case "VIEW_ORDERS_BY_CUSTOMER":
            case "DSA_DEMO":
                // All roles can do these
                return true;

            case "ADD_PRODUCT":
            case "UPDATE_STOCK":
            case "RESTOCK_PRODUCT":
            case "VIEW_TRANSACTIONS":
            case "SALES_REPORT":
                // MANAGER and ADMIN only
                return role == User.Role.ADMIN || role == User.Role.MANAGER;

            case "DELETE_PRODUCT":
            case "MANAGE_USERS":
                // ADMIN only
                return role == User.Role.ADMIN;

            default:
                return false;
        }
    }

    /**
     * Retrieves all users (for admin user management).
     */
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    /**
     * Adds a new user (admin only).
     */
    public void addUser(User user) {
        userDao.addUser(user);
    }

    /**
     * Deletes a user by ID (admin only).
     */
    public void deleteUser(int userId) {
        userDao.deleteUser(userId);
    }
}
