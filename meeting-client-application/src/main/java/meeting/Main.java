package meeting;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import meeting.client.Client;
import meeting.controller.LoginWindowController;


public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/fxml/LoginWindow.fxml"));
        StageLoader.loadStage(new Stage(), fxmlLoader);
        LoginWindowController loginWindowController = fxmlLoader.getController();
        loginWindowController.setClient(new Client());
    }
}
