package meeting.controller;

import meeting.api.request.UserDataRequest;
import meeting.api.response.FlagResponse;
import meeting.enums.RequestFlag;
import meeting.enums.ResponseFlag;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import meeting.serializer.Serializer;
import meeting.service.ApplicationService;

import java.nio.charset.StandardCharsets;

import static com.google.common.hash.Hashing.sha256;

public class RegistrationWindowController {

    @FXML public CheckBox isLeader;
    @FXML private PasswordField password;
    @FXML private PasswordField confirmPassword;
    @FXML private TextField username;
    @FXML private Button registerButton;
    @FXML private Label infoLabel;

    private Serializer serializer;

    @FXML
    public void registerClicked(ActionEvent event) {
        if(password.getText().trim().equals(confirmPassword.getText().trim())) {

            String hashedPassword = sha256()
                    .hashString(password.getText(), StandardCharsets.UTF_8)
                    .toString();

            UserDataRequest userDataRequest = UserDataRequest.builder()
                    .flag(RequestFlag.REGISTR.toString())
                    .username(username.getText())
                    .password(hashedPassword)
                    .isLeader(isLeader.isSelected())
                    .build();

            FlagResponse flagResponse = serializer.register(userDataRequest);

            if (flagResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
                ApplicationService.showErrorAlert("Username is already in use");
                return;
            }
            ApplicationService.showInformationAlert("Information Dialog", "Registration completed");
            cancelClicked(event);
        }
        else {
            infoLabel.setText("Passwords do not match");
        }
    }

    @FXML
    public void cancelClicked(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/LoginWindow.fxml"));
            ApplicationService.loadStage((Stage)((Node) event.getSource()).getScene().getWindow(), fxmlLoader);
            LoginWindowController loginWindowController = fxmlLoader.getController();
            loginWindowController.setSerializer(serializer);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void fieldChanged() {
        registerButton.setDisable(username.getText().trim().isEmpty() ||
                password.getText().trim().isEmpty() || confirmPassword.getText().trim().isEmpty());

        if(!infoLabel.getText().isEmpty())
            infoLabel.setText("");
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }
}
