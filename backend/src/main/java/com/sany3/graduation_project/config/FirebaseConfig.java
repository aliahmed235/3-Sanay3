package com.sany3.graduation_project.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Firebase configuration - only initializes when firebase.enabled=true
 * Graceful degradation: if credentials file not found, logs warning and skips
 */
@Configuration
@ConditionalOnProperty(name = "firebase.enabled", havingValue = "true")
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials-file:}")
    private String credentialsFile;

    @Bean
    public FirebaseApp firebaseApp() {
        if (credentialsFile == null || credentialsFile.isBlank()) {
            log.warn("Firebase enabled but no credentials file configured — push notifications disabled");
            return null;
        }

        try {
            // Try multiple locations for the credentials file
            InputStream serviceAccount = findCredentialsFile(credentialsFile);

            if (serviceAccount == null) {
                log.warn("Firebase credentials file not found at '{}' — push notifications disabled", credentialsFile);
                return null;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp app = FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully");
            return app;

        } catch (Exception e) {
            log.warn("Failed to initialize Firebase: {} — push notifications disabled", e.getMessage());
            return null;
        }
    }

    /**
     * Try to find the credentials file in multiple locations:
     * 1. Exact path as given
     * 2. Inside the backend/ subdirectory
     * 3. On the classpath
     */
    private InputStream findCredentialsFile(String path) {
        // 1. Try exact path
        File file = new File(path);
        if (file.exists()) {
            log.info("Found Firebase credentials at: {}", file.getAbsolutePath());
            try { return new FileInputStream(file); } catch (Exception e) { /* fall through */ }
        }

        // 2. Try backend/ subdirectory (when app starts from parent directory)
        File backendFile = new File("backend/" + path);
        if (backendFile.exists()) {
            log.info("Found Firebase credentials at: {}", backendFile.getAbsolutePath());
            try { return new FileInputStream(backendFile); } catch (Exception e) { /* fall through */ }
        }

        // 3. Try classpath
        InputStream classpathStream = getClass().getClassLoader().getResourceAsStream(path);
        if (classpathStream != null) {
            log.info("Found Firebase credentials on classpath: {}", path);
            return classpathStream;
        }

        return null;
    }
}
