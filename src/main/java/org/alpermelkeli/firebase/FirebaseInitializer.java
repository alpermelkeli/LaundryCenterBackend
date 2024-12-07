package org.alpermelkeli.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.Firestore;
import org.alpermelkeli.util.ConfigUtil;

import java.io.IOException;
import java.io.InputStream;

public class FirebaseInitializer {

    private static Firestore firestore;
    private static FirebaseApp firebaseApp;

    private FirebaseInitializer() {

    }

    private static synchronized void initializeFirebaseApp() {
        if (firebaseApp == null) {
            try (InputStream serviceAccount = FirebaseInitializer.class
                    .getClassLoader()
                    .getResourceAsStream("laundryproject-firebase-adminsdk.json")) {

                if (serviceAccount == null) {
                    throw new IllegalStateException("Firebase config file not found in resources!");
                }

                String projectId = ConfigUtil.getProperty("FIREBASE_PROJECT_ID");

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(projectId)
                        .build();

                firebaseApp = FirebaseApp.initializeApp(options);
                System.out.println("Firebase app started successfully!");

            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize Firebase app: " + e.getMessage(), e);
            }
        }
    }

    public static FirebaseApp getFirebaseApp() {
        if (firebaseApp == null) {
            initializeFirebaseApp();
        }
        return firebaseApp;
    }

    public static Firestore getFirestore() {
        if (firestore == null) {
            initializeFirebaseApp();
            synchronized (FirebaseInitializer.class) {
                if (firestore == null) {
                    firestore = FirestoreOptions.getDefaultInstance().getService();
                    System.out.println("Firestore started successfully!");
                }
            }
        }
        return firestore;
    }

}
