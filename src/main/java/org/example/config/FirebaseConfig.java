package org.example.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;

@Component
public class FirebaseConfig {

    @PostConstruct
    public void initFirebase() throws Exception {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        String serviceAccountPath = System.getenv("FIREBASE_SERVICE_ACCOUNT_PATH");

        if (serviceAccountPath == null || serviceAccountPath.trim().isEmpty()) {
            serviceAccountPath = "firebase-service-account.json";
        }

        File serviceAccountFile = new File(serviceAccountPath);

        if (!serviceAccountFile.exists()) {
            throw new IllegalStateException(
                    "Firebase service account file not found: " + serviceAccountFile.getAbsolutePath()
            );
        }

        try (FileInputStream serviceAccount = new FileInputStream(serviceAccountFile)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        }
    }
}