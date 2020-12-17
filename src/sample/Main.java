package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.AnchorPane;
import org.w3c.dom.views.AbstractView;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {
    private Stage primaryStage;
    /*Подключаем макет программы*/
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Musikspieler");
        show_my_programm();
    }

    public void show_my_programm() {
        try {
            FXMLLoader loader = new FXMLLoader();
            URL xml_url = getClass().getResource("view.fxml");
            loader.setLocation(xml_url);
            Parent root = loader.load();
            Controller controller = loader.getController();
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

