package com.pixels.steloklab.task;

import com.pixels.steloklab.model.GameModel;
import com.pixels.steloklab.model.GameState;
import com.pixels.steloklab.model.Target;
import com.pixels.steloklab.view.GamePane;
import javafx.application.Platform;

public class TargetsMovementTask implements Runnable {
    private static final long TICK_MS = 25;

    private final GameModel model;
    private final GamePane gamePane;
    private volatile boolean running = true;
    private double nearDirection = 1;
    private double farDirection = 1;

    public TargetsMovementTask(GameModel model, GamePane gamePane) {
        this.model = model;
        this.gamePane = gamePane;
    }

    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            GameState state = model.getState();
            if (state == GameState.STOPPED) {
                running = false;
                break;
            }
            if (state == GameState.RUNNING) {
                Target near = model.getNearTarget();
                Target far = model.getFarTarget();
                double ny = near.getY() + near.getSpeed() * nearDirection;
                double fy = far.getY() + far.getSpeed() * farDirection;
                if (ny <= 0) { ny = 0; nearDirection = 1; }
                if (ny + near.getHeight() >= GameModel.GAME_HEIGHT) { ny = GameModel.GAME_HEIGHT - near.getHeight(); nearDirection = -1; }
                near.setY(ny);

                if (fy <= 0) { fy = 0; farDirection = 1; }
                if (fy + far.getHeight() >= GameModel.GAME_HEIGHT) { fy = GameModel.GAME_HEIGHT - far.getHeight(); farDirection = -1; }
                far.setY(fy);

                Platform.runLater(() -> gamePane.redraw());
            }
            try {
                Thread.sleep(TICK_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
                break;
            }
        }
    }
}
