package com.iot.subscription.repository;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

import java.sql.ResultSet;
import java.sql.Timestamp;

public class DeviceModel {
    public static JsonObject fromResultSet(ResultSet rs) throws Exception {
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
        return obj;
    }

    public static JsonObject fromRow(Row row) {
        JsonObject obj = new JsonObject();
        obj.put("id", row.getLong("id"));
        Object uuid = row.getValue("device_uuid");
        obj.put("device_uuid", uuid != null ? uuid.toString() : null);
        obj.put("device_name", row.getString("device_name"));
        obj.put("device_type", row.getString("device_type"));
        obj.put("firmware_version", row.getString("firmware_version"));
        obj.put("location", row.getString("location"));
        obj.put("status", row.getString("status"));
        java.time.OffsetDateTime odt = row.getOffsetDateTime("created_at");
        if (odt != null) obj.put("created_at", odt.toString());
        else {
            Object ts = row.getValue("created_at");
            if (ts != null) obj.put("created_at", ts.toString());
            else obj.put("created_at", (String) null);
        }
        return obj;
    }
}