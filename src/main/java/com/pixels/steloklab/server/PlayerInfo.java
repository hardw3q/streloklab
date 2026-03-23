package com.pixels.steloklab.server;

public class PlayerInfo {

    public static final double[] PLAYER_Y = { 70.0, 180.0, 280.0, 360.0 };

    final String username;
    final int playerIndex;

    volatile int score;
    volatile int shots;
    volatile boolean ready;

    volatile int wins;

    volatile boolean arrowVisible;
    volatile double arrowX;
    volatile double arrowY;

    public PlayerInfo(String username, int playerIndex) {
        this.username = username;
        this.playerIndex = playerIndex;
    }

    void resetSession() {
        score = 0;
        shots = 0;
        ready = false;
        arrowVisible = false;
    }
}
