package com.sany3.graduation_project.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials-file:}")
    private String credentialsFile;

    @Value("${GOOGLE_APPLICATION_CREDENTIALS:}")
    private String credentialsJson;

    @Bean
    public FirebaseApp firebaseApp() {
        try {
            InputStream credentials = resolveCredentials();

            if (credentials == null) {
                log.warn("No Firebase credentials found — push notifications disabled");
                return null;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentials))
                    .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully");
            return app;

        } catch (Exception e) {
            log.warn("Failed to initialize Firebase: {} — push notifications disabled", e.getMessage());
            return null;
        }
    }

    private InputStream resolveCredentials() {
        // 1. Try GOOGLE_APPLICATION_CREDENTIALS as raw JSON (Railway env var)
        if (credentialsJson != null && !credentialsJson.isBlank() && credentialsJson.trim().startsWith("{")) {
            log.info("Using Firebase credentials from GOOGLE_APPLICATION_CREDENTIALS env var");
            return new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8));
        }

        // 2. Try credentials-file as file path
        if (credentialsFile != null && !credentialsFile.isBlank()) {
            // Try exact path
            File file = new File(credentialsFile);
            if (file.exists()) {
                log.info("Using Firebase credentials from file: {}", file.getAbsolutePath());
                try { return new FileInputStream(file); } catch (Exception e) { /* fall through */ }
            }

            // Try backend/ subdirectory
            File backendFile = new File("backend/" + credentialsFile);
            if (backendFile.exists()) {
                log.info("Using Firebase credentials from file: {}", backendFile.getAbsolutePath());
                try { return new FileInputStream(backendFile); } catch (Exception e) { /* fall through */ }
            }
        }

        // 3. Try classpath (src/main/resources)
        if (credentialsFile != null && !credentialsFile.isBlank()) {
            InputStream classpathStream = getClass().getClassLoader().getResourceAsStream(credentialsFile);
            if (classpathStream != null) {
                log.info("Using Firebase credentials from classpath: {}", credentialsFile);
                return classpathStream;
            }
        }

        // 4. Try GOOGLE_APPLICATION_CREDENTIALS as file path
        if (credentialsJson != null && !credentialsJson.isBlank() && !credentialsJson.trim().startsWith("{")) {
            File file = new File(credentialsJson);
            if (file.exists()) {
                log.info("Using Firebase credentials from GOOGLE_APPLICATION_CREDENTIALS file: {}", file.getAbsolutePath());
                try { return new FileInputStream(file); } catch (Exception e) { /* fall through */ }
            }
        }

        return null;
    }
}
