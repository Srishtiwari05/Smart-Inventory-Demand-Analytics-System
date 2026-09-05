package com.inventory.services;

import com.inventory.daos.UserDao;
import com.inventory.models.User;
import com.inventory.utils.SecurityUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    // Singleton so AuthInterceptor and AuthController share the same token store
    private static final AuthService INSTANCE = new AuthService();
    public static AuthService getInstance() { return INSTANCE; }

    private final UserDao userDao = new UserDao();
    private final Map<String, User> activeTokens = new ConcurrentHashMap<>();

    private AuthService() {}

    /**
     * Authenticates a user by username and password.
     * Returns a token on success, null on failure.
     */
    public String login(String username, String password) {
        User user = userDao.getUserByUsername(username);
        if (user != null) {
            String hashedInput = SecurityUtil.hashPassword(password);
            // Accept hashed match (new accounts) OR plain-text match (old seeded users)
            if (user.getPassword().equals(hashedInput) || user.getPassword().equals(password)) {
                String token = SecurityUtil.generateToken();
                activeTokens.put(token, user);
                return token;
            }
        }
        return null;
    }

    public User getUserByToken(String token) {
        if (token == null) return null;
        return activeTokens.get(token);
    }

    /**
     * Registers a new user with a hashed password.
     * Default role is STAFF for self-registered users.
     * Returns true if successful, false if username already taken.
     */
    public boolean registerUser(String username, String password) {
        if (userDao.getUserByUsername(username) != null) return false;
        String hashed = SecurityUtil.hashPassword(password);
        userDao.addUser(new User(username, hashed, User.Role.STAFF));
        return true;
    }

    /**
     * Checks if a user has permission for a given action.
     * Role hierarchy:
     *   OWNER   — full access
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
            // API access (all roles)
            case "API_VIEW_PRODUCTS":
            case "API_VIEW_ORDERS":
                return true;

            case "ADD_PRODUCT":
            case "UPDATE_STOCK":
            case "RESTOCK_PRODUCT":
            case "VIEW_TRANSACTIONS":
            case "SALES_REPORT":
            // API access (MANAGER and OWNER)
            case "API_ADD_PRODUCT":
            case "API_UPDATE_STOCK":
            case "API_VIEW_TRANSACTIONS":
            case "API_VIEW_ANALYTICS":
            case "API_PREDICT_DEMAND":
            case "API_ADJUST_STOCK":
                return role == User.Role.OWNER || role == User.Role.MANAGER;

            case "DELETE_PRODUCT":
            case "MANAGE_USERS":
            // API access (OWNER only)
            case "API_DELETE_PRODUCT":
            case "API_MANAGE_USERS":
                return role == User.Role.OWNER;

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
