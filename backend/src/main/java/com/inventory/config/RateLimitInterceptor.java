package com.inventory.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static class RateWindow {
        private int count;
        private long windowStart;

        public RateWindow(long now) {
            this.count = 1;
            this.windowStart = now;
        }

        public synchronized boolean allow(int maxRequests, long windowSizeMs) {
            long now = System.currentTimeMillis();
            if (now - windowStart > windowSizeMs) {
                windowStart = now;
                count = 1;
                return true;
            }
            if (count < maxRequests) {
                count++;
                return true;
            }
            return false;
        }
    }

    private final Map<String, RateWindow> rateMap = new ConcurrentHashMap<>();

    private static final int MAX_LOGIN_PER_MIN = 10;
    private static final int MAX_API_PER_MIN = 200;
    private static final long ONE_MINUTE_MS = 60_000L;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String clientIp = request.getRemoteAddr();
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = "unknown";
        }

        boolean isLogin = uri.equals("/api/auth/login");
        String key = isLogin ? "login:" + clientIp : "api:" + clientIp;
        int maxAllowed = isLogin ? MAX_LOGIN_PER_MIN : MAX_API_PER_MIN;

        long now = System.currentTimeMillis();
        RateWindow window = rateMap.computeIfAbsent(key, k -> new RateWindow(now));

        if (!window.allow(maxAllowed, ONE_MINUTE_MS)) {
            response.setStatus(429); // 429 Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too Many Requests\", \"message\": \"Rate limit exceeded. Please wait before retrying.\"}");
            return false;
        }

        return true;
    }
}
