package com.iot.subscription;

import io.vertx.core.Vertx;

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new DeviceSubscriptionVerticle(), res -> {
            if (res.succeeded()) {
                System.out.println("SubscriptionVerticle deployed");
            } else {
                System.err.println("Failed to deploy verticle: " + res.cause());
                vertx.close();
            }
        });
    }
}
