package com.campusrecycle.service;

import com.campusrecycle.dto.SubmissionRequest;
import com.campusrecycle.model.RecyclingSubmission;
import com.campusrecycle.model.User;
import com.campusrecycle.repository.RecyclingSubmissionRepository;
import com.campusrecycle.repository.UserRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class RecyclingSubmissionService {

    private static final String ITEM_PLASTIC = "Plastic Bottle";
    private static final String ITEM_METAL = "Aluminum Can";

    private static final int POINTS_PER_PLASTIC = 10; 
    private static final int POINTS_PER_METAL = 15;

    private final RecyclingSubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public RecyclingSubmissionService(RecyclingSubmissionRepository submissionRepository,
                                      UserRepository userRepository,
                                      UserService userService) {
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    /**
     * 🛰️ Fetches the live root activeSession token ID string from the Realtime Database.
     */
    public CompletableFuture<String> getActiveSessionId() {
        System.out.println("🛰️ [LOG] Fetching current activeSessionId token from Firebase...");
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference activeSessionRef = db.getReference("activeSession");
        CompletableFuture<String> future = new CompletableFuture<>();

        activeSessionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String activeSessionId = snapshot.getValue(String.class);
                System.out.println("🛰️ [LOG] Firebase activeSession root reads: " + activeSessionId);
                future.complete(activeSessionId != null ? activeSessionId : "");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("❌ [LOG ERROR] Firebase activeSession check failed: " + error.getMessage());
                future.completeExceptionally(error.toException());
            }
        });

        return future;
    }

    @Transactional
    public String processQrClaim(Long userId, String sessionId) throws ExecutionException, InterruptedException {
        System.out.println("\n========================================================");
        System.out.println("🚀 [QR CLAIM START] Incoming claim transaction triggered!");
        System.out.println("📍 Parameter User ID: " + userId);
        System.out.println("📍 Parameter Session ID: " + sessionId);
        System.out.println("========================================================");

        System.out.println("🔍 [STEP 1] Confirming if User ID exists in Neon Postgres...");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    System.out.println("❌ [CRITICAL] User lookup failed for ID: " + userId);
                    return new RuntimeException("User not found: " + userId);
                });
        System.out.println("🟢 User verified successfully in database. Internal User reference secured.");

        FirebaseDatabase db = FirebaseDatabase.getInstance();

        // Step 1: Read the activeSession pointer from the DB root
        System.out.println("🔍 [STEP 2] Validating active hardware token in Firebase...");
        DatabaseReference activeSessionRef = db.getReference("activeSession");
        CompletableFuture<DataSnapshot> activeSessionFuture = new CompletableFuture<>();

        activeSessionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                activeSessionFuture.complete(snapshot);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                activeSessionFuture.completeExceptionally(error.toException());
            }
        });

        DataSnapshot activeSessionSnapshot = activeSessionFuture.get();
        String activeSessionId = activeSessionSnapshot.getValue(String.class);
        System.out.println("📄 Firebase Active Bin Token: [" + activeSessionId + "]");

        if (activeSessionId == null || activeSessionId.isBlank()) {
            System.out.println("❌ [ABORT] activeSession root value is completely empty inside Firebase!");
            throw new RuntimeException("No active recycling session found on the bin right now.");
        }

        // Step 2: Confirm the session id the frontend sent matches the bin's active session
        System.out.println("🔍 [STEP 3] Comparing Frontend Token against Live Hardware Snapshot...");
        if (!activeSessionId.equals(sessionId)) {
            System.out.println("❌ [ABORT] Handshake mismatch! Frontend passed: " + sessionId + " | Active: " + activeSessionId);
            throw new RuntimeException("This QR code does not match the bin's current active session. Please rescan.");
        }
        System.out.println("🟢 Handshake Token match confirmed!");

        // Step 3: Fetch the confirmed session's data
        System.out.println("🔍 [STEP 4] Downloading full session nodes payload from Firebase...");
        DatabaseReference ref = db.getReference("sessions").child(activeSessionId);
        CompletableFuture<DataSnapshot> future = new CompletableFuture<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) { 
                future.complete(snapshot); 
            }
            @Override
            public void onCancelled(DatabaseError error) { 
                future.completeExceptionally(error.toException()); 
            }
        });

        DataSnapshot snapshot = future.get();

        if (!snapshot.exists()) {
            System.out.println("❌ [ABORT] Node lookup returned null. Target path key doesn't exist: sessions/" + activeSessionId);
            throw new RuntimeException("Invalid QR Code: This recycling session does not exist!");
        }

        // 🛑 --- STEP 4 DIAGNOSTICS: EXTRACTING & VALIDATING DATA FROM SNAPSHOT ---
        System.out.println("\n🔍 [STEP 4 DIAGNOSTICS] Reading and Validating Firebase Data Payload Nodes...");
        String status = null;
        int plasticCount = 0;
        int metalCount = 0;
        int totalPoints = 0;

        try {
            status = snapshot.child("status").getValue(String.class);
            System.out.println("   👉 Raw status string from Firebase: '" + status + "'");

            if (status == null) {
                System.out.println("❌ [ABORT STEP 4] Status variable string returned null pointer lifecycle flag!");
                throw new RuntimeException("Corrupted data payload: Missing active status state!");
            }

            if ("active".equalsIgnoreCase(status)) {
                System.out.println("❌ [ABORT STEP 4] Bin is still accepting items. Status cannot be active during claims.");
                throw new RuntimeException("Session is still active! Please wait for the hardware bin to finish processing your items.");
            }

            if ("claimed".equalsIgnoreCase(status)) {
                System.out.println("❌ [ABORT STEP 4] Session double-spend prevented! Status already marked 'claimed'.");
                throw new RuntimeException("This QR code has already been claimed by another student!");
            }

            if (!"completed".equalsIgnoreCase(status)) {
                System.out.println("❌ [ABORT STEP 4] Invalid status configuration block found: " + status);
                throw new RuntimeException("Invalid session state: Cannot claim a '" + status + "' session.");
            }

            Integer plasticCountObj = snapshot.child("plasticCount").getValue(Integer.class);
            Integer metalCountObj = snapshot.child("metalCount").getValue(Integer.class);

            System.out.println("   👉 Raw plasticCount object wrapper: " + plasticCountObj);
            System.out.println("   👉 Raw metalCount object wrapper: " + metalCountObj);

            plasticCount = plasticCountObj != null ? plasticCountObj : 0;
            metalCount = metalCountObj != null ? metalCountObj : 0;

            totalPoints = (plasticCount * POINTS_PER_PLASTIC) + (metalCount * POINTS_PER_METAL);
            System.out.println("   📊 Evaluated Metrics -> Plastics: " + plasticCount + " | Metals: " + metalCount + " | Total Points: " + totalPoints);

            if (totalPoints == 0) {
                System.out.println("❌ [ABORT STEP 4] Points pool evaluated to 0. Stopping database execution pipeline.");
                throw new RuntimeException("No items were detected in this recycling session.");
            }
            System.out.println("🟢 [STEP 4 SUCCESS] Snapshot parsed cleanly. Data is structurally sound.");
        } catch (Exception e) {
            System.out.println("💥 [CRITICAL FAILURE IN STEP 4] Error parsing snapshot or checking session boundaries!");
            System.out.println("   Details: " + e.getMessage());
            throw e;
        }

        // 🛑 --- STEP 5 DIAGNOSTICS: SENDING DATA INSERT STATEMENTS TO NEON POSTGRES ---
        System.out.println("\n⚡ [STEP 5 DIAGNOSTICS] Initiating Neon Postgres Storage Save Routine via Hibernate...");
        try {
            if (plasticCount > 0) {
                int plasticPoints = plasticCount * POINTS_PER_PLASTIC;
                System.out.println("   📝 [STEP 5a] Persisting plastic row entry: " + plasticCount + " x " + ITEM_PLASTIC);
                saveAutomatedRecord(user, ITEM_PLASTIC, plasticCount, plasticPoints);
                System.out.println("   🟢 [STEP 5a SUCCESS] Plastic record row stored in Hibernate persistence context.");
            }
            if (metalCount > 0) {
                int metalPoints = metalCount * POINTS_PER_METAL;
                System.out.println("   📝 [STEP 5b] Persisting metal row entry: " + metalCount + " x " + ITEM_METAL);
                saveAutomatedRecord(user, ITEM_METAL, metalCount, metalPoints);
                System.out.println("   🟢 [STEP 5b SUCCESS] Metal record row stored in Hibernate persistence context.");
            }
            System.out.println("🟢 [STEP 5 SUCCESS] All individual item records written successfully.");
        } catch (Exception dbEx) {
            System.out.println("💥 [CRITICAL CRASH IN STEP 5] Neon Postgres transaction failed during submission save!");
            System.out.println("   Details: " + dbEx.getMessage());
            dbEx.printStackTrace();
            throw dbEx;
        }

        // 🛑 --- STEP 6 DIAGNOSTICS: USER BALANCES UPDATE ---
        System.out.println("\n🛰️ [STEP 6 DIAGNOSTICS] Invoking point balance increment inside userService...");
        try {
            System.out.println("   📝 Adding +" + totalPoints + " reward allocation points to User ID: " + userId);
            userService.addPoints(userId, totalPoints);
            System.out.println("   🟢 [STEP 6 SUCCESS] User entity point balances adjusted successfully without rollbacks.");
        } catch (Exception userEx) {
            System.out.println("💥 [CRITICAL CRASH IN STEP 6] Point balance mutation failed inside User Service layer!");
            System.out.println("   Details: " + userEx.getMessage());
            throw userEx;
        }

        // 🛑 --- STEP 7 DIAGNOSTICS: FIREBASE STATE CLOSURE ---
        System.out.println("\n🔒 [STEP 7 DIAGNOSTICS] Sending synchronization updates to change status token node to 'claimed'...");
        try {
            ref.child("status").setValueAsync("claimed");
            System.out.println("   🟢 [STEP 7 SUCCESS] Async claim update token successfully dispatched to Firebase reference.");
        } catch (Exception fbEx) {
            System.out.println("💥 [CRITICAL CRASH IN STEP 7] Firebase node status lock assignment failed!");
            System.out.println("   Details: " + fbEx.getMessage());
            throw fbEx;
        }

        System.out.println("\n🏁 [QR CLAIM END] Transaction finalized safely. Sending response body string.");
        System.out.println("========================================================\n");

        return "QR Verified! Processed " + plasticCount + " plastics and " + metalCount + " metals. +" + totalPoints + " Points!";
    }

    private void saveAutomatedRecord(User user, String itemType, int qty, int points) {
        System.out.println("     ↳ [Repository Trigger] Saving entry properties to `submissionRepository`: " + qty + "x " + itemType);
        RecyclingSubmission submission = new RecyclingSubmission();
        submission.setUser(user);
        submission.setItemType(itemType); 
        submission.setQuantity(qty);
        submission.setPointsEarned(points);
        submission.setStatus("APPROVED");
        submission.setLocation("Hostel Bin 1"); 
        submission.setNotes("Scanned via Campus Hardware Bin QR Code");
        submissionRepository.save(submission);
    }

    @Transactional
    public RecyclingSubmission submit(Long userId, SubmissionRequest request) {
        System.out.println("📝 [LOG] Internal manually typed submission dashboard handler triggered.");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        String inputType = request.getItemType() != null ? request.getItemType().trim() : "";
        String dbItemType;

        if (inputType.equalsIgnoreCase("BOTTLE") || inputType.equalsIgnoreCase(ITEM_PLASTIC)) {
            dbItemType = ITEM_PLASTIC;
        } else if (inputType.equalsIgnoreCase("CAN") || inputType.equalsIgnoreCase(ITEM_METAL)) {
            dbItemType = ITEM_METAL;
        } else {
            throw new IllegalArgumentException("Invalid item type '" + inputType + "'. Must be Plastic Bottle or Aluminum Can.");
        }

        int qty = Math.max(1, request.getQuantity());
        int points = qty * (dbItemType.equals(ITEM_PLASTIC) ? POINTS_PER_PLASTIC : POINTS_PER_METAL);

        RecyclingSubmission submission = new RecyclingSubmission();
        submission.setUser(user);
        submission.setItemType(dbItemType);
        submission.setQuantity(qty);
        submission.setPointsEarned(points);
        submission.setLocation(request.getLocation());
        submission.setNotes(request.getNotes());
        submission.setStatus("APPROVED");

        RecyclingSubmission saved = submissionRepository.save(submission);
        userService.addPoints(userId, points);

        return saved;
    }

    public List<RecyclingSubmission> getUserSubmissions(Long userId) {
        return submissionRepository.findByUserIdOrderBySubmittedAtDesc(userId);
    }

    public List<RecyclingSubmission> getAllSubmissions() {
        return submissionRepository.findAll();
    }

    public List<RecyclingSubmission> getRecentSubmissions() {
        return submissionRepository.findTop20ByOrderBySubmittedAtDesc();
    }

    public Map<String, Object> getCampusStats() {
        List<Object[]> itemStats = submissionRepository.getStatsByItemType();
        long totalUsers = userRepository.count();
        long totalSubmissions = submissionRepository.count();

        long totalBottles = 0, totalCans = 0, totalPoints = 0;
        for (Object[] row : itemStats) {
            String type = (String) row[0];
            long qty = ((Number) row[2]).longValue();

            if (ITEM_PLASTIC.equals(type)) {
                totalBottles = qty;
                totalPoints += (qty * POINTS_PER_PLASTIC);
            } else if (ITEM_METAL.equals(type)) {
                totalCans = qty;
                totalPoints += (qty * POINTS_PER_METAL);
            }
        }

        return Map.of(
            "totalSubmissions", totalSubmissions,
            "totalPoints", totalPoints,
            "totalBottles", totalBottles,
            "totalCans", totalCans,
            "totalUsers", totalUsers
        );
    }

    @Transactional
    public RecyclingSubmission review(Long submissionId, String status, Long reviewerId) {
        RecyclingSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found: " + submissionId));

        String normalised = status.toUpperCase();
        if (!List.of("APPROVED", "REJECTED").contains(normalised)) {
            throw new IllegalArgumentException("Status must be APPROVED or REJECTED");
        }

        boolean wasApproved = "APPROVED".equals(submission.getStatus());
        boolean nowApproved = "APPROVED".equals(normalised);

        if (!wasApproved && nowApproved) {
            userService.addPoints(submission.getUser().getId(), submission.getPointsEarned());
        } else if (wasApproved && !nowApproved) {
            userService.addPoints(submission.getUser().getId(), -submission.getPointsEarned());
        }

        submission.setStatus(normalised);
        submission.setReviewedAt(LocalDateTime.now());
        return submissionRepository.save(submission);
    }

    public Optional<RecyclingSubmission> findById(Long id) {
        return submissionRepository.findById(id);
    }

    public Map<String, Object> getItemInfo() {
        return Map.of(
            "items", List.of(
                Map.of("type", ITEM_PLASTIC, "pointsPerItem", POINTS_PER_PLASTIC, "description", "Plastic bottle"),
                Map.of("type", ITEM_METAL, "pointsPerItem", POINTS_PER_METAL, "description", "Aluminum can")
            ),
            "welcomeBonus", 20
        );
    }
}
