package org.monkey_business.utility_supervisor.limiter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BandwidthBuilder;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Bandwidth limit = BandwidthBuilder.builder().capacity(20)
            .refillIntervally(10, Duration.ofMinutes(1)).build();

    public boolean allowRequest(String userId) {
        Bucket bucket = buckets.computeIfAbsent(userId, id -> Bucket.builder().addLimit(limit).build());
        return bucket.tryConsume(1);
    }
}
