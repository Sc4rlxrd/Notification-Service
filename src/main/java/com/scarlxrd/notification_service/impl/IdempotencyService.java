package com.scarlxrd.notification_service.impl;

import java.time.Duration;

public interface IdempotencyService {

    boolean wasProcessed(String context, Object key, Duration ttl);

    void markAsProcessed(String context, Object key);
}