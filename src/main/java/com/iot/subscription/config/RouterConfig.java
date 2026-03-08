package com.iot.subscription.config;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class RouterConfig {
    private final Vertx vertx;

    public RouterConfig(Vertx vertx) {
        this.vertx = vertx;
    }

    public Router createRouter() {
        Router router = Router.router(vertx);
        // routing will be added by SubscriptionVerticle wiring the handlers
        return router;
    }
}
