package org.tu.isn.server.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimiter implements DisposableBean {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private ScheduledFuture<?> cacheResetter;

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

    @PostConstruct
    public void resetCachePeriodically() {
        cacheResetter = Executors.newSingleThreadScheduledExecutor()
                                 .scheduleAtFixedRate(cache::clear, 60, TimeUnit.HOURS.toSeconds(3), TimeUnit.SECONDS);
    }

    @Override
    public void destroy() {
        cache.clear();
        cacheResetter.cancel(false);
    }
}
