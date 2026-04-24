package com.scarlxrd.notification_service.service;

import com.scarlxrd.notification_service.impl.IdempotencyService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryIdempotencyService implements IdempotencyService {

    private final Map<String, Long> processed = new ConcurrentHashMap<>();

    private String buildKey(String context, Object key) {
        return context + ":" + key;
    }

    @Override
    public boolean isDuplicate(String context, Object key, Duration ttl) {
        String finalKey = buildKey(context, key);
        long now = System.currentTimeMillis();

        cleanup(now, ttl);

        Long existing = processed.putIfAbsent(finalKey, now);

        if (existing == null) return false;

        if (now - existing > ttl.toMillis()) {
            processed.put(finalKey, now);
            return false;
        }

        return true;
    }

    private void cleanup(long now, Duration ttl) {
        processed.entrySet().removeIf(e ->
                now - e.getValue() > ttl.toMillis()
        );
    }
}
