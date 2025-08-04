package com.sky_ecommerce.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Loads variables from .env (project root) very early and injects them into Spring Environment.
 * This allows using ${VAR} placeholders in application.yml without relying on external OS env injection.
 *
 * Order: It hooks at ApplicationEnvironmentPreparedEvent so that properties are available
 * before bean creation and @Value resolution.
 */
@Component
public class EnvLoaderConfig implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final Logger log = LoggerFactory.getLogger(EnvLoaderConfig.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment env = event.getEnvironment();

        try {
            // Load .env from working directory; dotenv-java tolerates missing files by default when ignoreIfMissing(true)
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            Map<String, Object> props = new HashMap<>();

            // Push all .env entries as high-priority property source
            dotenv.entries().forEach(e -> props.put(e.getKey(), e.getValue()));

            if (!props.isEmpty()) {
                MapPropertySource ps = new MapPropertySource("dotenv", props);
                // Add first so it has high precedence over application.yml defaults
                env.getPropertySources().addFirst(ps);
                log.info("Loaded {} entries from .env into Spring Environment", props.size());
            } else {
                log.info(".env not found or empty; skipping dotenv injection");
            }
        } catch (Exception ex) {
            log.warn("Failed to load .env file: {}", ex.getMessage());
        }
    }
}
