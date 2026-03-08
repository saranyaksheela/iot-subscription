package com.iot.subscription.config;

import java.util.Properties;
import com.iot.subscription.utility.Constants;

public class DbConfig {
    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String password;
    private final int httpPort;

    public DbConfig(String host, int port, String database, String user, String password, int httpPort) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
        this.httpPort = httpPort;
    }

    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getDatabase() { return database; }
    public String getUser() { return user; }
    public String getPassword() { return password; }
    public int getHttpPort() { return httpPort; }

    public static DbConfig fromProperties(Properties cfg) {
        String host = getEnv(Constants.ENV_PG_HOST, cfg.getProperty("pg.host", Constants.DEFAULT_PG_HOST));
        int port = parseIntSafe(getEnv(Constants.ENV_PG_PORT, cfg.getProperty("pg.port", Integer.toString(Constants.DEFAULT_PG_PORT))), Constants.DEFAULT_PG_PORT);
        String database = getEnv(Constants.ENV_PG_DATABASE, cfg.getProperty("pg.database", Constants.DEFAULT_PG_DATABASE));
        String user = getEnv(Constants.ENV_PG_USER, cfg.getProperty("pg.user", Constants.DEFAULT_PG_USER));
        String password = getEnv(Constants.ENV_PG_PASSWORD, cfg.getProperty("pg.password", Constants.DEFAULT_PG_PASSWORD));
        int httpPort = parseIntSafe(getEnv(Constants.ENV_HTTP_PORT, cfg.getProperty("http.port", Integer.toString(Constants.DEFAULT_HTTP_PORT))), Constants.DEFAULT_HTTP_PORT);
        return new DbConfig(host, port, database, user, password, httpPort);
    }

    private static String getEnv(String name, String defaultVal) {
        String v = System.getenv(name);
        if (v == null) return defaultVal;
        return v.trim();
    }

    private static int parseIntSafe(String s, int fallback) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return fallback; }
    }
}