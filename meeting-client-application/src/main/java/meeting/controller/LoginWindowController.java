package meeting.controller;

import javafx.application.Platform;
import meeting.api.ConnectionManager;
import meeting.api.request.UserDataRequest;
import meeting.api.response.UserLoginResponse;
import meeting.enums.RequestFlag;
import meeting.enums.ResponseFlag;
import meeting.enums.SystemRole;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import meeting.model.User;
import meeting.serializer.Serializer;
import meeting.service.ApplicationService;

import java.nio.charset.StandardCharsets;

import static com.google.common.hash.Hashing.sha256;

public class LoginWindowController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    private String address;
    private String port;

    @FXML
    public void initialize() {

        Platform.runLater(() -> {
            if(address == null)
                address = "localhost";

            if(port == null)
                port = "9543";
        });
    }

    @FXML
    public void signInClicked(ActionEvent event) {

        if(!validateCredentials()) {
            ApplicationService.showErrorAlert("Wrong username or password!");
            return;
        }

        String hashedPassword = sha256()
                .hashString(passwordField.getText(), StandardCharsets.UTF_8)
                .toString();

        UserDataRequest userDataRequest = UserDataRequest.builder()
                .flag(RequestFlag.LOGGING.toString())
                .username(usernameField.getText())
                .password(hashedPassword)
                .build();

        ConnectionManager connectionManager = new ConnectionManager(address, port);
        Serializer serializer = new Serializer(connectionManager);

        UserLoginResponse userLoginResponse = serializer.signIn(userDataRequest);

        if(userLoginResponse.getFlag().equals(ResponseFlag.DISCONN.toString())) {
            ApplicationService.showErrorAlert("CANT CONNECT");
            return;
        }
        if (userLoginResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            ApplicationService.showErrorAlert("Wrong username or password!");
            return;
        }

        User user = User.builder()
                .id(userLoginResponse.getId())
                .username(userLoginResponse.getUsername())
                .password(userLoginResponse.getPassword())
                .systemRole(SystemRole.valueOf(userLoginResponse.getSystemRole()))
                .build();

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/GroupsWindow.fxml"));
            ApplicationService.loadStage((Stage)((Node) event.getSource()).getScene().getWindow(), fxmlLoader);
            GroupsWindowController groupsWindowController = fxmlLoader.getController();
            groupsWindowController.setSerializer(serializer);
            groupsWindowController.setUser(user);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void registration(ActionEvent event) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/RegistrationWindow.fxml"));
            ApplicationService.loadStage((Stage)((Node) event.getSource()).getScene().getWindow(), fxmlLoader);
            RegistrationWindowController registrationWindowController = fxmlLoader.getController();
            registrationWindowController.setAddress(this.address);
            registrationWindowController.setPort(this.port);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validateCredentials() {
        return !usernameField.getText().trim().equals("") && !passwordField.getText().trim().equals("");
    }

    public void settingsClicked(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/Settings.fxml"));
            ApplicationService.loadStage((Stage)((Node) actionEvent.getSource()).getScene().getWindow(), fxmlLoader);
            SettingsWindowController settingsWindowController = fxmlLoader.getController();
            settingsWindowController.setAddress(this.address);
            settingsWindowController.setPort(this.port);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    void setAddress(String address) {
        this.address = address;
    }

    void setPort(String port) {
        this.port = port;
    }
}

