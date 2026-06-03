package com.campusrecycle.util;

public class LevelUtils {

    public record LevelInfo(int level, String title, int currentPoints, int nextLevelPoints, int progressPercent) {}

    public static LevelInfo getLevel(int points) {
        int level;
        String title;
        int nextThreshold;

        if (points < 50) {
            level = 1; title = "Sapling"; nextThreshold = 50;
        } else if (points < 150) {
            level = 2; title = "Sprout"; nextThreshold = 150;
        } else if (points < 350) {
            level = 3; title = "Plant"; nextThreshold = 350;
        } else if (points < 700) {
            level = 4; title = "Tree"; nextThreshold = 700;
        } else {
            level = 5; title = "Legendary Tree"; nextThreshold = -1;
        }

        int prevThreshold = switch (level) {
            case 1 -> 0;
            case 2 -> 50;
            case 3 -> 150;
            case 4 -> 350;
            default -> 700;
        };

        int progress;
        if (nextThreshold == -1) {
            progress = 100;
        } else {
            int range = nextThreshold - prevThreshold;
            int earned = points - prevThreshold;
            progress = (int) ((earned / (double) range) * 100);
        }

        return new LevelInfo(level, title, points, nextThreshold, progress);
    }
}
