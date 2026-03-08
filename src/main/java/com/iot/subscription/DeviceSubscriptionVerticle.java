package com.iot.subscription;

import java.io.InputStream;
import java.util.Properties;

import com.iot.subscription.config.DbConfig;
import com.iot.subscription.config.RouterConfig;
import com.iot.subscription.controller.DeviceController;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class DeviceSubscriptionVerticle extends AbstractVerticle {

    private DeviceController deviceController;

    @Override
    public void start(Promise<Void> startPromise) {
        Properties cfg = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) cfg.load(in);
        } catch (Exception e) {
            System.err.println("Warning: could not load config.properties: " + e.getMessage());
        }

        DbConfig dbConfig = DbConfig.fromProperties(cfg);
        deviceController = new DeviceController(dbConfig, vertx);
        RouterConfig routerConfig = new RouterConfig(vertx);
        Router router = routerConfig.createRouter();
        // accept both /devices and /devices/ (trailing slash)
        router.get("/devices").handler(this::handleGetDevices);
        router.get("/devices/").handler(this::handleGetDevices);
        // accept both /devices/count and /devices/count/
        router.get("/devices/count").handler(this::handleGetDevicesCount);
        router.get("/devices/count/").handler(this::handleGetDevicesCount);

        int httpPort = dbConfig.getHttpPort();
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(httpPort, ar -> {
                if (ar.succeeded()) {
                    System.out.println("HTTP server started on port " + httpPort);
                    startPromise.complete();
                } else {
                    startPromise.fail(ar.cause());
                }
            });
    }

    private void handleGetDevices(RoutingContext ctx) {
        deviceController.fetchDevices().onComplete(ar -> {
            if (ar.succeeded()) {
                JsonArray arr = new JsonArray(ar.result());
                ctx.response().putHeader("content-type", "application/json").end(arr.encode());
            } else {
                ctx.response().setStatusCode(500).end("DB error: " + ar.cause().getMessage());
            }
        });
    }

    private void handleGetDevicesCount(RoutingContext ctx) {
        deviceController.fetchDevicesCount().onComplete(ar -> {
            if (ar.succeeded()) {
                ctx.response().putHeader("content-type", "application/json").end(new JsonObject().put("count", ar.result()).encode());
            } else {
                ctx.response().setStatusCode(500).end("DB error: " + ar.cause().getMessage());
            }
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        if (deviceController != null) {
            deviceController.close(stopPromise);
        } else {
            stopPromise.complete();
        }
    }
}