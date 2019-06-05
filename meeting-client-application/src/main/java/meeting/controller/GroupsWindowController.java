package meeting.controller;

import meeting.api.request.GroupListRequest;
import meeting.api.request.NewGroupRequest;
import meeting.api.response.GroupListResponse;
import meeting.api.response.NewGroupResponse;
import meeting.enums.RequestFlag;
import meeting.enums.ResponseFlag;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import meeting.model.Group;
import meeting.model.User;
import meeting.serializer.Serializer;
import meeting.service.ApplicationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static meeting.enums.SystemRole.USER;

public class GroupsWindowController {

    @FXML ListView<Group> listView;
    @FXML Button createButton;
    @FXML Button requestsButton;
    @FXML Button allGroupsButton;
    @FXML Label roleInfoLabel;

    private Group pickedGroup;
    private User user;

    private Serializer serializer;

    @FXML
    public void initialize() {

        Platform.runLater(() -> {
            if(user.getSystemRole() == USER) {
                createButton.setDisable(true);
                requestsButton.setDisable(true);
                roleInfoLabel.setText("Logged as " + user.getUsername() + " (User)");
            }
            else {
                allGroupsButton.setDisable(true);
                roleInfoLabel.setText("Logged as " + user.getUsername() + " (Team Leader)");
            }
            refreshClicked();
        });
    }

    @FXML
    private void allGroupsClicked(Event event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/AllGroupsWindow.fxml"));
            ApplicationService.loadStage((Stage)((Node) event.getSource()).getScene().getWindow(), fxmlLoader);
            AllGroupsWindowController allGroupsWindowController = fxmlLoader.getController();
            allGroupsWindowController.setSerializer(serializer);
            allGroupsWindowController.setUser(user);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void requestsClicked(Event event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/RequestsReviewWindow.fxml"));
            ApplicationService.loadStage((Stage)((Node) event.getSource()).getScene().getWindow(), fxmlLoader);
            RequestsReviewWindowController reqWindowController = fxmlLoader.getController();
            reqWindowController.setSerializer(serializer);
            reqWindowController.setUser(user);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void listClicked(Event event){

        if(listView.getSelectionModel().getSelectedItem() != null &&
                listView.getSelectionModel().getSelectedItem() != pickedGroup) {

            pickedGroup = listView.getSelectionModel().getSelectedItem();

            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/EventsWindow.fxml"));
                ApplicationService.loadStage((Stage)((Node) event.getSource()).getScene().getWindow(), fxmlLoader);
                EventsWindowController eventsWindowController = fxmlLoader.getController();
                eventsWindowController.setPickedGroup(pickedGroup);
                eventsWindowController.setSerializer(serializer);
                eventsWindowController.setUser(user);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void signOutClicked(ActionEvent actionEvent){
        ApplicationService.signOut(actionEvent, EventsWindowController.class, serializer);
    }

    @FXML
    private void refreshClicked() {

        GroupListRequest groupRequest = GroupListRequest.builder()
                .flag(RequestFlag.USERGRP.toString())
                .userId(user.getId())
                .build();


        GroupListResponse groupListResponse = serializer.refreshGroups(groupRequest);

        if(groupListResponse.getFlag().equals(ResponseFlag.DISCONN.toString())) {
            if (ApplicationService.showDisconnectAlert() == 0) {
                refreshClicked();
            }
            return;
        }
        if(groupListResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            ApplicationService.showErrorAlert("Error response for USERGRP");
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

    @FXML
    public void createClicked() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New group");
        dialog.setHeaderText(null);
        dialog.setContentText("Please enter group name:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            if (name.equals("")) ApplicationService.showErrorAlert("Field can not be empty!");
            else {

                NewGroupRequest newGroupRequest = NewGroupRequest.builder()
                        .flag(RequestFlag.MAKEGRP.toString())
                        .leaderId(user.getId())
                        .groupName(name)
                        .build();

                NewGroupResponse newGroupResponse = serializer.createGroup(newGroupRequest);

                if(newGroupResponse.getFlag().equals(ResponseFlag.DISCONN.toString())) {
                    if (ApplicationService.showDisconnectAlert() == 0) {
                        createClicked();
                    }
                    return;
                }
                if(newGroupResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
                    ApplicationService.showErrorAlert("Error response for MAKEGRP");
                    return;
                }

                Group g = Group.builder()
                        .id(newGroupResponse.getId())
                        .name(newGroupResponse.getName())
                        .leader(newGroupResponse.getLeader())
                        .build();

                listView.getItems().add(g);
            }
        });
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }
}
