package com.iot.subscription.utility;

public final class Constants {

    private Constants() {}

    // Endpoints (use wildcard patterns where applicable)
    public static final String ENDPOINT_DEVICES = "/devices*";
    public static final String ENDPOINT_DEVICES_COUNT = "/devices/count*";
    public static final String ENDPOINT_HEALTH = "/health*";

    // HTTP headers and content types
    public static final String HEADER_CONTENT_TYPE = "content-type";
    public static final String CONTENT_TYPE_JSON = "application/json";

    // JSON keys and values
    public static final String JSON_KEY_COUNT = "count";
    public static final String JSON_KEY_STATUS = "status";
    public static final String JSON_KEY_DB = "db";
    public static final String JSON_KEY_ERROR = "error";
    public static final String JSON_KEY_DB_ERROR = "dbError";

    public static final String JSON_STATUS_UP = "UP";
    public static final String JSON_STATUS_DOWN = "DOWN";

    // Environment variables names
    public static final String ENV_PG_HOST = "PG_HOST";
    public static final String ENV_PG_PORT = "PG_PORT";
    public static final String ENV_PG_DATABASE = "PG_DATABASE";
    public static final String ENV_PG_USER = "PG_USER";
    public static final String ENV_PG_PASSWORD = "PG_PASSWORD";
    public static final String ENV_HTTP_PORT = "HTTP_PORT";

    // Property keys from config.properties
    public static final String PROP_PG_HOST = "pg.host";
    public static final String PROP_PG_PORT = "pg.port";
    public static final String PROP_PG_DATABASE = "pg.database";
    public static final String PROP_PG_USER = "pg.user";
    public static final String PROP_PG_PASSWORD = "pg.password";
    public static final String PROP_HTTP_PORT = "http.port";

    // Default DB values
    public static final String DEFAULT_PG_HOST = "localhost";
    public static final int DEFAULT_PG_PORT = 5432;
    public static final String DEFAULT_PG_DATABASE = "postgres";
    public static final String DEFAULT_PG_USER = "postgres";
    public static final String DEFAULT_PG_PASSWORD = "123";

    // Database / SQL constants
    public static final String TABLE_DEVICES = "devices";
    public static final String SQL_FETCH_DEVICES = "SELECT id, device_uuid, device_name, device_type, firmware_version, location, status, created_at FROM devices LIMIT 100";
    public static final String SQL_COUNT_DEVICES = "SELECT COUNT(*) AS cnt FROM devices";
    public static final String SQL_COUNT_COLUMN = "cnt";

    // Devices table column names
    public static final String COL_ID = "id";
    public static final String COL_DEVICE_UUID = "device_uuid";
    public static final String COL_DEVICE_NAME = "device_name";
    public static final String COL_DEVICE_TYPE = "device_type";
    public static final String COL_FIRMWARE_VERSION = "firmware_version";
    public static final String COL_LOCATION = "location";
    public static final String COL_STATUS = "status";
    public static final String COL_CREATED_AT = "created_at";

    // Defaults
    public static final int DEFAULT_HTTP_PORT = 8082;
}