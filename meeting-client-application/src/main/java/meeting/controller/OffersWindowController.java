package meeting.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import meeting.StageLoader;
import meeting.api.request.OfferListRequest;
import meeting.api.response.OfferListResponse;
import meeting.client.Client;
import meeting.enums.RequestFlag;
import meeting.enums.ResponseFlag;
import meeting.model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static meeting.enums.SystemRole.USER;

public class OffersWindowController {

    @FXML public Button signOutButton;
    @FXML public Button returnButton;
    @FXML public Button refreshButton;
    @FXML public Button createButton;
    @FXML public Button proposalsButton;

    @FXML public TableView<Offer> offersTable;
    @FXML public TableColumn<Offer, String> offerDate;
    @FXML public TableColumn<Offer, Integer> offerVotes;

    @FXML public TableView<Offer> proposalsTable;
    @FXML public TableColumn<Offer, String> proposalDate;
    @FXML public TableColumn<Offer, Integer> proposalVotes;

    @FXML public TableView<Comment> commentsTable;
    @FXML public TableColumn<Comment, String> commentDate;
    @FXML public TableColumn<Comment, String> commentUsername;
    @FXML public TableColumn<Comment, String> commentMessage;

    private Group pickedGroup;
    private Event pickedEvent;
    private Client client;
    private User user;

    void setPickedGroup(Group pickedGroup) {
        this.pickedGroup = pickedGroup;
    }

    void setPickedEvent(Event pickedEvent) {
        this.pickedEvent = pickedEvent;
    }

    void setClient(Client client) {
        this.client = client;
    }

    void setUser(User user) {
        this.user = user;
    }

    @FXML
    public void initialize() {
        initCols();

        Platform.runLater(() ->{
            if(user.getSystemRole() == USER) {
                createButton.setDisable(true);
            }
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/EventsWindow.fxml"));
            StageLoader.loadStage((Stage)((Node) actionEvent.getSource()).getScene().getWindow(), fxmlLoader);
            EventsWindowController eventsWindowController = fxmlLoader.getController();
            eventsWindowController.setPickedGroup(pickedGroup);
            eventsWindowController.setClient(client);
            eventsWindowController.setUser(user);
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

        OfferListRequest groupRequest = OfferListRequest.builder()
                .flag(RequestFlag.EVNTOFR.toString())
                .eventId(pickedEvent.getId())
                .build();

        String request = gson.toJson(groupRequest);
//        String response = client.sendRequestRecResponse(request);

        // fake response:

        String fakeResponse = "{\n" +
                "  \"flag\": \"EVNTOFR\",\n" +
                "  \"offers\": [\n" +
                "    {\n" +
                "      \"id\" : 101,\n" +
                "      \"startDate\" : \"2019-05-25T16:00:00.000000\",\n" +
                "      \"votesCount\" : 3,\n" +
                "      \"acceptedOffer\" : true\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 102,\n" +
                "      \"startDate\" : \"2019-05-25T18:00:00.000000\",\n" +
                "      \"votesCount\" : 5,\n" +
                "      \"acceptedOffer\" : false\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 103,\n" +
                "      \"startDate\" : \"2019-05-28T16:00:00.000000\",\n" +
                "      \"votesCount\" : 3,\n" +
                "      \"acceptedOffer\" : true\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 104,\n" +
                "      \"startDate\" : \"2019-05-28T19:00:00.000000\",\n" +
                "      \"votesCount\" : 6,\n" +
                "      \"acceptedOffer\" : false\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 105,\n" +
                "      \"startDate\" : \"2019-05-28T20:30:00.000000\",\n" +
                "      \"votesCount\" : 9,\n" +
                "      \"acceptedOffer\" : true\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 106,\n" +
                "      \"startDate\" : \"2019-06-03T18:00:00.000000\",\n" +
                "      \"votesCount\" : 9,\n" +
                "      \"acceptedOffer\" : true\n" +
                "    }\n" +
                "  ],\n" +
                "  \"comments\": [\n" +
                "    {\n" +
                "      \"id\" : 255,\n" +
                "      \"username\" : \"piotrek wariat\",\n" +
                "      \"message\" : \"siemano wariatyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy\",\n" +
                "      \"postDate\" : \"2019-05-28T20:30:00.000000\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 256,\n" +
                "      \"username\" : \"pawelek z ustronia\",\n" +
                "      \"message\" : \"ustronalia to gUWno niesamowite\",\n" +
                "      \"postDate\" : \"2019-05-28T20:30:00.000000\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 257,\n" +
                "      \"username\" : \"Maria (Lwow)\",\n" +
                "      \"message\" : \"w paszczu pisiont, w zopu sto\",\n" +
                "      \"postDate\" : \"2019-05-28T20:30:00.000000\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        System.out.println(fakeResponse);

        OfferListResponse offerListResponse = gson.fromJson(fakeResponse, OfferListResponse.class);

        if(offerListResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Error response for EVNTOFR");
            alert.show();
            return;
        }

        List<Offer> acceptedOffers = new ArrayList<>();
        List<Offer> proposals = new ArrayList<>();

        offerListResponse.getOffers().forEach(offer -> {
            Offer o = Offer.builder()
                    .id(offer.getId())
                    .startDate(LocalDateTime.parse(offer.getStartDate()))
                    .votesCount(offer.getVotesCount())
                    .acceptedOffer(offer.isAcceptedOffer())
                    .build();
            if(offer.isAcceptedOffer()) acceptedOffers.add(o);
            else proposals.add(o);
        });

        List<Comment> comments = new ArrayList<>();

        offerListResponse.getComments().forEach(comment -> {
            Comment c = Comment.builder()
                    .id(comment.getId())
                    .username(comment.getUsername())
                    .message(comment.getMessage())
                    .postDate(LocalDateTime.parse(comment.getPostDate()))
                    .build();
            comments.add(c);
        });

        offersTable.getItems().clear();
        proposalsTable.getItems().clear();
        commentsTable.getItems().clear();

        offersTable.getItems().addAll(acceptedOffers);
        proposalsTable.getItems().addAll(proposals);
        commentsTable.getItems().addAll(comments);
    }

    private void initCols() {
        offerDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        offerVotes.setCellValueFactory(new PropertyValueFactory<>("votesCount"));
        proposalDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        proposalVotes.setCellValueFactory(new PropertyValueFactory<>("votesCount"));
        commentDate.setCellValueFactory(new PropertyValueFactory<>("postDate"));
        commentUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        commentMessage.setCellValueFactory(new PropertyValueFactory<>("message"));
    }

    public void proposalsClicked(ActionEvent mouseEvent) {
    }

    public void commentsClicked(MouseEvent mouseEvent) {
    }

    public void createClicked(ActionEvent actionEvent) {
    }

    public void acceptedOffersTableClicked(MouseEvent mouseEvent) {
    }

    public void proposalsTableClicked(MouseEvent mouseEvent) {
    }

    public void commentsTableClicked(MouseEvent mouseEvent) {
    }
}
