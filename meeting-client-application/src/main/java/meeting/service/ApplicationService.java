package meeting.service;


import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import meeting.Main;
import meeting.client.Client;
import meeting.controller.LoginWindowController;

import javafx.event.ActionEvent;
import java.util.Optional;

public class ApplicationService {

    public static void loadStage(Stage stage, FXMLLoader fxmlLoader)
    {
        try {
            AnchorPane root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(Main.class.getResource("/css/background.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("meeTINg Client Application");
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void signOut(ActionEvent actionEvent, Class theClass, Client client) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setContentText("Do you want to sign out?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            ButtonType reloadedResult = result.get();
            if (reloadedResult == ButtonType.OK){
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(theClass.getResource("/fxml/LoginWindow.fxml"));
                    loadStage((Stage)((Node) actionEvent.getSource()).getScene().getWindow(), fxmlLoader);
                    LoginWindowController loginWindowController = fxmlLoader.getController();
                    loginWindowController.setClient(client);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }
}
