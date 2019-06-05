package meeting.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import meeting.service.ApplicationService;


public class SettingsWindowController {

    @FXML public TextField addressField;
    @FXML public TextField portField;
    @FXML public Button backButton;


    @FXML
    private void backClicked(ActionEvent actionEvent){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/LoginWindow.fxml"));
        ApplicationService.loadStage((Stage)((Node) actionEvent.getSource()).getScene().getWindow(), fxmlLoader);
        LoginWindowController loginWindowController = fxmlLoader.getController();
        loginWindowController.setAddress(addressField.getText());
        loginWindowController.setPort(portField.getText());
    }

    @FXML
    private void fieldChanged() {

        backButton.setDisable(addressField.getText().trim().isEmpty() ||
                portField.getText().trim().isEmpty());

        try {
            int port = Integer.parseInt(portField.getText());
            if (port < 1024 || port > 65535) throw new NumberFormatException();
        }
        catch (NumberFormatException e) {
            backButton.setDisable(true);
        }
    }

    void setAddress(String address) {
        addressField.setText(address);
    }

    void setPort(String port) {
        portField.setText(port);
    }
}

