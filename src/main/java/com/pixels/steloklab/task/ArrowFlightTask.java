package com.pixels.steloklab.task;

import com.pixels.steloklab.model.GameModel;
import com.pixels.steloklab.model.GameState;
import com.pixels.steloklab.model.HitResult;
import com.pixels.steloklab.service.CollisionDetector;
import com.pixels.steloklab.view.GamePane;
import javafx.application.Platform;

public class ArrowFlightTask implements Runnable {
    private static final long TICK_MS = 20;

    private final GameModel model;
    private final GamePane gamePane;
    private final Runnable onScoreUpdate;

    public ArrowFlightTask(GameModel model, GamePane gamePane, Runnable onScoreUpdate) {
        this.model = model;
        this.gamePane = gamePane;
        this.onScoreUpdate = onScoreUpdate;
    }

    @Override
    public void run() {
        while (model.getArrow().isVisible()) {
            if (model.getState() == GameState.PAUSED) {
                try { Thread.sleep(TICK_MS); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                continue;
            }
            if (model.getState() == GameState.STOPPED) {
                model.hideArrow();
                Platform.runLater(() -> { gamePane.redraw(); if (onScoreUpdate != null) onScoreUpdate.run(); });
                break;
            }
            model.getArrow().setX(model.getArrow().getX() + GameModel.ARROW_SPEED);
            HitResult hit = CollisionDetector.checkHit(model.getArrow(), model.getNearTarget(), model.getFarTarget());
            if (hit == HitResult.NEAR_HIT) {
                model.addScore(1);
                model.hideArrow();
                Platform.runLater(() -> { gamePane.redraw(); if (onScoreUpdate != null) onScoreUpdate.run(); });
                break;
            }
            if (hit == HitResult.FAR_HIT) {
                model.addScore(2);
                model.hideArrow();
                Platform.runLater(() -> { gamePane.redraw(); if (onScoreUpdate != null) onScoreUpdate.run(); });
                break;
            }
            if (model.getArrow().getX() >= GameModel.GAME_WIDTH) {
                model.hideArrow();
                Platform.runLater(() -> { gamePane.redraw(); if (onScoreUpdate != null) onScoreUpdate.run(); });
                break;
            }
            Platform.runLater(() -> gamePane.redraw());
            try {
                Thread.sleep(TICK_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
