    package com.campusrecycle.service;

    import com.campusrecycle.model.User;
    import com.campusrecycle.repository.UserRepository;
    import com.campusrecycle.util.LevelUtils;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.time.LocalDateTime;
    import java.util.List;
    import java.util.Optional;

    @Service
    public class UserService {

        private static final int WELCOME_BONUS = 20;

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;

        public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
        }

        @Transactional
        public User registerUser(String email, String password, String name, String usn) {
            if (userRepository.existsByEmail(email)) {
                throw new RuntimeException("Email already in use");
            }

            if (usn != null && !usn.trim().isEmpty() && userRepository.existsByUsn(usn)) {
                throw new RuntimeException("USN already in use");
            }

            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setName(name);
            user.setUsn(usn); 
            user.setPoints(WELCOME_BONUS);
            user.setRole("USER"); 
            user.setCreatedAt(LocalDateTime.now());

            // ✨ ADDED: Sync the baseline Level Title ("Sapling") immediately upon registration!
            user.setAvatarUrl(LevelUtils.getLevel(WELCOME_BONUS).title());

            return userRepository.save(user);
        }

        public Optional<User> findByEmail(String email) {
            return userRepository.findByEmail(email);
        }

        @Transactional
        public User findOrCreateUser(String githubId, String email, String name, String avatarUrl) {
            Optional<User> existing = userRepository.findByGithubId(githubId);

            if (existing.isPresent()) {
                User user = existing.get();
                user.setName(name);

                // Sync dynamic level titles on GitHub login if they have point history
                LevelUtils.LevelInfo info = LevelUtils.getLevel(user.getPoints());
                user.setAvatarUrl(info.title());

                user.setLastLoginAt(LocalDateTime.now());
                return userRepository.save(user);
            }

            User user = new User();
            user.setGithubId(githubId);
            user.setEmail(email);
            user.setName(name);
            user.setPoints(WELCOME_BONUS);
            user.setAvatarUrl(LevelUtils.getLevel(WELCOME_BONUS).title()); // Set default level title
            user.setRole("USER");
            user.setCreatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }

        public Optional<User> findById(Long id) {
            return userRepository.findById(id);
        }

        public List<User> findAllUsers() {
            return userRepository.findAll();
        }

        @Transactional
        public User addPoints(Long userId, int pointsToAdd) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 1. Add the incoming points to their current total
            int newPoints = user.getPoints() + pointsToAdd;
            user.setPoints(newPoints);

            // 2. 🔥 CALCULATE NEW LEVEL TITLE AND SYNC IT TO THE DATABASE COLUMN
            user.setAvatarUrl(LevelUtils.getLevel(newPoints).title()); 

            // 3. Save the updated user row back to the database
            return userRepository.save(user);
        }
    }