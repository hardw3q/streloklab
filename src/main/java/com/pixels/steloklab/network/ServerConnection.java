package com.pixels.steloklab.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ServerConnection {

    private static final Gson GSON = new Gson();

    private Socket socket;
    private PrintWriter out;
    private final Object writeLock = new Object();
    private final Consumer<JsonObject> onMessage;

    public ServerConnection(Consumer<JsonObject> onMessage) {
        this.onMessage = onMessage;
    }

    public void connect(String host, int port, String username) throws IOException {
        socket = new Socket(host, port);

        synchronized (writeLock) {
            out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        }

        JsonObject join = new JsonObject();
        join.addProperty("type", "JOIN");
        join.addProperty("username", username);
        sendRaw(GSON.toJson(join));

        Thread reader = new Thread(this::readLoop, "ServerReader");
        reader.setDaemon(true);
        reader.start();
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public void sendReady()       { send("READY"); }
    public void sendPause()       { send("PAUSE"); }
    public void sendShoot()       { send("SHOOT"); }
    public void sendLeaderboard() { send("LEADERBOARD"); }

    private void send(String type) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type", type);
        sendRaw(GSON.toJson(msg));
    }

    private void sendRaw(String json) {
        synchronized (writeLock) {
            if (out != null) out.println(json);
        }
    }

    private void readLoop() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = in.readLine()) != null) {
                final JsonObject msg = GSON.fromJson(line, JsonObject.class);
                Platform.runLater(() -> onMessage.accept(msg));
            }
        } catch (IOException e) {
            JsonObject err = new JsonObject();
            err.addProperty("type", "DISCONNECTED");
            Platform.runLater(() -> onMessage.accept(err));
        }
    }
}
