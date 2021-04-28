package cz.bain.autosweeper;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        // load main window
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("sample.fxml")));

        primaryStage.setTitle("fractals-distinct-title");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        System.out.println("loaded");
    }


    public static void main(String[] args) {
        launch(args);
    }
}
