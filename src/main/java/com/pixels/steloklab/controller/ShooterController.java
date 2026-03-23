package com.pixels.steloklab.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pixels.steloklab.network.ServerConnection;
import com.pixels.steloklab.view.GamePane;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ShooterController implements Initializable {

    @FXML private VBox connectPanel;
    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private TextField usernameField;
    @FXML private Label connectErrorLabel;

    @FXML private BorderPane gameRoot;
    @FXML private Pane gamePaneContainer;
    @FXML private VBox playersPanel;

    @FXML private Button readyButton;
    @FXML private Button stopButton;
    @FXML private Button shootButton;
    @FXML private Button leaderboardButton;
    @FXML private Label statusLabel;

    private GamePane gamePane;
    private ServerConnection connection;
    private String myUsername;
    private String lastPhase = "WAITING";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gamePane = new GamePane();
        gamePaneContainer.getChildren().add(gamePane);
    }

    @FXML
    private void onConnect() {
        String host     = hostField.getText().trim();
        String portStr  = portField.getText().trim();
        String username = usernameField.getText().trim();

        connectErrorLabel.setText("");

        if (host.isEmpty() || portStr.isEmpty() || username.isEmpty()) {
            connectErrorLabel.setText("Заполните все поля.");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            connectErrorLabel.setText("Порт должен быть числом.");
            return;
        }

        myUsername = username;
        connection = new ServerConnection(this::handleServerMessage);

        final int finalPort = port;
        Thread connectThread = new Thread(() -> {
            try {
                connection.connect(host, finalPort, username);
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                    connectErrorLabel.setText("Не удалось подключиться: " + e.getMessage()));
            }
        }, "ConnectThread");
        connectThread.setDaemon(true);
        connectThread.start();
    }

    @FXML
    private void onReady() {
        if (connection != null) connection.sendReady();
    }

    @FXML
    private void onStop() {
        if (connection != null) connection.sendPause();
    }

    @FXML
    private void onShoot() {
        if (connection != null) connection.sendShoot();
    }

    @FXML
    private void onLeaderboard() {
        if (connection != null) connection.sendLeaderboard();
    }

    private void handleServerMessage(JsonObject msg) {
        String type = msg.has("type") ? msg.get("type").getAsString() : "";

        switch (type) {
            case "JOIN_OK"    -> switchToGame();
            case "JOIN_ERROR" -> {
                String reason = msg.has("reason") ? msg.get("reason").getAsString() : "";
                connectErrorLabel.setText(reason);
                connection = null;
            }
            case "STATE"       -> handleState(msg);
            case "LEADERBOARD" -> showLeaderboard(msg);
            case "DISCONNECTED"-> onDisconnected();
        }
    }

    private void switchToGame() {
        connectPanel.setVisible(false);
        gameRoot.setVisible(true);
        statusLabel.setText("Ожидание игроков...");
    }

    private void handleState(JsonObject msg) {
        String phase  = msg.has("phase")  ? msg.get("phase").getAsString()  : "WAITING";
        double nearY  = msg.has("nearY")  ? msg.get("nearY").getAsDouble()  : 0;
        double farY   = msg.has("farY")   ? msg.get("farY").getAsDouble()   : 0;
        String winner = msg.has("winner") ? msg.get("winner").getAsString() : null;

        JsonArray playersArr = msg.has("players") ? msg.getAsJsonArray("players") : new JsonArray();
        List<GamePane.PlayerDto> players = new ArrayList<>();
        for (JsonElement el : playersArr) {
            JsonObject p = el.getAsJsonObject();
            players.add(new GamePane.PlayerDto(p.get("playerIndex").getAsInt()));
        }

        JsonArray arrowsArr = msg.has("arrows") ? msg.getAsJsonArray("arrows") : new JsonArray();
        List<GamePane.ArrowDto> arrows = new ArrayList<>();
        for (JsonElement el : arrowsArr) {
            JsonObject a = el.getAsJsonObject();
            arrows.add(new GamePane.ArrowDto(
                    a.get("playerIndex").getAsInt(),
                    a.get("x").getAsDouble(),
                    a.get("y").getAsDouble()));
        }

        gamePane.redrawServer(nearY, farY, arrows, players);
        updatePlayersPanel(playersArr);
        updateControls(phase, winner);

        if ("FINISHED".equals(phase) && !"FINISHED".equals(lastPhase) && winner != null) {
            showWinnerAlert(winner);
        }
        lastPhase = phase;
    }

    private void updateControls(String phase, String winner) {
        switch (phase) {
            case "WAITING" -> {
                readyButton.setDisable(false);
                stopButton.setDisable(true);
                shootButton.setDisable(true);
                statusLabel.setText("Ожидание игроков — нажмите «Готов»");
            }
            case "RUNNING" -> {
                readyButton.setDisable(true);
                stopButton.setDisable(false);
                shootButton.setDisable(false);
                statusLabel.setText("Игра идёт");
            }
            case "PAUSED" -> {
                readyButton.setDisable(false);
                stopButton.setDisable(true);
                shootButton.setDisable(true);
                statusLabel.setText("Пауза — нажмите «Готов» для продолжения");
            }
            case "FINISHED" -> {
                readyButton.setDisable(false);
                stopButton.setDisable(true);
                shootButton.setDisable(true);
                String msg = winner != null
                        ? (winner.equals(myUsername) ? "Вы победили! 🎉" : "Победитель: " + winner)
                        : "Игра завершена";
                statusLabel.setText(msg + " — нажмите «Готов» для новой игры");
            }
        }
    }

    private static final Color[] PLAYER_COLORS = {
        Color.DARKBLUE, Color.DARKGREEN, Color.DARKORANGE, Color.PURPLE
    };

    private void updatePlayersPanel(JsonArray playersArr) {
        playersPanel.getChildren().clear();

        Label header = new Label("Игроки");
        header.setFont(Font.font(null, FontWeight.BOLD, 13));
        playersPanel.getChildren().add(header);

        for (JsonElement el : playersArr) {
            JsonObject p  = el.getAsJsonObject();
            String name   = p.get("username").getAsString();
            int score     = p.get("score").getAsInt();
            int shots     = p.get("shots").getAsInt();
            int wins      = p.get("wins").getAsInt();
            boolean ready = p.get("ready").getAsBoolean();
            int idx       = p.get("playerIndex").getAsInt();

            VBox card = new VBox(2);
            card.setPadding(new Insets(6));
            card.setStyle("-fx-background-color: white; -fx-border-color: #ccc; "
                        + "-fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4;");

            Color c = PLAYER_COLORS[idx % PLAYER_COLORS.length];
            String hex = String.format("#%02X%02X%02X",
                    (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
            card.setStyle(card.getStyle()
                    + "-fx-border-left-width: 4; -fx-border-color: " + hex + " #ccc #ccc #ccc;");

            Label nameLabel = new Label("Игрок: " + name + (ready ? " ✓" : ""));
            nameLabel.setFont(Font.font(null, FontWeight.BOLD, 12));

            Label scoreLabel  = new Label("Счёт игрока: " + score);
            Label shotsLabel  = new Label("Выстрелов: " + shots);
            Label winsLabel   = new Label("Число побед: " + wins);

            card.getChildren().addAll(nameLabel, scoreLabel, shotsLabel, winsLabel);
            VBox.setMargin(card, new Insets(4, 0, 0, 0));
            playersPanel.getChildren().add(card);
        }
    }

    private void showLeaderboard(JsonObject msg) {
        JsonArray entries = msg.has("entries") ? msg.getAsJsonArray("entries") : new JsonArray();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Таблица лидеров");
        dialog.setHeaderText("Лучшие игроки (по числу побед)");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(6);
        content.setPadding(new Insets(10));

        HBox header = new HBox(10);
        Label hName = new Label("Имя игрока");
        hName.setFont(Font.font(null, FontWeight.BOLD, 12));
        hName.setMinWidth(160);
        Label hWins = new Label("Побед");
        hWins.setFont(Font.font(null, FontWeight.BOLD, 12));
        header.getChildren().addAll(hName, hWins);
        content.getChildren().add(header);

        if (entries.size() == 0) {
            content.getChildren().add(new Label("Нет данных."));
        } else {
            for (JsonElement el : entries) {
                JsonObject e = el.getAsJsonObject();
                HBox row = new HBox(10);
                Label rName = new Label(e.get("username").getAsString());
                rName.setMinWidth(160);
                Label rWins = new Label(String.valueOf(e.get("wins").getAsInt()));
                row.getChildren().addAll(rName, rWins);
                content.getChildren().add(row);
            }
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefSize(300, 260);
        dialog.getDialogPane().setContent(scroll);

        dialog.setOnHidden(e -> {
            if (connection != null) connection.sendReady();
        });

        dialog.showAndWait();
    }

    private void showWinnerAlert(String winner) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Игра завершена");
        if (winner.equals(myUsername)) {
            alert.setHeaderText("Вы победили!");
            alert.setContentText("Поздравляем! Нажмите «Готов» для новой игры.");
        } else {
            alert.setHeaderText("Победитель: " + winner);
            alert.setContentText("Нажмите «Готов» для новой игры.");
        }
        alert.showAndWait();
    }

    private void onDisconnected() {
        gameRoot.setVisible(false);
        connectPanel.setVisible(true);
        connectErrorLabel.setText("Соединение с сервером разорвано.");
        connection = null;
    }

    public void shutdown() {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }
}
