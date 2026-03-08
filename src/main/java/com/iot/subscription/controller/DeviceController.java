package com.iot.subscription.controller;

import java.util.ArrayList;
import java.util.List;

import com.iot.subscription.config.DbConfig;
import com.iot.subscription.repository.DeviceModel;
import com.iot.subscription.utility.Constants;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgPool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

public class DeviceController {
    private final PgPool client;

    public DeviceController(DbConfig dbConfig, Vertx vertx) {
        PgConnectOptions connectOptions = new PgConnectOptions()
            .setHost(dbConfig.getHost())
            .setPort(dbConfig.getPort())
            .setDatabase(dbConfig.getDatabase())
            .setUser(dbConfig.getUser())
            .setPassword(dbConfig.getPassword());
        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
        this.client = PgPool.pool(vertx, connectOptions, poolOptions);
    }

    public Future<List<JsonObject>> fetchDevices() {
        Promise<List<JsonObject>> promise = Promise.promise();
        String sql = Constants.SQL_FETCH_DEVICES;
        client.query(sql).execute(ar -> {
            if (ar.succeeded()) {
                RowSet<Row> rows = ar.result();
                List<JsonObject> list = new ArrayList<>();
                for (Row row : rows) {
                    list.add(DeviceModel.fromRow(row));
                }
                promise.complete(list);
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    public Future<Integer> fetchDevicesCount() {
        Promise<Integer> promise = Promise.promise();
        String sql = Constants.SQL_COUNT_DEVICES;
        client.query(sql).execute(ar -> {
            if (ar.succeeded()) {
                RowSet<Row> rows = ar.result();
                int count = 0;
                for (Row row : rows) {
                    // getLong may be used then cast
                    Object v = row.getValue(Constants.SQL_COUNT_COLUMN);
                    if (v instanceof Number) count = ((Number) v).intValue();
                    else count = Integer.parseInt(String.valueOf(v));
                }
                promise.complete(count);
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    public void close(Promise<Void> when) {
        if (client != null) client.close(ar -> when.handle(ar.mapEmpty()));
        else when.complete();
    }
}