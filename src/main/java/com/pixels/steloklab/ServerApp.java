package com.pixels.steloklab;

import com.pixels.steloklab.db.PlayerDao;
import com.pixels.steloklab.server.GameServer;

public class ServerApp {

    public static void main(String[] args) {
        System.out.println("=== Меткий стрелок — Сервер ===");

        PlayerDao playerDao = null;
        try {
            System.out.println("[Server] Инициализация базы данных (Hibernate)...");
            playerDao = new PlayerDao();
            System.out.println("[Server] БД готова.");
        } catch (Exception e) {
            System.err.println("[Server] ПРЕДУПРЕЖДЕНИЕ: не удалось подключить БД — " + e.getMessage());
            System.err.println("[Server] Сервер продолжит работу без сохранения данных.");
        }

        GameServer server = new GameServer(playerDao);

        final PlayerDao finalDao = playerDao;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[Server] Остановка...");
            server.stop();
            if (finalDao != null) finalDao.close();
        }));

        try {
            server.start();
        } catch (Exception e) {
            System.err.println("[Server] Критическая ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
