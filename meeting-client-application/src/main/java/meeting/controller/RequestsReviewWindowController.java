package meeting.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import meeting.StageLoader;
import meeting.api.request.RequestDecisionRequest;
import meeting.api.request.RequestReviewListRequest;
import meeting.api.response.ErrorResponse;
import meeting.api.response.FlagResponse;
import meeting.api.response.RequestReviewListResponse;
import meeting.client.Client;
import meeting.enums.RequestFlag;
import meeting.enums.ResponseFlag;
import meeting.model.RequestReview;
import meeting.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RequestsReviewWindowController {

    @FXML public Button refreshButton;
    @FXML public Button returnButton;
    @FXML public Button signOutButton;
    @FXML public Button acceptButton;
    @FXML public Button declineButton;

    @FXML private TableView<RequestReview> reqTable;
    @FXML private TableColumn<RequestReview, Long> groupIdCol;
    @FXML private TableColumn<RequestReview, String> groupNameCol;
    @FXML private TableColumn<RequestReview, String> userNameCol;
    @FXML private TableColumn<RequestReview, Long> userIdCol;

    private Client client;
    private User user;

    private RequestReview pickedRequest;

    @FXML
    public void initialize() {
        initCols();

        Platform.runLater(() -> {
            // blokowanie accept i decline w tej funkcji, bo odswiezac mozna samemu, wtedy sie wiersz odznacza
            refreshClicked();
        });
    }

    @FXML
    public void signOutClicked(ActionEvent actionEvent) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setContentText("Do you want to sign out?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            ButtonType reloadedResult = result.get();
            if (reloadedResult == ButtonType.OK){
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/LoginWindow.fxml"));
                    StageLoader.loadStage((Stage)((Node) actionEvent.getSource()).getScene().getWindow(), fxmlLoader);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @FXML
    public void returnClicked(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/GroupsWindow.fxml"));
            StageLoader.loadStage((Stage)((Node) actionEvent.getSource()).getScene().getWindow(), fxmlLoader);
            GroupsWindowController groupsWindowController = fxmlLoader.getController();
            groupsWindowController.setClient(client);
            groupsWindowController.setUser(user);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void refreshClicked() {
        // robie JSONa
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        RequestReviewListRequest request = RequestReviewListRequest.builder()
                .flag(RequestFlag.USERREQ.toString())
                .leaderId(user.getId())
                .build();

        String requestString = gson.toJson(request);

//        String response = client.sendRequestRecResponse(requestString);

        // imitacja response
        String response =
                "{\n" +
                "  \"flag\" : \"USERREQ\", \n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"groupId\": \"101\",\n" +
                "      \"groupName\": \"TKOM 2019\",\n" +
                "      \"userName\": \"Marcin Jancurewicz\",\n" +
                "      \"userId\": \"20\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"groupId\": \"121\",\n" +
                "      \"groupName\": \"TKOM 2018\",\n" +
                "      \"userName\": \"Michael do Santa\",\n" +
                "      \"userId\": \"23\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";

        RequestReviewListResponse reqRevListResponse = gson.fromJson(response, RequestReviewListResponse.class);

        if(reqRevListResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            showErrorAlert("Cannot do request USERREQ");
            return;
        }

        List<RequestReview> requests = new ArrayList<>();

        reqRevListResponse.getItems().forEach(element -> {
            RequestReview r = RequestReview.builder()
                    .groupId(element.getGroupId())
                    .groupName(element.getGroupName())
                    .userName(element.getUserName())
                    .userId(element.getUserId())
                    .build();
            requests.add(r);
        });

        reqTable.getItems().clear();
        reqTable.getItems().addAll(requests);

        acceptButton.setDisable(true);
        declineButton.setDisable(true);
    }

    @FXML
    public void tableClicked() {

        if(reqTable.getSelectionModel().getSelectedItem() != null &&
                reqTable.getSelectionModel().getSelectedItem() != pickedRequest) {

            pickedRequest = reqTable.getSelectionModel().getSelectedItem();
            acceptButton.setDisable(false);
            declineButton.setDisable(false);
        }
    }

    String determineFlag(Event evt) {
        if(((Button)evt.getSource()).getId().equals("acceptButton"))
            return RequestFlag.USERACC.toString();
        else
            return RequestFlag.USERDEC.toString();
    }

    @FXML
    public void decisionClicked(Event evt) {
        // robie JSONa
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        String flagInApp = determineFlag(evt);

        RequestDecisionRequest request = RequestDecisionRequest.builder()
                .flag(flagInApp)
                .userId(pickedRequest.getUserId())
                .groupId(pickedRequest.getGroupId())
                .build();

        String requestString = gson.toJson(request);

//        String response = client.sendRequestRecResponse(requestString);

        // imitacja response
        String decisionResponse =
                "{\n" +
                "  \"flag\" : \"USERACC\" \n" +
                "}\n";

        FlagResponse response = gson.fromJson(decisionResponse, FlagResponse.class);

        if(response.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            showErrorAlert("Cannot do request USERACC/USERDEC");
            return;
        }

        if(flagInApp.equals(RequestFlag.USERACC.toString())) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("User \"" + pickedRequest.getUserName() +"\" accepted to group \"" + pickedRequest.getGroupName() + "\"");
            alert.showAndWait();
        }
        else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information Dialog");
            alert.setHeaderText("User \"" + pickedRequest.getUserName() +"\" not accepted to group \"" + pickedRequest.getGroupName() + "\"");
            alert.showAndWait();
        }

        // odswiezam zeby nie bylo juz tego wiersza w kolumnie
        refreshClicked();
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private void initCols() {
        groupIdCol.setCellValueFactory(new PropertyValueFactory<>("groupId"));
        groupNameCol.setCellValueFactory(new PropertyValueFactory<>("groupName"));
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
    }

    void setClient(Client client) {
        this.client = client;
    }

    void setUser(User user) {
        this.user = user;
    }
}
