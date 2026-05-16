package com.procurement.common.config;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class ConnectionShutdownConfig {

    private static final Logger log = LoggerFactory.getLogger(ConnectionShutdownConfig.class);

    private final CacheManager cacheManager;

    @PreDestroy
    void onShutdown() {
        SecurityContextHolder.clearContext();
        cacheManager.getCacheNames().forEach(name ->
                Optional.ofNullable(cacheManager.getCache(name)).ifPresent(cache -> cache.clear()));
        log.info("Application shutdown cleanup completed");
    }
}
