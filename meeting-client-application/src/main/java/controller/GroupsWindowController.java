package controller;

import api.request.GroupListRequest;
import api.response.GroupListResponse;
import client.Client;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import enums.RequestFlag;
import enums.ResponseFlag;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Group;
import model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static enums.SystemRole.USER;

public class GroupsWindowController {

    @FXML ListView<Group> listView;
    @FXML Button createButton;
    @FXML Button requestsButton;

    private Group pickedGroup;
    private Client client;
    private List<Group> groups;
    private User user;

    // na refresh powinno isc od klienta do serwa zapytanie o wszystkie grupy, to samo jak przy wejsciu do groupsview
    // i pozniej od nowa odpalic loadGroups

    @FXML
    public void initialize() {
        // TODO  initialize wyprzedza setUser
        // jesli ktos nie jest leader to trzeba buttons disable
        if(user.getSystemRole() == USER) {
            createButton.setDisable(true);
            requestsButton.setDisable(true);
        }

        refreshClicked();
    }

    @FXML
    private void listClicked(){
        // albo po kliknieciu przerzucac do grupy albo dac buttona ktory klika enter picked group

        // warunek zeby po kliknieciu w puste pola sie nie wywolywalo i zeby po kliknieciu w puste pole nie odpalalo sie dla biezacego pickedGroup
        if(listView.getSelectionModel().getSelectedItem() != null
                && listView.getSelectionModel().getSelectedItem() != pickedGroup) {
            pickedGroup = listView.getSelectionModel().getSelectedItem();

            System.out.println(pickedGroup.toString());
        }
    }

    @FXML
    private void signOutClicked(ActionEvent event){
        // TODO wyslanie requesta o wylogowanie, ale czy potrzeba???

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Are you sure??");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/LoginWindow.fxml"));
                AnchorPane root = fxmlLoader.load();
                Scene scene = new Scene(root);
                Stage userMainStage = (Stage)((Node) event.getSource()).getScene().getWindow();

                userMainStage.setScene(scene);
                userMainStage.show();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void refreshClicked() {
        // robie JSONa
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        GroupListRequest groupRequest = GroupListRequest.builder()
                .userId(user.getId())
                .build();

        String requestString = RequestFlag.USERGRP.toString() + gson.toJson(groupRequest);

//        String groupResponseString = client.sendRequestRecResponse(requestString);

        String groupResponseString = ResponseFlag.USERGRP.toString() +
                "{\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"id\": \"1\",\n" +
                "      \"name\": \"TKOM\",\n" +
                "      \"leader\": \"Gawkowski\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"21\",\n" +
                "      \"name\": \"TIN\",\n" +
                "      \"leader\": \"Blinowski\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";

        if(groupResponseString.substring(0, 7).equals(ResponseFlag.__ERROR.toString())) {
            // TODO obsługa błędu pobrania listy grup
            return;
        }

        GroupListResponse groupListResponse = gson.fromJson(groupResponseString.substring(7), GroupListResponse.class);

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

        System.out.println("refresh!");
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
