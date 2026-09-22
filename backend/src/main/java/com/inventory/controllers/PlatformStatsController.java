package com.inventory.controllers;

import com.inventory.daos.PlatformStatsDao;
import com.inventory.models.PlatformStats;
import com.inventory.services.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform")
public class PlatformStatsController {

    private final PlatformStatsDao platformStatsDao;
    private final CacheService cacheService;

    @Autowired
    public PlatformStatsController(PlatformStatsDao platformStatsDao, CacheService cacheService) {
        this.platformStatsDao = platformStatsDao;
        this.cacheService = cacheService;
    }

    /**
     * Public endpoint: returns aggregated, anonymized platform metrics.
     * Complies strictly with the Data Privacy Contract — never reveals individual tenant data or PII.
     */
    @GetMapping("/stats")
    public ResponseEntity<PlatformStats> getPlatformStats() {
        // Cache platform-level stats with orgId=0 for 60 seconds
        PlatformStats cached = cacheService.get(0, "platform_stats", PlatformStats.class);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }

        PlatformStats stats = platformStatsDao.getAggregatePlatformStats();
        cacheService.put(0, "platform_stats", stats, 60_000L); // 60s TTL
        return ResponseEntity.ok(stats);
    }
}
