package com.campusrecycle.service;

import com.campusrecycle.dto.SubmissionRequest;
import com.campusrecycle.model.RecyclingSubmission;
import com.campusrecycle.model.User;
import com.campusrecycle.repository.RecyclingSubmissionRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RecyclingSubmissionServiceTest {

    @Mock private RecyclingSubmissionRepository submissionRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserService userService;

    @InjectMocks
    private RecyclingSubmissionService service;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setGithubId("gh1");
        testUser.setEmail("student@uni.edu");
        testUser.setName("Student");
        testUser.setPoints(20);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(submissionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userService.addPoints(anyLong(), anyInt())).thenReturn(testUser);
    }

    @Test
    void submitBottle_earns1PointPerItem() {
        SubmissionRequest req = new SubmissionRequest();
        req.setItemType("BOTTLE");
        req.setQuantity(3);

        RecyclingSubmission result = service.submit(1L, req);

        assertEquals("BOTTLE", result.getItemType());
        assertEquals(3, result.getQuantity());
        assertEquals(3, result.getPointsEarned());
        assertEquals("APPROVED", result.getStatus());
        verify(userService).addPoints(1L, 3);
    }

    @Test
    void submitCan_earns1PointPerItem() {
        SubmissionRequest req = new SubmissionRequest();
        req.setItemType("CAN");
        req.setQuantity(10);

        RecyclingSubmission result = service.submit(1L, req);

        assertEquals("CAN", result.getItemType());
        assertEquals(10, result.getPointsEarned());
        verify(userService).addPoints(1L, 10);
    }

    @Test
    void submitLowercase_isNormalised() {
        SubmissionRequest req = new SubmissionRequest();
        req.setItemType("bottle");
        req.setQuantity(1);

        RecyclingSubmission result = service.submit(1L, req);
        assertEquals("BOTTLE", result.getItemType());
    }

    @Test
    void submitInvalidItem_throwsException() {
        SubmissionRequest req = new SubmissionRequest();
        req.setItemType("PLASTIC");
        req.setQuantity(1);

        assertThrows(IllegalArgumentException.class, () -> service.submit(1L, req));
        verify(submissionRepository, never()).save(any());
    }

    @Test
    void submitZeroQuantity_defaultsToOne() {
        SubmissionRequest req = new SubmissionRequest();
        req.setItemType("CAN");
        req.setQuantity(0);

        RecyclingSubmission result = service.submit(1L, req);
        assertEquals(1, result.getQuantity());
        assertEquals(1, result.getPointsEarned());
    }

    @Test
    void getItemInfo_returnsBottleAndCan() {
        var info = service.getItemInfo();
        assertTrue(info.containsKey("items"));
        assertEquals(20, info.get("welcomeBonus"));
    }
}
