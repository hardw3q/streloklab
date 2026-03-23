package com.pixels.steloklab;

import com.pixels.steloklab.controller.ShooterController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ShooterApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                ShooterApplication.class.getResource("game-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 920, 520);
        stage.setTitle("Меткий стрелок");
        stage.setScene(scene);
        stage.setResizable(false);

        ShooterController controller = fxmlLoader.getController();
        stage.setOnCloseRequest(e -> controller.shutdown());

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
