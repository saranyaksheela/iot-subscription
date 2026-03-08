package com.iot.subscription;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.iot.subscription.config.DbConfig;
import com.iot.subscription.config.RouterConfig;
import com.iot.subscription.controller.DeviceController;
import com.iot.subscription.utility.Constants;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class DeviceSubscriptionVerticle extends AbstractVerticle {

    private DeviceController deviceController;
    private static final Logger LOGGER = Logger.getLogger(DeviceSubscriptionVerticle.class.getName());

    @Override
    public void start(Promise<Void> startPromise) {
        Properties cfg = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (in != null) cfg.load(in);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Warning: could not load config.properties: {0}", e.getMessage());
        }

        DbConfig dbConfig = DbConfig.fromProperties(cfg);
        deviceController = new DeviceController(dbConfig, vertx);
        RouterConfig routerConfig = new RouterConfig(vertx);
        Router router = routerConfig.createRouter();
        // Use wildcard prefix routes to accept trailing slashes and any minor suffixes.
        // Examples matched: /devices, /devices/, /devices?..., /devices/count, /devices/count/
        router.get(Constants.ENDPOINT_DEVICES).handler(this::handleGetDevices);
        router.get(Constants.ENDPOINT_DEVICES_COUNT).handler(this::handleGetDevicesCount);
        router.get(Constants.ENDPOINT_HEALTH).handler(this::handleHealth);

        int httpPort = dbConfig.getHttpPort();
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(httpPort, ar -> {
                if (ar.succeeded()) {
                    LOGGER.info("HTTP server started on port " + httpPort);
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
                sendJson(ctx, 200, arr.encode());
            } else {
                sendJson(ctx, 500, new JsonObject().put(Constants.JSON_KEY_ERROR, "DB error: " + ar.cause().getMessage()).encode());
            }
        });
    }

    private void handleGetDevicesCount(RoutingContext ctx) {
        deviceController.fetchDevicesCount().onComplete(ar -> {
            if (ar.succeeded()) {
                sendJson(ctx, 200, new JsonObject().put(Constants.JSON_KEY_COUNT, ar.result()).encode());
            } else {
                sendJson(ctx, 500, new JsonObject().put(Constants.JSON_KEY_ERROR, "DB error: " + ar.cause().getMessage()).encode());
            }
        });
    }

    private void handleHealth(RoutingContext ctx) {
        // lightweight DB check: try fetching count and report db as UP/DOWN
        deviceController.fetchDevicesCount().onComplete(ar -> {
            JsonObject res = new JsonObject();
            res.put(Constants.JSON_KEY_STATUS, Constants.JSON_STATUS_UP);
            if (ar.succeeded()) {
                res.put(Constants.JSON_KEY_DB, Constants.JSON_STATUS_UP);
            } else {
                res.put(Constants.JSON_KEY_DB, Constants.JSON_STATUS_DOWN);
                res.put(Constants.JSON_KEY_DB_ERROR, ar.cause().getMessage());
                // return 503 for strict readiness when DB is down
                sendJson(ctx, 503, res.encode());
                return;
            }
            sendJson(ctx, 200, res.encode());
        });
    }

    // Helper to centralize JSON responses
    private void sendJson(RoutingContext ctx, int status, String json) {
        ctx.response()
            .setStatusCode(status)
            .putHeader(Constants.HEADER_CONTENT_TYPE, Constants.CONTENT_TYPE_JSON)
            .end(json);
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