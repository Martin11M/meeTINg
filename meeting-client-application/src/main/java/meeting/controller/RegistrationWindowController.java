package meeting.controller;

import meeting.api.request.UserDataRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import meeting.api.response.FlagResponse;
import meeting.enums.RequestFlag;
import meeting.enums.ResponseFlag;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import meeting.client.Client;
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

    private Client client;

    @FXML
    public void registerClicked(ActionEvent event) {
        if(password.getText().trim().equals(confirmPassword.getText().trim())) {

            // hashuje hasło
            String hashedPassword = sha256()
                    .hashString(password.getText(), StandardCharsets.UTF_8)
                    .toString();

            // tworze requesta
            UserDataRequest request = UserDataRequest.builder()
                    .flag(RequestFlag.REGISTR.toString())
                    .username(username.getText())
                    .password(hashedPassword)
                    .isLeader(isLeader.isSelected())
                    .build();

            // robie JSONa
            GsonBuilder builder = new GsonBuilder();
            builder.setPrettyPrinting();
            Gson gson = builder.create();

            String requestString = gson.toJson(request);

            String responseString = client.sendRequestRecResponse(requestString);

            FlagResponse response = gson.fromJson(responseString, FlagResponse.class);

            // jesli odpowiedz ze blad, to komunikat i od nowa
            if(response.getFlag().equals(ResponseFlag.__ERROR.toString())) {
                showErrorAlert("Username occupied :(");
                return;
            }
            // jesli odpowiedz ze zapisano uzytkownika to komunikat i do okna logowania
            else if(response.getFlag().equals(ResponseFlag.REGISTR.toString())) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Information Dialog");
                alert.setHeaderText("Account created :)");
                alert.showAndWait();

                cancelClicked(event);
            }
        }
        else {
            infoLabel.setText("Passwords doesnt match");
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    @FXML
    public void cancelClicked(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/LoginWindow.fxml"));
            ApplicationService.loadStage((Stage)((Node) event.getSource()).getScene().getWindow(), fxmlLoader);
            LoginWindowController loginWindowController = fxmlLoader.getController();
            loginWindowController.setClient(client);
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

    public void setClient(Client client) {
        this.client = client;
    }
}
