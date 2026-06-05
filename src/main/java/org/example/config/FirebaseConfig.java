package org.example.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;

@Component
public class FirebaseConfig {

    private static final Logger log = LoggerFactory.getLogger(FirebaseConfig.class);

    private boolean firebaseEnabled = false;

    @PostConstruct
    public void initFirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            firebaseEnabled = true;
            log.info("Firebase is already initialized");
            return;
        }

        String serviceAccountPath = System.getenv("FIREBASE_SERVICE_ACCOUNT_PATH");

        if (serviceAccountPath == null || serviceAccountPath.trim().isEmpty()) {
            serviceAccountPath = "firebase-service-account.json";
        }

        File serviceAccountFile = new File(serviceAccountPath);

        if (!serviceAccountFile.exists()) {
            log.warn(
                    "Firebase service account file not found: {}. " +
                    "Firebase auth will be disabled. " +
                    "Set FIREBASE_SERVICE_ACCOUNT_PATH env or place the file at the expected path.",
                    serviceAccountFile.getAbsolutePath()
            );
            firebaseEnabled = false;
            return;
        }

        try (FileInputStream serviceAccount = new FileInputStream(serviceAccountFile)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            firebaseEnabled = true;
            log.info("Firebase initialized successfully");
        } catch (Exception e) {
            log.warn(
                    "Failed to initialize Firebase: {}. Firebase auth will be disabled.",
                    e.getMessage()
            );
            firebaseEnabled = false;
        }
    }

    public boolean isFirebaseEnabled() {
        return firebaseEnabled;
    }
}
