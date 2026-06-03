package com.campusrecycle.service;

import com.campusrecycle.model.User;
import com.campusrecycle.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void newUser_receives20WelcomeBonus() {
        when(userRepository.findByGithubId("gh123")).thenReturn(Optional.empty());

        User user = userService.findOrCreateUser("gh123", "test@example.com", "Test User", null);

        assertEquals(20, user.getPoints());
        assertEquals("STUDENT", user.getRole());
    }

    @Test
    void existingUser_doesNotReceiveWelcomeBonus() {
        User existing = new User();
        existing.setGithubId("gh123");
        existing.setEmail("test@example.com");
        existing.setName("Test User");
        existing.setPoints(50);

        when(userRepository.findByGithubId("gh123")).thenReturn(Optional.of(existing));

        User user = userService.findOrCreateUser("gh123", "test@example.com", "Test User", null);

        assertEquals(50, user.getPoints());
    }

    @Test
    void addPoints_increasesBalance() {
        User user = new User();
        user.setPoints(20);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.addPoints(1L, 5);

        assertEquals(25, user.getPoints());
    }

    @Test
    void addPoints_cannotGoBelowZero() {
        User user = new User();
        user.setPoints(3);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.addPoints(1L, -10);

        assertEquals(0, user.getPoints());
    }

    @Test
    void findById_notFound_returnsEmpty() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertTrue(userService.findById(999L).isEmpty());
    }
}
