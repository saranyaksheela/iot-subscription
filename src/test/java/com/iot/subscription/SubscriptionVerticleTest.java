package com.iot.subscription;

import org.junit.jupiter.api.Test;

public class SubscriptionVerticleTest {

    @Test
    void simpleSanity() {
        // Basic sanity test - JVM loads classes
        new DeviceSubscriptionVerticle();
    }
}
