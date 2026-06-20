package com.campusrecycle.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class DataSourceConfig {

    @Value("${SUPABASE_DB_URL:#{null}}")
    private String supabaseDbUrl;

    @Value("${DATABASE_URL:#{null}}")
    private String databaseUrl;

    @Primary
    @Bean
    public DataSource dataSource() {
        String rawUrl = (supabaseDbUrl != null && !supabaseDbUrl.isBlank())
                ? supabaseDbUrl : databaseUrl;

        if (rawUrl == null || rawUrl.isBlank()) {
            throw new IllegalStateException(
                "No database URL configured. Set SUPABASE_DB_URL or DATABASE_URL.");
        }

        // Convert postgres:// or postgresql:// to jdbc:postgresql://
        // Keep ALL query params intact (sslmode, etc.)
        String jdbcUrl = rawUrl
                .replaceFirst("^postgres://", "jdbc:postgresql://")
                .replaceFirst("^postgresql://", "jdbc:postgresql://");

        if (!jdbcUrl.startsWith("jdbc:")) {
            jdbcUrl = "jdbc:" + jdbcUrl;
        }

        // Build Hikari config before pool starts so properties are sealed correctly
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setConnectionTimeout(30000);
        config.setMaximumPoolSize(5);
        config.setMaxLifetime(1800000);
        config.setConnectionTestQuery("SELECT 1");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");

        return new HikariDataSource(config);
    }
}
