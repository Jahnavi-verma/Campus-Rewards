package com.campusrecycle.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DataSourceConfig {

    @Value("${SUPABASE_DB_URL:#{null}}")
    private String supabaseDbUrl;

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Primary
    @Bean
    public DataSource dataSource() {
        // Prefer SUPABASE_DB_URL if set, fall back to DATABASE_URL
        String rawUrl = (supabaseDbUrl != null && !supabaseDbUrl.isBlank())
                ? supabaseDbUrl : databaseUrl;

        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalStateException(
                "No database URL configured. Set SUPABASE_DB_URL or DATABASE_URL.");
        }

        return buildDataSource(rawUrl);
    }

    private DataSource buildDataSource(String rawUrl) {
        try {
            // Normalise scheme
            String normalized = rawUrl
                    .replaceFirst("^postgres://", "postgresql://")
                    .replaceFirst("^jdbc:postgresql://", "postgresql://");

            // Strip query string
            String query = "";
            int qIdx = normalized.indexOf('?');
            if (qIdx >= 0) {
                query = normalized.substring(qIdx + 1);
                normalized = normalized.substring(0, qIdx);
            }

            URI uri = new URI(normalized);
            String host = uri.getHost();
            int port = uri.getPort() > 0 ? uri.getPort() : 5432;
            String path = uri.getPath(); // "/postgres"

            String username = null;
            String password = null;
            String userInfo = uri.getUserInfo();
            if (userInfo != null) {
                String[] parts = userInfo.split(":", 2);
                username = parts[0];
                password = parts.length > 1 ? parts[1] : "";
            }

            String jdbcUrl = String.format("jdbc:postgresql://%s:%d%s", host, port, path);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            if (username != null) config.setUsername(username);
            if (password != null) config.setPassword(password);
            config.setConnectionTimeout(30000);
            config.setMaximumPoolSize(5);

            // SSL for Supabase
            if (host != null && host.contains("supabase.co")) {
                config.addDataSourceProperty("sslmode", "require");
            } else if (query.contains("sslmode=disable")) {
                config.addDataSourceProperty("sslmode", "disable");
            }

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");

            return new HikariDataSource(config);

        } catch (URISyntaxException e) {
            throw new IllegalStateException("Cannot parse database URL: " + rawUrl, e);
        }
    }
}
