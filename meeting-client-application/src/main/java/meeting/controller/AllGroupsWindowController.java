package meeting.controller;

import javafx.scene.control.*;
import meeting.StageLoader;
import meeting.api.request.GroupListRequest;
import meeting.api.request.MembershipRequest;
import meeting.api.request.NewGroupRequest;
import meeting.api.response.ErrorResponse;
import meeting.api.response.FlagResponse;
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
import javafx.stage.Stage;
import meeting.model.Group;
import meeting.model.User;
import sun.rmi.runtime.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        Platform.runLater(() ->{
            if(user.getSystemRole() == USER) {
                roleInfoLabel.setText("Logged as User (limited options)");
            }

            refreshClicked();
        });
    }

    @FXML
    private void applyClicked() {

        // robie JSONa
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
            showErrorAlert("Cannot do request MEMBREQ");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText("Membership request sent to leader of group");
        alert.showAndWait();

        // zeby po aplikowaniu uzytkownika nie pokazala mu sie znow ta sama grupa zeby nie mogl w nia kliknac
        refreshClicked();
    }

    @FXML
    private void returnClicked(Event event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/GroupsWindow.fxml"));
            StageLoader.loadStage((Stage)((Node) event.getSource()).getScene().getWindow(), fxmlLoader);
            GroupsWindowController groupsWindowController = fxmlLoader.getController();
            groupsWindowController.setClient(client);
            groupsWindowController.setUser(user);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void listClicked(){
        // warunek zeby po kliknieciu w puste pola sie nie wywolywalo i zeby po kliknieciu w puste pole nie odpalalo sie dla biezacego pickedGroup
        if(listView.getSelectionModel().getSelectedItem() != null &&
                listView.getSelectionModel().getSelectedItem() != pickedGroup) {

            pickedGroup = listView.getSelectionModel().getSelectedItem();
            applyButton.setDisable(false);
        }
    }

    @FXML
    private void signOutClicked(ActionEvent event) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setContentText("Do you want to sign out?");

        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent()){
            ButtonType reloadedResult = result.get();
            if (reloadedResult == ButtonType.OK){
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/LoginWindow.fxml"));
                    StageLoader.loadStage((Stage)((Node) event.getSource()).getScene().getWindow(), fxmlLoader);
                    LoginWindowController loginWindowController = fxmlLoader.getController();
                    loginWindowController.setClient(client);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @FXML
    private void refreshClicked() {
        // robie JSONa
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        // id uzytkownia idzie po to zeby przyslali spowrotem tylko te grupy do ktorych uzytkownik nie nalezy
        GroupListRequest allGroupsRequest = GroupListRequest.builder()
                .flag(RequestFlag.GRPLIST.toString())
                .userId(user.getId())
                .build();

        String requestString = gson.toJson(allGroupsRequest);

        String allGroupsResponseString = client.sendRequestRecResponse(requestString);

        GroupListResponse groupListResponse = gson.fromJson(allGroupsResponseString, GroupListResponse.class);

        if(groupListResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            showErrorAlert("Cannot do request GRPLIST");
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

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    void setClient(Client client) {
        this.client = client;
    }

    void setUser(User user) {
        this.user = user;
    }
}
