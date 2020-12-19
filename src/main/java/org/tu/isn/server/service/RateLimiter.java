package org.tu.isn.server.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class RateLimiter implements DisposableBean {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final AtomicBoolean preDestroy = new AtomicBoolean(false);

    public RateLimiter() {
        new Thread(this::resetCachePeriodically).start();
    }

    public Bucket resolveForSessionId(String sessionId) {
        return cache.computeIfAbsent(sessionId, this::createBucket);
    }

    private Bucket createBucket(String sessionId) {
        String rateLimitPerHour = System.getenv("RATE_LIMIT_PER_HOUR");
        long rateLimit = Long.parseLong(rateLimitPerHour);
        return Bucket4j.builder()
                       .addLimit(Bandwidth.classic(rateLimit, Refill.intervally(rateLimit, Duration.ofHours(1))))
                       .build();
    }

    private void resetCachePeriodically() {
        String rateLimitCacheLifetime = System.getenv("RATE_LIMIT_CACHE_LIFETIME");
        long rateLimitReset = Long.parseLong(rateLimitCacheLifetime);
        long start = System.nanoTime();
        while (!preDestroy.get()) {
            long current = System.nanoTime();
            if (TimeUnit.NANOSECONDS.toHours(current - start) >= rateLimitReset) {
                start = current;
                cache.clear();
            }
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(10));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void destroy() {
        preDestroy.set(true);
        cache.clear();
    }
}
