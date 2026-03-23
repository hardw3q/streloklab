package com.pixels.steloklab.server;

import com.pixels.steloklab.model.GameModel;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerGameState {

    public enum Phase { WAITING, RUNNING, PAUSED, FINISHED }

    private static final double GAME_W     = GameModel.GAME_WIDTH;
    private static final double GAME_H     = GameModel.GAME_HEIGHT;
    private static final double NEAR_X     = GameModel.NEAR_TARGET_X;
    private static final double FAR_X      = GameModel.FAR_TARGET_X;
    private static final double NEAR_SIZE  = GameModel.NEAR_TARGET_SIZE;
    private static final double FAR_SIZE   = NEAR_SIZE / 2.0;
    private static final double NEAR_SPEED = GameModel.NEAR_TARGET_SPEED;
    private static final double FAR_SPEED  = NEAR_SPEED * 2.0;
    private static final double ARROW_SPD  = GameModel.ARROW_SPEED;
    private static final double ARROW_W    = GameModel.ARROW_LENGTH;
    private static final double ARROW_H    = GameModel.ARROW_HEIGHT;
    private static final double PLAYER_X   = GameModel.PLAYER_X;
    private static final double PLAYER_SZ  = GameModel.PLAYER_SIZE;
    static final int WIN_SCORE  = 6;
    static final int MAX_PLAYERS = 4;

    private volatile Phase phase = Phase.WAITING;

    private double nearY    = GAME_H / 2 - NEAR_SIZE / 2;
    private double farY     = GAME_H / 2 - FAR_SIZE / 2;
    private double nearDir  = 1;
    private double farDir   = 1;

    private String lastWinner = null;

    private final List<PlayerInfo> players = new CopyOnWriteArrayList<>();

    public Phase getPhase()        { return phase; }
    public double getNearY()       { return nearY; }
    public double getFarY()        { return farY; }
    public String getLastWinner()  { return lastWinner; }
    public List<PlayerInfo> getPlayers() { return players; }

    public synchronized boolean addPlayer(PlayerInfo player) {
        if (players.size() >= MAX_PLAYERS) return false;
        players.add(player);
        return true;
    }

    public synchronized void removePlayer(PlayerInfo player) {
        players.remove(player);
        if (players.isEmpty()) {
            phase = Phase.WAITING;
            lastWinner = null;
        }
    }

    public synchronized void playerReady(PlayerInfo player) {
        player.ready = true;
        if ((phase == Phase.WAITING || phase == Phase.FINISHED || phase == Phase.PAUSED)
                && !players.isEmpty()
                && players.stream().allMatch(p -> p.ready)) {
            startGame();
        }
    }

    public synchronized void playerPause(PlayerInfo player) {
        if (phase == Phase.RUNNING) {
            phase = Phase.PAUSED;
            for (PlayerInfo p : players) p.ready = false;
        }
    }

    public synchronized boolean playerShoot(PlayerInfo player) {
        if (phase != Phase.RUNNING) return false;
        if (player.arrowVisible) return false;
        double startX = PLAYER_X + PLAYER_SZ;
        double startY = PlayerInfo.PLAYER_Y[player.playerIndex] + PLAYER_SZ / 2.0 - ARROW_H / 2.0;
        player.arrowX = startX;
        player.arrowY = startY;
        player.arrowVisible = true;
        player.shots++;
        return true;
    }

    public synchronized String tick() {
        if (phase != Phase.RUNNING) return null;

        nearY += NEAR_SPEED * nearDir;
        if (nearY <= 0)               { nearY = 0;              nearDir = 1; }
        if (nearY + NEAR_SIZE >= GAME_H) { nearY = GAME_H - NEAR_SIZE; nearDir = -1; }

        farY += FAR_SPEED * farDir;
        if (farY <= 0)             { farY = 0;             farDir = 1; }
        if (farY + FAR_SIZE >= GAME_H) { farY = GAME_H - FAR_SIZE; farDir = -1; }

        for (PlayerInfo p : players) {
            if (!p.arrowVisible) continue;
            p.arrowX += ARROW_SPD;

            if (hit(p.arrowX, p.arrowY, NEAR_X, nearY, NEAR_SIZE, NEAR_SIZE)) {
                p.arrowVisible = false;
                p.score += 1;
            } else if (hit(p.arrowX, p.arrowY, FAR_X, farY, FAR_SIZE, FAR_SIZE)) {
                p.arrowVisible = false;
                p.score += 2;
            } else if (p.arrowX >= GAME_W) {
                p.arrowVisible = false;
            }

            if (p.score >= WIN_SCORE) {
                phase = Phase.FINISHED;
                lastWinner = p.username;
                p.wins++;
                return p.username;
            }
        }
        return null;
    }

    private void startGame() {
        for (PlayerInfo p : players) p.resetSession();
        nearY   = GAME_H / 2 - NEAR_SIZE / 2;
        farY    = GAME_H / 2 - FAR_SIZE / 2;
        nearDir = 1;
        farDir  = 1;
        lastWinner = null;
        phase = Phase.RUNNING;
    }

    private boolean hit(double ax, double ay,
                        double tx, double ty, double tw, double th) {
        return ax < tx + tw && ax + ARROW_W > tx &&
               ay < ty + th && ay + ARROW_H > ty;
    }
}
