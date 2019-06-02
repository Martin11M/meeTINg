package meeting;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import meeting.api.ConnectionManager;
import meeting.controller.LoginWindowController;
import meeting.serializer.Serializer;
import meeting.service.ApplicationService;


public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource("/fxml/LoginWindow.fxml"));
        ApplicationService.loadStage(new Stage(), fxmlLoader);
        ConnectionManager connectionManager = new ConnectionManager();
        Serializer serializer = new Serializer(connectionManager);
        LoginWindowController loginWindowController = fxmlLoader.getController();
        loginWindowController.setSerializer(serializer);
    }
}
