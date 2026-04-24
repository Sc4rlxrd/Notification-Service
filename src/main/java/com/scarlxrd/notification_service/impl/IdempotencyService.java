package com.scarlxrd.notification_service.impl;

import java.time.Duration;

public interface IdempotencyService {

    boolean isDuplicate(String context, Object key, Duration ttl);

}
