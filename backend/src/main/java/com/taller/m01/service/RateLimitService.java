package com.taller.m01.service;

import com.taller.m01.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.*;
import java.util.concurrent.*;

@Service
public class RateLimitService {
    private record Bucket(Instant startedAt, int attempts) { }
    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    public void check(String operation, String identity) {
        String key = operation + ':' + identity;
        buckets.compute(key, (ignored, bucket) -> {
            Instant now = Instant.now();
            if (bucket == null || bucket.startedAt().plusSeconds(60).isBefore(now)) return new Bucket(now, 1);
            if (bucket.attempts() >= 10) throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "Demasiadas solicitudes. Intente más tarde.");
            return new Bucket(bucket.startedAt(), bucket.attempts() + 1);
        });
    }
}
