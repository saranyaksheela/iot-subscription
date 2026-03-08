package com.iot.subscription;

import io.vertx.core.Vertx;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new DeviceSubscriptionVerticle(), res -> {
            if (res.succeeded()) {
                LOGGER.info("SubscriptionVerticle deployed");
            } else {
                LOGGER.log(Level.SEVERE, "Failed to deploy verticle:", res.cause());
                vertx.close();
            }
        });
    }
}