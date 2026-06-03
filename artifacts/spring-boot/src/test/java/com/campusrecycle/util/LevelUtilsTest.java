package com.campusrecycle.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LevelUtilsTest {

    @Test
    void newUser_with20WelcomePoints_isSapling() {
        var info = LevelUtils.getLevel(20);
        assertEquals(1, info.level());
        assertEquals("Sapling", info.title());
        assertEquals(50, info.nextLevelPoints());
    }

    @Test
    void exactly0Points_isLevel1() {
        var info = LevelUtils.getLevel(0);
        assertEquals(1, info.level());
        assertEquals("Sapling", info.title());
        assertEquals(0, info.progressPercent());
    }

    @Test
    void exactly50Points_isLevel2Sprout() {
        var info = LevelUtils.getLevel(50);
        assertEquals(2, info.level());
        assertEquals("Sprout", info.title());
    }

    @Test
    void exactly150Points_isLevel3Plant() {
        var info = LevelUtils.getLevel(150);
        assertEquals(3, info.level());
        assertEquals("Plant", info.title());
    }

    @Test
    void exactly350Points_isLevel4Tree() {
        var info = LevelUtils.getLevel(350);
        assertEquals(4, info.level());
        assertEquals("Tree", info.title());
    }

    @Test
    void exactly700Points_isLevel5LegendaryTree() {
        var info = LevelUtils.getLevel(700);
        assertEquals(5, info.level());
        assertEquals("Legendary Tree", info.title());
        assertEquals(100, info.progressPercent());
        assertEquals(-1, info.nextLevelPoints());
    }

    @Test
    void highPoints_stayAtLevel5() {
        var info = LevelUtils.getLevel(99999);
        assertEquals(5, info.level());
        assertEquals("Legendary Tree", info.title());
        assertEquals(100, info.progressPercent());
    }

    @Test
    void progressWithin_level1_isCorrect() {
        // 25 of 50 points = 50%
        var info = LevelUtils.getLevel(25);
        assertEquals(50, info.progressPercent());
    }

    @Test
    void progressWithin_level2_isCorrect() {
        // 50 = start of level 2, next threshold 150, range 100
        // 100 points = 50/100 = 50%
        var info = LevelUtils.getLevel(100);
        assertEquals(2, info.level());
        assertEquals(50, info.progressPercent());
    }
}
