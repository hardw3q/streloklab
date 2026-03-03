package com.pixels.steloklab.controller;

import com.pixels.steloklab.model.GameModel;
import com.pixels.steloklab.model.GameState;
import com.pixels.steloklab.task.ArrowFlightTask;
import com.pixels.steloklab.task.TargetsMovementTask;
import com.pixels.steloklab.view.GamePane;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class ShooterController implements Initializable {

    @FXML private Label scoreLabel;
    @FXML private Label shotsLabel;
    @FXML private Button pauseButton;
    @FXML private Button shootButton;
    @FXML private Pane gamePaneContainer;

    private GameModel model;
    private GamePane gamePane;
    private Thread targetsThread;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        model = new GameModel();
        gamePane = new GamePane();
        gamePane.setModel(model);
        gamePaneContainer.getChildren().add(gamePane);
        updateLabels();
        updateButtons();
        gamePane.redraw();
    }

    private void updateLabels() {
        scoreLabel.setText(String.valueOf(model.getScore()));
        shotsLabel.setText(String.valueOf(model.getShotsCount()));
    }

    private void updateButtons() {
        boolean running = model.getState() == GameState.RUNNING;
        boolean paused = model.getState() == GameState.PAUSED;
        boolean arrowInFlight = model.getArrow().isVisible();
        shootButton.setDisable(!running || paused || arrowInFlight);
        pauseButton.setDisable(model.getState() == GameState.STOPPED);
        pauseButton.setText(paused ? "Продолжить" : "Пауза");
    }

    @FXML
    private void onStart() {
        if (model.getState() == GameState.RUNNING) return;
        if (model.getState() == GameState.STOPPED) {
            model.resetScoreAndShots();
            model.resetTargetsToCenter();
            updateLabels();
        }
        model.setState(GameState.RUNNING);
        updateButtons();
        gamePane.redraw();
        if (targetsThread == null || !targetsThread.isAlive()) {
            TargetsMovementTask task = new TargetsMovementTask(model, gamePane);
            targetsThread = new Thread(task);
            targetsThread.setDaemon(true);
            targetsThread.start();
        }
    }

    @FXML
    private void onStop() {
        model.setState(GameState.STOPPED);
        if (targetsThread != null) {
            targetsThread.interrupt();
            targetsThread = null;
        }
        updateButtons();
        gamePane.redraw();
    }

    @FXML
    private void onPause() {
        if (model.getState() == GameState.STOPPED) return;
        if (model.getState() == GameState.PAUSED) {
            model.setState(GameState.RUNNING);
        } else {
            model.setState(GameState.PAUSED);
        }
        updateButtons();
    }

    @FXML
    private void onShoot() {
        if (model.getState() != GameState.RUNNING || model.getState() == GameState.PAUSED) return;
        if (model.getArrow().isVisible()) return;
        model.placeArrowAtPlayer();
        model.incrementShots();
        updateLabels();
        updateButtons();
        Runnable onScoreUpdate = () -> Platform.runLater(() -> { updateLabels(); updateButtons(); });
        ArrowFlightTask arrowTask = new ArrowFlightTask(model, gamePane, onScoreUpdate);
        Thread arrowThread = new Thread(arrowTask);
        arrowThread.setDaemon(true);
        arrowThread.start();
    }
}
