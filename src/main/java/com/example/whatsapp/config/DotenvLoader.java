package com.example.whatsapp.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads key=value pairs from a project-root {@code .env} file into the environment
 * when the variable is not already set (does not override real OS env).
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DotenvLoader implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envFile = Path.of(".env");
        if (!Files.isRegularFile(envFile)) {
            return;
        }

        Map<String, Object> values = new LinkedHashMap<>();
        try {
            for (String line : Files.readAllLines(envFile)) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }
                int idx = trimmed.indexOf('=');
                String key = trimmed.substring(0, idx).trim();
                String value = trimmed.substring(idx + 1).trim();
                if (key.isEmpty()) {
                    continue;
                }
                if (environment.getProperty(key) == null && System.getenv(key) == null) {
                    values.put(key, value);
                }
            }
        } catch (IOException ignored) {
            return;
        }

        if (!values.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource("dotenv", values));
        }
    }
}
