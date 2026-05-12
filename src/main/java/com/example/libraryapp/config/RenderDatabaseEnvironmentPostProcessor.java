package com.example.libraryapp.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Render injects {@code DB_URL} as a libpq-style URI ({@code postgresql://...}).
 * Spring JDBC expects {@code jdbc:postgresql://...}.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RenderDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String SOURCE_NAME = "renderJdbcDatasourceUrl";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty("DB_URL");
        if (url == null || url.isBlank()) {
            return;
        }
        String trimmed = url.trim();
        String jdbcUrl = toJdbcPostgresqlUrl(trimmed);
        if (jdbcUrl == null) {
            return;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("spring.datasource.url", jdbcUrl);
        environment.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, map));
    }

    private static String toJdbcPostgresqlUrl(String trimmed) {
        if (trimmed.startsWith("jdbc:postgresql:")) {
            return null;
        }
        if (trimmed.startsWith("postgresql://")) {
            return "jdbc:" + trimmed;
        }
        if (trimmed.startsWith("postgres://")) {
            return "jdbc:postgresql://" + trimmed.substring("postgres://".length());
        }
        return null;
    }
}
