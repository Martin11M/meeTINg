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
import meeting.api.request.NewCommentRequest;
import meeting.api.request.OfferListRequest;
import meeting.api.response.NewCommentResponse;
import meeting.api.response.OfferListResponse;
import meeting.client.Client;
import meeting.enums.RequestFlag;
import meeting.enums.ResponseFlag;
import meeting.model.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static meeting.enums.SystemRole.USER;

public class OffersWindowController {

    @FXML public Button signOutButton;
    @FXML public Button returnButton;
    @FXML public Button refreshButton;
    @FXML public Button createButton;
    @FXML public Button acceptButton;
    @FXML public Button proposeButton;
    @FXML public Button voteButton;
    @FXML public Button commentButton;

    @FXML public TableView<FormattedOffer> offersTable;
    @FXML public TableColumn<FormattedOffer, String> offerDate;
    @FXML public TableColumn<FormattedOffer, Integer> offerVotes;

    @FXML public TableView<FormattedOffer> proposalsTable;
    @FXML public TableColumn<FormattedOffer, String> proposalDate;
    @FXML public TableColumn<FormattedOffer, Integer> proposalVotes;

    @FXML public ListView<String> commentsList;
    @FXML public Label roleInfoLabel;

    private Group pickedGroup;
    private Event pickedEvent;
    private Client client;
    private User user;

    private List<Offer> acceptedOffers = new ArrayList<>();
    private List<Offer> proposals = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();

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
                acceptButton.setDisable(true);
                roleInfoLabel.setText("Logged as " + user.getUsername() + " (User)");
            }
            else {
                roleInfoLabel.setText("Logged as " + user.getUsername() + " (Team Leader)");
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
                "      \"postDate\" : \"2019-05-27T20:30:00.000000\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 256,\n" +
                "      \"username\" : \"pawelek z ustronia\",\n" +
                "      \"message\" : \"ustronalia to gUWno niesamowite\",\n" +
                "      \"postDate\" : \"2019-05-28T16:32:25.620031\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 257,\n" +
                "      \"username\" : \"Maria (Lwow)\",\n" +
                "      \"message\" : \"w paszczu pisiont, w zopu sto\",\n" +
                "      \"postDate\" : \"2019-05-28T20:58:10.326503\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        OfferListResponse offerListResponse = gson.fromJson(fakeResponse, OfferListResponse.class);

        if(offerListResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Error response for EVNTOFR");
            alert.show();
            return;
        }

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

        offerListResponse.getComments().forEach(comment -> {
            Comment c = Comment.builder()
                    .id(comment.getId())
                    .username(comment.getUsername())
                    .message(comment.getMessage())
                    .postDate(LocalDateTime.parse(comment.getPostDate()))
                    .build();
            comments.add(c);
        });

        fillTables(acceptedOffers, proposals, comments);
    }

    private void initCols() {
        offerDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        offerVotes.setCellValueFactory(new PropertyValueFactory<>("votesCount"));
        proposalDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        proposalVotes.setCellValueFactory(new PropertyValueFactory<>("votesCount"));
    }

    private void fillTables( List<Offer> acceptedOffers, List<Offer> proposals, List<Comment> comments) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

        List<FormattedOffer> formattedAcceptedOffers = new ArrayList<>();
        List<FormattedOffer> formattedProposals = new ArrayList<>();
        List<String> formattedComments = new ArrayList<>();

        acceptedOffers.forEach(offer -> formattedAcceptedOffers.add(new FormattedOffer(offer.getStartDate().format(formatter), offer.getVotesCount())));
        proposals.forEach(proposal -> formattedProposals.add(new FormattedOffer(proposal.getStartDate().format(formatter), proposal.getVotesCount())));
        comments.forEach(comment -> formattedComments.add(comment.getPostDate().format(formatter) + " " + comment.getUsername() + ": " + comment.getMessage()));

        offersTable.getItems().clear();
        proposalsTable.getItems().clear();
        commentsList.getItems().clear();

        offersTable.getItems().addAll(formattedAcceptedOffers);
        proposalsTable.getItems().addAll(formattedProposals);
        commentsList.getItems().addAll(formattedComments);
    }

    private void addComment(String comment) {

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        LocalDateTime postDate = LocalDateTime.now();

        NewCommentRequest newCommentRequest = NewCommentRequest.builder()
                .flag(RequestFlag.COMMENT.toString())
                .userId(user.getId())
                .eventId(pickedEvent.getId())
                .message(comment)
                .postDate(postDate.toString())
                .build();

        String request = gson.toJson(newCommentRequest);
//        String response = client.sendRequestRecResponse(request);

        // fake response:

        String fakeResponse = "{\n" +
                "  \"flag\": \"COMMENT\",\n" +
                "  \"commentId\": 603\n" +
                "}";

        NewCommentResponse newCommentResponse = gson.fromJson(fakeResponse, NewCommentResponse.class);

        if(newCommentResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Error response for COMMENT");
            alert.show();
            return;
        }

        comments.add(
                Comment.builder()
                .id(newCommentResponse.getCommentId())
                .username(user.getUsername())
                .message(comment)
                .postDate(postDate)
                .build()
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        String formattedComment = postDate.format(formatter) + " " + user.getUsername() + ": " + comment;
        commentsList.getItems().add(formattedComment);
    }

    public void createClicked(ActionEvent actionEvent) {
    }

    public void acceptClicked(ActionEvent actionEvent) {
    }

    public void proposeClicked(ActionEvent mouseEvent) {
    }

    public void voteClicked(ActionEvent actionEvent) {
    }

    public void commentClicked(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Comment");
        dialog.setHeaderText("Add new comment to " + pickedEvent.getName());

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::addComment);
    }

    public void acceptedOffersTableClicked(MouseEvent mouseEvent) {
    }

    public void proposalsTableClicked(MouseEvent mouseEvent) {
    }

    public class FormattedOffer {

        private String startDate;

        private int votesCount;

        FormattedOffer(String startDate, int votesCount) {
            this.startDate = startDate;
            this.votesCount = votesCount;
        }

        public String getStartDate() {
            return startDate;
        }

        public int getVotesCount() {
            return votesCount;
        }
    }
}
