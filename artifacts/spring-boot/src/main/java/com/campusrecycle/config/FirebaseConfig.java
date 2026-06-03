package com.campusrecycle.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // 🔒 Safely read the JSON text block straight out of your Environment Secrets manager
                String secretJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON");

                if (secretJson == null || secretJson.isBlank()) {
                    throw new IllegalStateException("CRITICAL ERROR: Environment variable FIREBASE_SERVICE_ACCOUNT_JSON is missing or empty!");
                }

                // Convert the raw text configuration string directly into a byte stream
                InputStream serviceAccount = new ByteArrayInputStream(secretJson.getBytes(StandardCharsets.UTF_8));

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setDatabaseUrl("https://recyclebin-d3be8-default-rtdb.firebaseio.com/")
                        .build();

                FirebaseApp.initializeApp(options);
                System.out.println("🚀 Firebase Admin SDK Initialized Securely from Environment Secrets!");
            }
        } catch (IOException e) {
            System.err.println("❌ Critical: Firebase Secret Account Parsing Failed! " + e.getMessage());
        }
    }
}