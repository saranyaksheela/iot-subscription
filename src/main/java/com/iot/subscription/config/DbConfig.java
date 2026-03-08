package com.iot.subscription.config;

import java.util.Properties;

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
        String host = getEnv("PG_HOST", cfg.getProperty("pg.host", "localhost"));
        int port = parseIntSafe(getEnv("PG_PORT", cfg.getProperty("pg.port", "5432")), 5432);
        String database = getEnv("PG_DATABASE", cfg.getProperty("pg.database", "postgres"));
        String user = getEnv("PG_USER", cfg.getProperty("pg.user", "postgres"));
        String password = getEnv("PG_PASSWORD", cfg.getProperty("pg.password", "123"));
        int httpPort = parseIntSafe(getEnv("HTTP_PORT", cfg.getProperty("http.port", "8080")), 8080);
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
