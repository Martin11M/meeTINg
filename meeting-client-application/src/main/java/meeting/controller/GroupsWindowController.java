package meeting.controller;

import meeting.api.request.GroupListRequest;
import meeting.api.request.NewGroupRequest;
import meeting.api.response.GroupListResponse;
import meeting.api.response.NewGroupResponse;
import meeting.client.Client;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
    private Client client;
    private User user;

    @FXML
    public void initialize() {

        Platform.runLater(() ->{
            // jesli ktos nie jest leader to trzeba buttons disable
            if(user.getSystemRole() == USER) {
                createButton.setDisable(true);
                requestsButton.setDisable(true);
                roleInfoLabel.setText("Logged as " + user.getUsername() + " (User)");
            }
            else {
                // leader nie moze aplikowac do grup
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
            allGroupsWindowController.setClient(client);
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
            reqWindowController.setClient(client);
            reqWindowController.setUser(user);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void listClicked(Event event){
        // warunek zeby po kliknieciu w puste pola sie nie wywolywalo i zeby po kliknieciu w puste pole nie odpalalo sie dla biezacego pickedGroup
        if(listView.getSelectionModel().getSelectedItem() != null &&
                listView.getSelectionModel().getSelectedItem() != pickedGroup) {

            pickedGroup = listView.getSelectionModel().getSelectedItem();

            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/EventsWindow.fxml"));
                ApplicationService.loadStage((Stage)((Node) event.getSource()).getScene().getWindow(), fxmlLoader);
                EventsWindowController eventsWindowController = fxmlLoader.getController();
                eventsWindowController.setPickedGroup(pickedGroup);
                eventsWindowController.setClient(client);
                eventsWindowController.setUser(user);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void signOutClicked(ActionEvent actionEvent){
        ApplicationService.signOut(actionEvent, EventsWindowController.class, client);
    }

    @FXML
    private void refreshClicked() {
        // robie JSONa
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        GroupListRequest groupRequest = GroupListRequest.builder()
                .flag(RequestFlag.USERGRP.toString())
                .userId(user.getId())
                .build();

        String requestString = gson.toJson(groupRequest);

        String groupResponseString = client.sendRequestRecResponse(requestString);


        GroupListResponse groupListResponse = gson.fromJson(groupResponseString, GroupListResponse.class);

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
                // robie JSONa
                GsonBuilder builder = new GsonBuilder();
                builder.setPrettyPrinting();
                Gson gson = builder.create();

                NewGroupRequest newGroupRequest = NewGroupRequest.builder()
                        .flag(RequestFlag.MAKEGRP.toString())
                        .leaderId(user.getId())
                        .groupName(name)
                        .build();

                String requestString = gson.toJson(newGroupRequest);

                String response = client.sendRequestRecResponse(requestString);

                NewGroupResponse newGroupResponse = gson.fromJson(response, NewGroupResponse.class);

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

    public void setClient(Client client) {
        this.client = client;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
