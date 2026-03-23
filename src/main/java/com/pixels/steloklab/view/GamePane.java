package com.pixels.steloklab.view;

import com.pixels.steloklab.model.Arrow;
import com.pixels.steloklab.model.GameModel;
import com.pixels.steloklab.model.Target;
import com.pixels.steloklab.server.PlayerInfo;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.List;

public class GamePane extends Pane {

    private static final Color[] PLAYER_COLORS = {
        Color.DARKBLUE, Color.DARKGREEN, Color.DARKORANGE, Color.PURPLE
    };

    private final Canvas canvas;
    private final GraphicsContext gc;
    private GameModel model;

    public record ArrowDto(int playerIndex, double x, double y) {}
    public record PlayerDto(int playerIndex) {}

    public GamePane() {
        canvas = new Canvas(GameModel.GAME_WIDTH, GameModel.GAME_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);
        setMinSize(GameModel.GAME_WIDTH, GameModel.GAME_HEIGHT);
        setMaxSize(GameModel.GAME_WIDTH, GameModel.GAME_HEIGHT);
    }

    public void setModel(GameModel model) {
        this.model = model;
    }

    public void redraw() {
        if (model == null) return;
        gc.clearRect(0, 0, GameModel.GAME_WIDTH, GameModel.GAME_HEIGHT);
        drawBackground();

        Target near = model.getNearTarget();
        Target far  = model.getFarTarget();
        drawGuideLines(near.getX() + near.getWidth() / 2, far.getX() + far.getWidth() / 2);
        drawNearTarget(near.getX(), near.getY(), near.getWidth(), near.getHeight());
        drawFarTarget(far.getX(), far.getY(), far.getWidth(), far.getHeight());

        double py = GameModel.GAME_HEIGHT / 2 - GameModel.PLAYER_SIZE / 2;
        drawPlayer(GameModel.PLAYER_X, py, 0);

        Arrow arrow = model.getArrow();
        if (arrow.isVisible()) {
            drawArrow(arrow.getX(), arrow.getY(), 0);
        }
    }

    public void redrawServer(double nearY, double farY,
                             List<ArrowDto> arrows, List<PlayerDto> players) {
        double nearSize = GameModel.NEAR_TARGET_SIZE;
        double farSize  = nearSize / 2.0;
        double nearX    = GameModel.NEAR_TARGET_X;
        double farX     = GameModel.FAR_TARGET_X;

        gc.clearRect(0, 0, GameModel.GAME_WIDTH, GameModel.GAME_HEIGHT);
        drawBackground();
        drawGuideLines(nearX + nearSize / 2, farX + farSize / 2);
        drawNearTarget(nearX, nearY, nearSize, nearSize);
        drawFarTarget(farX, farY, farSize, farSize);

        for (PlayerDto p : players) {
            double py = PlayerInfo.PLAYER_Y[p.playerIndex()];
            drawPlayer(GameModel.PLAYER_X, py, p.playerIndex());
        }

        for (ArrowDto a : arrows) {
            drawArrow(a.x(), a.y(), a.playerIndex());
        }
    }

    private void drawBackground() {
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, GameModel.GAME_WIDTH, GameModel.GAME_HEIGHT);
    }

    private void drawGuideLines(double nearCx, double farCx) {
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(2);
        gc.strokeLine(nearCx, 0, nearCx, GameModel.GAME_HEIGHT);
        gc.strokeLine(farCx,  0, farCx,  GameModel.GAME_HEIGHT);
    }

    private void drawNearTarget(double x, double y, double w, double h) {
        gc.setFill(Color.RED);
        gc.fillRect(x, y, w, h);
    }

    private void drawFarTarget(double x, double y, double w, double h) {
        gc.setFill(Color.DARKRED);
        gc.fillRect(x, y, w, h);
    }

    private void drawPlayer(double x, double y, int index) {
        gc.setFill(playerColor(index));
        gc.fillOval(x, y, GameModel.PLAYER_SIZE, GameModel.PLAYER_SIZE);
    }

    private void drawArrow(double x, double y, int index) {
        gc.setFill(playerColor(index));
        gc.fillRect(x, y, GameModel.ARROW_LENGTH, GameModel.ARROW_HEIGHT);
    }

    private Color playerColor(int index) {
        return PLAYER_COLORS[index % PLAYER_COLORS.length];
    }
}
