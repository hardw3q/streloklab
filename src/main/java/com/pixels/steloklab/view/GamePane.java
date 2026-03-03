package com.pixels.steloklab.view;

import com.pixels.steloklab.model.Arrow;
import com.pixels.steloklab.model.GameModel;
import com.pixels.steloklab.model.Target;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class GamePane extends Pane {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private GameModel model;

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

        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, GameModel.GAME_WIDTH, GameModel.GAME_HEIGHT);

        double px = GameModel.PLAYER_X;
        double py = GameModel.GAME_HEIGHT / 2 - GameModel.PLAYER_SIZE / 2;
        gc.setFill(Color.DARKBLUE);
        gc.fillOval(px, py, GameModel.PLAYER_SIZE, GameModel.PLAYER_SIZE);

        Target near = model.getNearTarget();
        Target far = model.getFarTarget();
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(2);
        gc.strokeLine(near.getX() + near.getWidth() / 2, 0, near.getX() + near.getWidth() / 2, GameModel.GAME_HEIGHT);
        gc.strokeLine(far.getX() + far.getWidth() / 2, 0, far.getX() + far.getWidth() / 2, GameModel.GAME_HEIGHT);

        gc.setFill(Color.RED);
        gc.fillRect(near.getX(), near.getY(), near.getWidth(), near.getHeight());
        gc.setFill(Color.DARKRED);
        gc.fillRect(far.getX(), far.getY(), far.getWidth(), far.getHeight());

        Arrow arrow = model.getArrow();
        if (arrow.isVisible()) {
            gc.setFill(Color.SADDLEBROWN);
            gc.fillRect(arrow.getX(), arrow.getY(), GameModel.ARROW_LENGTH, GameModel.ARROW_HEIGHT);
        }
    }
}
