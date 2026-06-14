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
    public boolean wasProcessed(String context, Object key, Duration ttl) {
        String finalKey = buildKey(context, key);
        long now = System.currentTimeMillis();

        cleanup(now, ttl);

        Long existing = processed.get(finalKey);

        if (existing == null) {
            return false;
        }

        if (now - existing > ttl.toMillis()) {
            processed.remove(finalKey);
            return false;
        }

        return true;
    }

    @Override
    public void markAsProcessed(String context, Object key) {
        String finalKey = buildKey(context, key);
        processed.put(finalKey, System.currentTimeMillis());
    }

    private void cleanup(long now, Duration ttl) {
        processed.entrySet().removeIf(e ->
                now - e.getValue() > ttl.toMillis()
        );
    }
}