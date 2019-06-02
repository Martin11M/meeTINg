package meeting.controller;

import javafx.scene.control.*;
import meeting.api.request.GroupListRequest;
import meeting.api.request.MembershipRequest;
import meeting.api.response.FlagResponse;
import meeting.api.response.GroupListResponse;
import meeting.api.Client;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import meeting.enums.RequestFlag;
import meeting.enums.ResponseFlag;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import meeting.model.Group;
import meeting.model.User;
import meeting.service.ApplicationService;

import java.util.ArrayList;
import java.util.List;

import static meeting.enums.SystemRole.USER;


public class AllGroupsWindowController {

    @FXML ListView<Group> listView;
    @FXML Button applyButton;
    @FXML Label roleInfoLabel;

    private Group pickedGroup;
    private Client client;
    private User user;

    @FXML
    public void initialize() {

        applyButton.setDisable(true);

        Platform.runLater(() -> {
            if(user.getSystemRole() == USER) {
                roleInfoLabel.setText("Logged as User (limited options)");
            }
            refreshClicked();
        });
    }

    @FXML
    private void applyClicked() {

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        MembershipRequest membershipRequest = MembershipRequest.builder()
                .flag(RequestFlag.MEMBREQ.toString())
                .userId(user.getId())
                .groupId(pickedGroup.getId())
                .build();

        String requestString = gson.toJson(membershipRequest);
        String membershipResponseString = client.sendRequestRecResponse(requestString);
        FlagResponse response = gson.fromJson(membershipResponseString, FlagResponse.class);

        if(response.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            ApplicationService.showErrorAlert("Error response for MEMBREQ");
            return;
        }

        ApplicationService.showInformationAlert("Information Dialog", "Membership request sent to leader of group");
        refreshClicked();
    }

    @FXML
    private void returnClicked(ActionEvent actionEvent) {
        try {
            ApplicationService.loadPreviousWindow(actionEvent, AllGroupsWindowController.class, client, user, null, "/fxml/GroupsWindow.fxml");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void listClicked(){
        if(listView.getSelectionModel().getSelectedItem() != null &&
                listView.getSelectionModel().getSelectedItem() != pickedGroup) {

            pickedGroup = listView.getSelectionModel().getSelectedItem();
            applyButton.setDisable(false);
        }
    }

    @FXML
    private void signOutClicked(ActionEvent actionEvent) {
        ApplicationService.signOut(actionEvent, EventsWindowController.class, client);
    }

    @FXML
    private void refreshClicked() {

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        GroupListRequest allGroupsRequest = GroupListRequest.builder()
                .flag(RequestFlag.GRPLIST.toString())
                .userId(user.getId())
                .build();

        String requestString = gson.toJson(allGroupsRequest);

        String allGroupsResponseString = client.sendRequestRecResponse(requestString);

        GroupListResponse groupListResponse = gson.fromJson(allGroupsResponseString, GroupListResponse.class);

        if(groupListResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            ApplicationService.showErrorAlert("Error response for GRPLIST");
            return;
        }

        List<Group> groups = new ArrayList<>();

        groupListResponse.getItems().forEach(element -> {
            Group g = Group.builder()
                    .id(element.getId())
                    .name(element.getName())
                    .leader(element.getLeader())
                    .build();
            groups.add(g);
        });

        listView.getItems().clear();
        listView.getItems().addAll(groups);
    }

    void setClient(Client client) {
        this.client = client;
    }

    void setUser(User user) {
        this.user = user;
    }
}
