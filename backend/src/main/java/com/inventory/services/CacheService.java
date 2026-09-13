package com.inventory.services;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CacheService {

    private static class CacheEntry {
        private final Object value;
        private final long expiryTime;

        public CacheEntry(Object value, long ttlMillis) {
            this.value = value;
            this.expiryTime = System.currentTimeMillis() + ttlMillis;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }

        public Object getValue() {
            return value;
        }
    }

    private static final CacheService INSTANCE = new CacheService();
    public static CacheService getInstance() { return INSTANCE; }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public CacheService() {}

    private String buildTenantKey(int orgId, String key) {
        return "tenant:" + orgId + ":" + key;
    }

    public void put(int orgId, String key, Object value, long ttlMillis) {
        if (value == null) return;
        String tenantKey = buildTenantKey(orgId, key);
        cache.put(tenantKey, new CacheEntry(value, ttlMillis));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(int orgId, String key, Class<T> clazz) {
        String tenantKey = buildTenantKey(orgId, key);
        CacheEntry entry = cache.get(tenantKey);
        if (entry == null) {
            return null;
        }
        if (entry.isExpired()) {
            cache.remove(tenantKey);
            return null;
        }
        try {
            return (T) entry.getValue();
        } catch (ClassCastException e) {
            cache.remove(tenantKey);
            return null;
        }
    }

    public void invalidateKey(int orgId, String key) {
        String tenantKey = buildTenantKey(orgId, key);
        cache.remove(tenantKey);
    }

    public void invalidateTenant(int orgId) {
        String prefix = "tenant:" + orgId + ":";
        cache.keySet().removeIf(k -> k.startsWith(prefix));
    }

    public void clearAll() {
        cache.clear();
    }
}
