package com.pixels.steloklab.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.pixels.steloklab.db.PlayerDao;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {

    private static final int  PORT    = 12345;
    private static final long TICK_MS = 40L;

    private static final Gson GSON = new Gson();

    private final ServerGameState gameState = new ServerGameState();
    private final CopyOnWriteArrayList<ClientHandler> handlers = new CopyOnWriteArrayList<>();
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private final PlayerDao playerDao;

    private volatile boolean running = true;

    public GameServer(PlayerDao playerDao) {
        this.playerDao = playerDao;
    }

    public void start() throws IOException {
        Thread tickThread = new Thread(this::tickLoop, "GameTick");
        tickThread.setDaemon(true);
        tickThread.start();

        System.out.println("[Server] Запущен на порту " + PORT);
        System.out.println("[Server] Ожидание подключений (до " + ServerGameState.MAX_PLAYERS + " игроков)...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (running) {
                Socket client = serverSocket.accept();
                System.out.println("[Server] Новое соединение: " + client.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(client, gameState, playerDao);
                handlers.add(handler);
                pool.execute(() -> {
                    handler.run();
                    handlers.remove(handler);
                });
            }
        }
    }

    public void stop() {
        running = false;
        pool.shutdownNow();
    }

    private void tickLoop() {
        while (running) {
            try {
                String winner = gameState.tick();

                if (winner != null && playerDao != null) {
                    final String w = winner;
                    pool.execute(() -> {
                        try {
                            playerDao.incrementWins(w);
                        } catch (Exception e) {
                            System.err.println("[Server] Ошибка обновления побед в БД: " + e.getMessage());
                        }
                    });
                }

                String stateJson = buildStateJson();
                for (ClientHandler h : handlers) {
                    h.sendMessage(stateJson);
                }

                Thread.sleep(TICK_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private String buildStateJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("type",   "STATE");
        obj.addProperty("phase",  gameState.getPhase().name());
        obj.addProperty("nearY",  gameState.getNearY());
        obj.addProperty("farY",   gameState.getFarY());

        String winner = gameState.getLastWinner();
        if (winner != null && gameState.getPhase() == ServerGameState.Phase.FINISHED) {
            obj.addProperty("winner", winner);
        }

        JsonArray players = new JsonArray();
        for (PlayerInfo p : gameState.getPlayers()) {
            JsonObject pObj = new JsonObject();
            pObj.addProperty("username",    p.username);
            pObj.addProperty("score",       p.score);
            pObj.addProperty("shots",       p.shots);
            pObj.addProperty("ready",       p.ready);
            pObj.addProperty("wins",        p.wins);
            pObj.addProperty("playerIndex", p.playerIndex);
            players.add(pObj);
        }
        obj.add("players", players);

        JsonArray arrows = new JsonArray();
        for (PlayerInfo p : gameState.getPlayers()) {
            if (p.arrowVisible) {
                JsonObject aObj = new JsonObject();
                aObj.addProperty("username",    p.username);
                aObj.addProperty("playerIndex", p.playerIndex);
                aObj.addProperty("x",           p.arrowX);
                aObj.addProperty("y",           p.arrowY);
                arrows.add(aObj);
            }
        }
        obj.add("arrows", arrows);

        return GSON.toJson(obj);
    }
}
