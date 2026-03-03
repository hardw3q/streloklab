package com.pixels.steloklab.model;

public class GameModel {
    public static final double GAME_WIDTH = 700;
    public static final double GAME_HEIGHT = 400;

    public static final double PLAYER_X = 40;
    public static final double PLAYER_SIZE = 30;

    public static final double ARROW_SPEED = 12;
    public static final double ARROW_LENGTH = 20;
    public static final double ARROW_HEIGHT = 4;

    public static final double NEAR_TARGET_X = GAME_WIDTH * 0.55;
    public static final double FAR_TARGET_X = GAME_WIDTH * 0.85;
    public static final double NEAR_TARGET_SIZE = 50;
    public static final double NEAR_TARGET_SPEED = 3;

    private volatile GameState state = GameState.STOPPED;
    private int score;
    private int shotsCount;

    private final Target nearTarget;
    private final Target farTarget;
    private final Arrow arrow;

    public GameModel() {
        nearTarget = new Target(Target.Type.NEAR, NEAR_TARGET_X, 0, NEAR_TARGET_SIZE, NEAR_TARGET_SPEED);
        farTarget = new Target(Target.Type.FAR, FAR_TARGET_X, 0, NEAR_TARGET_SIZE, NEAR_TARGET_SPEED);
        arrow = new Arrow();
    }

    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public void addScore(int points) { this.score += points; }

    public int getShotsCount() { return shotsCount; }
    public void setShotsCount(int shotsCount) { this.shotsCount = shotsCount; }
    public void incrementShots() { this.shotsCount++; }

    public Target getNearTarget() { return nearTarget; }
    public Target getFarTarget() { return farTarget; }
    public Arrow getArrow() { return arrow; }

    public void resetScoreAndShots() {
        score = 0;
        shotsCount = 0;
    }

    public void resetTargetsToCenter() {
        double cy = GAME_HEIGHT / 2 - NEAR_TARGET_SIZE / 2;
        nearTarget.setY(cy);
        cy = GAME_HEIGHT / 2 - (NEAR_TARGET_SIZE / 2) / 2;
        farTarget.setY(cy);
    }

    public void hideArrow() {
        arrow.setVisible(false);
    }

    public void placeArrowAtPlayer() {
        arrow.setX(PLAYER_X + PLAYER_SIZE);
        arrow.setY(GAME_HEIGHT / 2 - ARROW_HEIGHT / 2);
        arrow.setVisible(true);
    }
}
