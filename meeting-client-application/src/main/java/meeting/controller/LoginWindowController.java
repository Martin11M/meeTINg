package meeting.controller;

import meeting.api.request.UserDataRequest;
import meeting.api.response.UserLoginResponse;
import meeting.client.Client;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import meeting.enums.RequestFlag;
import meeting.enums.ResponseFlag;
import meeting.enums.SystemRole;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import meeting.model.User;
import meeting.service.ApplicationService;

import java.nio.charset.StandardCharsets;

import static com.google.common.hash.Hashing.sha256;

public class LoginWindowController {

    private Client client;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    public void signInClicked(ActionEvent event) {

        if(!validateCredentials()) {
            showLoginErrorAlert();
            return;
        }

        // hashuje hasło
        String hashedPassword = sha256()
                .hashString(passwordField.getText(), StandardCharsets.UTF_8)
                .toString();

        // tworze requesta
        UserDataRequest request = UserDataRequest.builder()
                .flag(RequestFlag.LOGGING.toString())
                .username(usernameField.getText())
                .password(hashedPassword)
                .build();

        System.out.println("Dlugosc username: " + usernameField.getText().length());
        System.out.println("Dlugosc password: " + hashedPassword.length());

        // robie JSONa
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        String requestString = gson.toJson(request);
        System.out.println(requestString);

        System.out.println("Dlugosc requestString: " + requestString.length());

        // wysyłam tego requesta, po czym przychodzi response:
        String responseString = client.sendRequestRecResponse(requestString);

        // parsuje JSONa
        UserLoginResponse response = gson.fromJson(responseString, UserLoginResponse.class);

        if (response.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            showLoginErrorAlert();
            return;
        }

        // tworze obiekt użytkownika
        User user = User.builder()
                .id(response.getId())
                .username(response.getUsername())
                .password(response.getPassword())
                .systemRole(SystemRole.valueOf(response.getSystemRole()))
                .build();

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/GroupsWindow.fxml"));
            ApplicationService.loadStage((Stage)((Node) event.getSource()).getScene().getWindow(), fxmlLoader);
            GroupsWindowController groupsWindowController = fxmlLoader.getController();
            groupsWindowController.setClient(client);
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
            registrationWindowController.setClient(client);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

//    private void loadStage(Stage stage, String resourcePath, String title)
//    {
//        try {
//            FXMLLoader fxmlLoader = new FXMLLoader(LoginWindowController.class.getResource(resourcePath));
//            AnchorPane root = fxmlLoader.load();
//            root.setId("userLogin");
//            Scene scene = new Scene(root);
//            scene.getStylesheets().addAll(this.getClass().getResource("../../resources/css/background.css").toExternalForm());
//            stage.setScene(scene);
//            stage.setTitle(title);
//            stage.setResizable(false);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void showLoginErrorAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText("Wrong username or password!");
        alert.show();
    }

    private boolean validateCredentials() {
        return !usernameField.getText().trim().equals("") && !passwordField.getText().trim().equals("");
    }

    public void setClient(Client client) {
        if (this.client == null) this.client = client;
    }
}

