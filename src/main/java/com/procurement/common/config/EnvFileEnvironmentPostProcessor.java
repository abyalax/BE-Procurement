package com.procurement.common.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class EnvFileEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String PROPERTY_SOURCE_NAME = "dotenv";
    private static final Path ENV_FILE = Path.of(".env");

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!Files.exists(ENV_FILE)) {
            return;
        }

        Map<String, Object> properties = loadEnvFile(ENV_FILE);
        if (properties.isEmpty()) {
            return;
        }

        environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    private Map<String, Object> loadEnvFile(Path path) {
        Map<String, Object> properties = new LinkedHashMap<>();

        try {
            for (String rawLine : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                String line = rawLine.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("export ")) {
                    line = line.substring("export ".length()).trim();
                }

                int separatorIndex = line.indexOf('=');
                if (separatorIndex <= 0) {
                    continue;
                }

                String key = line.substring(0, separatorIndex).trim();
                String value = line.substring(separatorIndex + 1).trim();

                if (key.isEmpty()) {
                    continue;
                }

                properties.put(key, stripMatchingQuotes(value));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read .env file", exception);
        }

        return properties;
    }

    private String stripMatchingQuotes(String value) {
        if (value.length() < 2) {
            return value;
        }

        boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
        boolean singleQuoted = value.startsWith("'") && value.endsWith("'");
        if (!doubleQuoted && !singleQuoted) {
            return value;
        }

        return value.substring(1, value.length() - 1);
    }
}
