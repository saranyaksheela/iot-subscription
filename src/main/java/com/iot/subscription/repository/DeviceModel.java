package com.iot.subscription.repository;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

import java.sql.ResultSet;
import java.sql.Timestamp;
import com.iot.subscription.utility.Constants;

public class DeviceModel {
    public static JsonObject fromResultSet(ResultSet rs) throws Exception {
        JsonObject obj = new JsonObject();
        obj.put(Constants.COL_ID, rs.getLong(Constants.COL_ID));
        obj.put(Constants.COL_DEVICE_UUID, rs.getObject(Constants.COL_DEVICE_UUID) != null ? rs.getObject(Constants.COL_DEVICE_UUID).toString() : null);
        obj.put(Constants.COL_DEVICE_NAME, rs.getString(Constants.COL_DEVICE_NAME));
        obj.put(Constants.COL_DEVICE_TYPE, rs.getString(Constants.COL_DEVICE_TYPE));
        obj.put(Constants.COL_FIRMWARE_VERSION, rs.getString(Constants.COL_FIRMWARE_VERSION));
        obj.put(Constants.COL_LOCATION, rs.getString(Constants.COL_LOCATION));
        obj.put(Constants.COL_STATUS, rs.getString(Constants.COL_STATUS));
        Timestamp ts = rs.getTimestamp(Constants.COL_CREATED_AT);
        obj.put(Constants.COL_CREATED_AT, ts != null ? ts.toInstant().toString() : null);
        return obj;
    }

    public static JsonObject fromRow(Row row) {
        JsonObject obj = new JsonObject();
        obj.put(Constants.COL_ID, row.getLong(Constants.COL_ID));
        Object uuid = row.getValue(Constants.COL_DEVICE_UUID);
        obj.put(Constants.COL_DEVICE_UUID, uuid != null ? uuid.toString() : null);
        obj.put(Constants.COL_DEVICE_NAME, row.getString(Constants.COL_DEVICE_NAME));
        obj.put(Constants.COL_DEVICE_TYPE, row.getString(Constants.COL_DEVICE_TYPE));
        obj.put(Constants.COL_FIRMWARE_VERSION, row.getString(Constants.COL_FIRMWARE_VERSION));
        obj.put(Constants.COL_LOCATION, row.getString(Constants.COL_LOCATION));
        obj.put(Constants.COL_STATUS, row.getString(Constants.COL_STATUS));
        java.time.OffsetDateTime odt = row.getOffsetDateTime(Constants.COL_CREATED_AT);
        if (odt != null) obj.put(Constants.COL_CREATED_AT, odt.toString());
        else {
            Object ts = row.getValue(Constants.COL_CREATED_AT);
            if (ts != null) obj.put(Constants.COL_CREATED_AT, ts.toString());
            else obj.put(Constants.COL_CREATED_AT, (String) null);
        }
        return obj;
    }
}