package com.pixels.steloklab.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pixels.steloklab.db.PlayerDao;
import com.pixels.steloklab.db.PlayerEntity;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {

    private static final Gson GSON = new Gson();

    private final Socket socket;
    private final ServerGameState gameState;
    private final PlayerDao playerDao;

    private PrintWriter out;
    private final Object writeLock = new Object();

    PlayerInfo playerInfo;

    public ClientHandler(Socket socket, ServerGameState gameState, PlayerDao playerDao) {
        this.socket = socket;
        this.gameState = gameState;
        this.playerDao = playerDao;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), "UTF-8"));
             PrintWriter writer = new PrintWriter(
                     new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true)) {

            synchronized (writeLock) { this.out = writer; }

            String line = in.readLine();
            if (line == null) return;

            JsonObject msg = GSON.fromJson(line, JsonObject.class);
            if (!"JOIN".equals(getType(msg))) return;

            String username = msg.has("username")
                    ? msg.get("username").getAsString().trim() : "";

            if (username.isEmpty()) {
                sendError("Имя пользователя не может быть пустым");
                return;
            }

            boolean taken = gameState.getPlayers().stream()
                    .anyMatch(p -> p.username.equalsIgnoreCase(username));
            if (taken) {
                sendError("Имя пользователя уже занято");
                return;
            }

            int index = gameState.getPlayers().size();
            playerInfo = new PlayerInfo(username, index);

            if (playerDao != null) {
                try {
                    PlayerEntity entity = playerDao.findByUsername(username);
                    if (entity == null) {
                        playerDao.save(new PlayerEntity(username));
                        playerInfo.wins = 0;
                    } else {
                        playerInfo.wins = entity.getWins();
                    }
                } catch (Exception e) {
                    System.err.println("Предупреждение: не удалось загрузить данные игрока из БД: " + e.getMessage());
                }
            }

            if (!gameState.addPlayer(playerInfo)) {
                sendError("Сервер заполнен (максимум " + ServerGameState.MAX_PLAYERS + " игрока)");
                return;
            }

            JsonObject ok = new JsonObject();
            ok.addProperty("type", "JOIN_OK");
            sendMessage(GSON.toJson(ok));
            System.out.println("[Server] Подключился: " + username);

            while ((line = in.readLine()) != null) {
                try {
                    handleMessage(GSON.fromJson(line, JsonObject.class));
                } catch (Exception e) {
                    System.err.println("[Server] Ошибка обработки сообщения: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("[Server] Клиент отключился: "
                    + (playerInfo != null ? playerInfo.username : "неизвестен"));
        } finally {
            if (playerInfo != null) {
                gameState.removePlayer(playerInfo);
                System.out.println("[Server] Отключился: " + playerInfo.username);
            }
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private void handleMessage(JsonObject msg) {
        switch (getType(msg)) {
            case "READY"       -> gameState.playerReady(playerInfo);
            case "PAUSE"       -> gameState.playerPause(playerInfo);
            case "SHOOT"       -> gameState.playerShoot(playerInfo);
            case "LEADERBOARD" -> {
                gameState.playerPause(playerInfo);
                sendLeaderboard();
            }
        }
    }

    private void sendLeaderboard() {
        JsonObject resp = new JsonObject();
        resp.addProperty("type", "LEADERBOARD");
        JsonArray entries = new JsonArray();

        if (playerDao != null) {
            try {
                List<PlayerEntity> all = playerDao.findAll();
                for (PlayerEntity p : all) {
                    JsonObject e = new JsonObject();
                    e.addProperty("username", p.getUsername());
                    e.addProperty("wins", p.getWins());
                    entries.add(e);
                }
            } catch (Exception ex) {
                System.err.println("[Server] Ошибка чтения лидеров из БД: " + ex.getMessage());
            }
        } else {
            for (PlayerInfo p : gameState.getPlayers()) {
                JsonObject e = new JsonObject();
                e.addProperty("username", p.username);
                e.addProperty("wins", p.wins);
                entries.add(e);
            }
        }
        resp.add("entries", entries);
        sendMessage(GSON.toJson(resp));
    }

    public void sendMessage(String json) {
        synchronized (writeLock) {
            if (out != null) {
                out.println(json);
            }
        }
    }

    private void sendError(String reason) {
        JsonObject err = new JsonObject();
        err.addProperty("type", "JOIN_ERROR");
        err.addProperty("reason", reason);
        sendMessage(GSON.toJson(err));
    }

    private String getType(JsonObject msg) {
        return msg.has("type") ? msg.get("type").getAsString() : "";
    }
}
