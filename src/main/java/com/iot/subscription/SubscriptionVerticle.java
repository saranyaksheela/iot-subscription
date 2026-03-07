package com.iot.subscription;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionVerticle extends AbstractVerticle {

    // Using JDBC connection for SCRAM auth compatibility
    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPassword;

    @Override
    public void start(Promise<Void> startPromise) {
        // Read Postgres connection info from environment variables with sensible defaults
        String pgHost = getEnv("PG_HOST", "localhost");
        int pgPort = parseIntSafe(getEnv("PG_PORT", "5432"), 5432);
        String pgDatabase = getEnv("PG_DATABASE", "postgres");
        String pgUser = getEnv("PG_USER", "postgres");
        String pgPassword = getEnv("PG_PASSWORD", "123");

        // Prepare JDBC connection details
        jdbcUser = pgUser;
        jdbcPassword = pgPassword;
        jdbcUrl = String.format("jdbc:postgresql://%s:%d/%s", pgHost, pgPort, pgDatabase);

        Router router = Router.router(vertx);
        router.get("/devices").handler(this::handleGetDevices);
        router.get("/devices/count").handler(this::handleGetDevicesCount);

        int httpPort = parseIntSafe(getEnv("HTTP_PORT", "8080"), 8080);
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

    private String getEnv(String name, String defaultVal) {
        String v = System.getenv(name);
        if (v == null) return defaultVal;
        return v.trim();
    }

    private int parseIntSafe(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private void handleGetDevices(RoutingContext ctx) {
        vertx.executeBlocking(promise -> {
            List<JsonObject> results = new ArrayList<>();
            String sql = "SELECT id, device_uuid, device_name, device_type, firmware_version, location, status, created_at FROM devices LIMIT 100";
            try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JsonObject obj = new JsonObject();
                    obj.put("id", rs.getLong("id"));
                    obj.put("device_uuid", rs.getObject("device_uuid") != null ? rs.getObject("device_uuid").toString() : null);
                    obj.put("device_name", rs.getString("device_name"));
                    obj.put("device_type", rs.getString("device_type"));
                    obj.put("firmware_version", rs.getString("firmware_version"));
                    obj.put("location", rs.getString("location"));
                    obj.put("status", rs.getString("status"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    obj.put("created_at", ts != null ? ts.toInstant().toString() : null);
                    results.add(obj);
                }
                promise.complete(results);
            } catch (Exception e) {
                promise.fail(e);
            }
        }, false, ar -> {
            if (ar.succeeded()) {
                JsonArray arr = new JsonArray((List) ar.result());
                ctx.response().putHeader("content-type", "application/json").end(arr.encode());
            } else {
                ctx.response().setStatusCode(500).end("DB error: " + ar.cause().getMessage());
            }
        });
    }

    private void handleGetDevicesCount(RoutingContext ctx) {
        vertx.executeBlocking(promise -> {
            String sql = "SELECT COUNT(*) AS cnt FROM devices";
            try (Connection conn = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                int count = 0;
                if (rs.next()) count = rs.getInt("cnt");
                promise.complete(count);
            } catch (Exception e) {
                promise.fail(e);
            }
        }, false, ar -> {
            if (ar.succeeded()) {
                ctx.response().putHeader("content-type", "application/json").end(new JsonObject().put("count", ar.result()).encode());
            } else {
                ctx.response().setStatusCode(500).end("DB error: " + ar.cause().getMessage());
            }
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        stopPromise.complete();
    }
}