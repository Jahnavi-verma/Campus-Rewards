package com.campusrecycle.config;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
public class FirebaseTestRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n🧪 [TEST] Running Replit Firebase Connection Test...");

        try {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("replit_test_signal");
            CompletableFuture<Boolean> testSignal = new CompletableFuture<>();

            String timestamp = "Spring Boot connected from Replit at: " + LocalDateTime.now();

            // Fire write operation to the Realtime Database matrix
            ref.setValue(timestamp, (error, reference) -> {
                if (error != null) {
                    System.err.println("❌ [TEST] Firebase write failed: " + error.getMessage());
                    testSignal.complete(false);
                } else {
                    System.out.println("🟢 [TEST] SUCCESS! Spring Boot successfully wrote data to Firebase!");
                    testSignal.complete(true);
                }
            });

            // Block execution briefly to wait for the Firebase server to respond back to Replit
            testSignal.get();

        } catch (Exception e) {
            System.err.println("❌ [TEST] Firebase crashed! Verify your Replit secret key string: " + e.getMessage());
        }
    }
}