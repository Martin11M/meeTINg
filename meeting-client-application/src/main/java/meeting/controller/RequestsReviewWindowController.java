package meeting.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import meeting.api.request.RequestDecisionRequest;
import meeting.api.request.RequestReviewListRequest;
import meeting.api.response.FlagResponse;
import meeting.api.response.RequestReviewListResponse;
import meeting.enums.RequestFlag;
import meeting.enums.ResponseFlag;
import meeting.model.RequestReview;
import meeting.model.User;
import meeting.serializer.Serializer;
import meeting.service.ApplicationService;

import java.util.ArrayList;
import java.util.List;

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

    private User user;
    private Serializer serializer;

    private RequestReview pickedRequest;

    @FXML
    public void initialize() {
        initCols();
        Platform.runLater(this::refreshClicked);
    }

    @FXML
    public void signOutClicked(ActionEvent actionEvent) {
        ApplicationService.signOut(actionEvent, EventsWindowController.class, serializer);
    }

    @FXML
    public void returnClicked(ActionEvent actionEvent) {
        try {
            ApplicationService.loadPreviousWindow(actionEvent, RequestsReviewWindowController.class, serializer, user, null, "/fxml/GroupsWindow.fxml");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void refreshClicked() {

        RequestReviewListRequest requestReviewListRequest = RequestReviewListRequest.builder()
                .flag(RequestFlag.USERREQ.toString())
                .leaderId(user.getId())
                .build();

        RequestReviewListResponse requestReviewListResponse = serializer.refreshRequests(requestReviewListRequest);

        if(requestReviewListResponse.getFlag().equals(ResponseFlag.DISCONN.toString())) {
            if (ApplicationService.showDisconnectAlert() == 0) {
                refreshClicked();
            }
            return;
        }
        if(requestReviewListResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            ApplicationService.showErrorAlert("Error response for USERREQ");
            return;
        }

        List<RequestReview> requests = new ArrayList<>();

        requestReviewListResponse.getItems().forEach(element -> {
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

    private String determineFlag(Event evt) {
        if(((Button)evt.getSource()).getId().equals("acceptButton"))
            return RequestFlag.USERACC.toString();
        else
            return RequestFlag.USERDEC.toString();
    }

    @FXML
    public void decisionClicked(Event evt) {

        String flagInApp = determineFlag(evt);

        RequestDecisionRequest requestDecisionRequest = RequestDecisionRequest.builder()
                .flag(flagInApp)
                .userId(pickedRequest.getUserId())
                .groupId(pickedRequest.getGroupId())
                .build();

        FlagResponse flagResponse = serializer.makeRequestDecision(requestDecisionRequest);

        if(flagResponse.getFlag().equals(ResponseFlag.DISCONN.toString())) {
            if (ApplicationService.showDisconnectAlert() == 0) {
                decisionClicked(evt);
            }
            return;
        }
        if(flagResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            ApplicationService.showErrorAlert("Error response for USERACC/USERDEC");
            return;
        }

        if(flagInApp.equals(RequestFlag.USERACC.toString())) {
            ApplicationService.showInformationAlert("Information Dialog", "User \"" + pickedRequest.getUserName() +"\" accepted to group \"" + pickedRequest.getGroupName() + "\"");
        }
        else {
            ApplicationService.showInformationAlert("Information Dialog", "User \"" + pickedRequest.getUserName() +"\" not accepted to group \"" + pickedRequest.getGroupName() + "\"");
        }
        refreshClicked();
    }

    private void initCols() {
        groupIdCol.setCellValueFactory(new PropertyValueFactory<>("groupId"));
        groupNameCol.setCellValueFactory(new PropertyValueFactory<>("groupName"));
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("userId"));
    }

    void setUser(User user) {
        this.user = user;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }
}
