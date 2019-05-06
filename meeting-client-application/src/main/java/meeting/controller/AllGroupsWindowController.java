package meeting.controller;

import meeting.StageLoader;
import meeting.api.request.MembershipRequest;
import meeting.api.response.GroupListResponse;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import meeting.model.Group;
import meeting.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class AllGroupsWindowController {

    @FXML ListView<Group> listView;
    @FXML Button applyButton;

    private Group pickedGroup;
    private Client client;
    private User user;

    @FXML
    public void initialize() {
        applyButton.setDisable(true);
        Platform.runLater(this::refreshClicked);
    }

    @FXML
    private void applyClicked() {

        // robie JSONa
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        MembershipRequest membershipRequest = MembershipRequest.builder()
                .userId(user.getId())
                .groupId(pickedGroup.getId())
                .build();

        String requestString = RequestFlag.MEMBREQ.toString() + gson.toJson(membershipRequest);

//        String membershipResponseString = meeting.client.sendRequestRecResponse(requestString);

        String membershipResponseString = ResponseFlag.MEMBREQ.toString();

        if(membershipResponseString.substring(0, 7).equals(ResponseFlag.__ERROR.toString())) {
            // TODO request o membership sie nie udal
            return;
        }

        // TODO jesli juz aplikowal do grupy to teraz to sprawdzic idac komunikat, albo blokowac button po wybraniu grupy do ktorej juz aplikowal
        // Tomek: prosciej: nie pobierac grup do ktorych uzytkownik juz nalezy

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText("Membership request send to leader of group");
        alert.showAndWait();
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
        // TODO wyslanie requesta o wylogowanie, ale czy potrzeba???

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

        String requestString = RequestFlag.GRPLIST.toString();

//        String groupResponseString = meeting.client.sendRequestRecResponse(requestString);

        String groupResponseString = ResponseFlag.GRPLIST.toString() +
                "{\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"id\": \"4\",\n" +
                "      \"name\": \"MAD\",\n" +
                "      \"leader\": \"Stronkowski\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": \"3\",\n" +
                "      \"name\": \"FO\",\n" +
                "      \"leader\": \"Gradowski\"\n" +
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
    }

    void setClient(Client client) {
        this.client = client;
    }

    void setUser(User user) {
        this.user = user;
    }
}