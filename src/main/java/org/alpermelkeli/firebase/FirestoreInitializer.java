package org.alpermelkeli.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.alpermelkeli.util.ConfigUtil;

import java.io.IOException;
import java.io.InputStream;

public class FirestoreInitializer {
    private static Firestore firestore;

    public static Firestore getFirestore() {
        if (firestore == null) {
            try {
                InputStream serviceAccount = FirestoreInitializer.class
                        .getClassLoader()
                        .getResourceAsStream("laundryproject-firebase-adminsdk.json");
                if (serviceAccount == null) {
                    throw new RuntimeException("Firebase config file not found in resources!");
                }
                String projectId = ConfigUtil.getProperty("FIREBASE_PROJECT_ID");
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(projectId)
                        .build();

                FirebaseApp.initializeApp(options);

                firestore = FirestoreOptions.getDefaultInstance().getService();

                System.out.println("Firestore started successfully!");
            } catch (IOException e) {
                throw new RuntimeException("Firestore cannot started: " + e.getMessage());
            }
        }
        return firestore;
    }
}


