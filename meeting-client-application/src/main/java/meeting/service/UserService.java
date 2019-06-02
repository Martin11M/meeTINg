package meeting.service;


import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import meeting.StageLoader;
import meeting.client.Client;
import meeting.controller.LoginWindowController;

import javafx.event.ActionEvent;
import java.util.Optional;

public class UserService {

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
                    StageLoader.loadStage((Stage)((Node) actionEvent.getSource()).getScene().getWindow(), fxmlLoader);
                    LoginWindowController loginWindowController = fxmlLoader.getController();
                    loginWindowController.setClient(client);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
