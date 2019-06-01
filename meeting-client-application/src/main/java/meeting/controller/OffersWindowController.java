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
import javafx.stage.Stage;
import meeting.StageLoader;
import meeting.api.request.*;
import meeting.api.response.FlagResponse;
import meeting.api.response.NewCommentResponse;
import meeting.api.response.OfferListResponse;
import meeting.api.response.OfferResponse;
import meeting.client.Client;
import meeting.enums.RequestFlag;
import meeting.enums.ResponseFlag;
import meeting.enums.SystemRole;
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

    private FormattedOffer pickedOffer;
    private FormattedOffer pickedProposal;

    private List<Offer> acceptedOffers = new ArrayList<>();
    private List<Offer> proposals = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
                proposeButton.setDisable(true);
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

        String response = client.sendRequestRecResponse(request);

        // fake response:

        /*String response = "{\n" +
                "  \"flag\": \"EVNTOFR\",\n" +
                "  \"offers\": [\n" +
                "    {\n" +
                "      \"id\" : 101,\n" +
                "      \"startDate\" : \"2019-05-25 16:00:00\",\n" +
                "      \"votesCount\" : 3,\n" +
                "      \"acceptedOffer\" : true\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 102,\n" +
                "      \"startDate\" : \"2019-05-25 18:00:00\",\n" +
                "      \"votesCount\" : 5,\n" +
                "      \"acceptedOffer\" : false\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 103,\n" +
                "      \"startDate\" : \"2019-05-28 16:00:00\",\n" +
                "      \"votesCount\" : 3,\n" +
                "      \"acceptedOffer\" : true\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 104,\n" +
                "      \"startDate\" : \"2019-05-28 19:00:00\",\n" +
                "      \"votesCount\" : 6,\n" +
                "      \"acceptedOffer\" : false\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 105,\n" +
                "      \"startDate\" : \"2019-05-28 20:30:00\",\n" +
                "      \"votesCount\" : 9,\n" +
                "      \"acceptedOffer\" : true\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 106,\n" +
                "      \"startDate\" : \"2019-06-03 18:00:00\",\n" +
                "      \"votesCount\" : 9,\n" +
                "      \"acceptedOffer\" : true\n" +
                "    }\n" +
                "  ],\n" +
                "  \"comments\": [\n" +
                "    {\n" +
                "      \"id\" : 255,\n" +
                "      \"username\" : \"piotrek wariat\",\n" +
                "      \"message\" : \"siemano wariatyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyyy\",\n" +
                "      \"postDate\" : \"2019-05-27 20:30:00\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 256,\n" +
                "      \"username\" : \"pawelek z ustronia\",\n" +
                "      \"message\" : \"ustronalia to gUWno niesamowite\",\n" +
                "      \"postDate\" : \"2019-05-28 16:32:25\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\" : 257,\n" +
                "      \"username\" : \"Maria (Lwow)\",\n" +
                "      \"message\" : \"w paszczu pisiont, w zopu sto\",\n" +
                "      \"postDate\" : \"2019-05-28 20:58:10\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
*/

        OfferListResponse offerListResponse = gson.fromJson(response, OfferListResponse.class);

        if(offerListResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Error response for EVNTOFR");
            alert.show();
            return;
        }

        acceptedOffers.clear();
        proposals.clear();
        comments.clear();

        offerListResponse.getOffers().forEach(offer -> {
            Offer o = Offer.builder()
                    .id(offer.getId())
                    .startDate(LocalDateTime.parse(offer.getStartDate(), formatter))
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
                    .postDate(LocalDateTime.parse(comment.getPostDate(), formatter))
                    .build();
            comments.add(c);
        });

        fillTables(acceptedOffers, proposals, comments);

        acceptButton.setDisable(true);
        voteButton.setDisable(true);
    }

    private void initCols() {
        offerDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        offerVotes.setCellValueFactory(new PropertyValueFactory<>("votesCount"));
        proposalDate.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        proposalVotes.setCellValueFactory(new PropertyValueFactory<>("votesCount"));
    }

    private void fillTables(List<Offer> acceptedOffers, List<Offer> proposals, List<Comment> comments) {
        List<FormattedOffer> formattedAcceptedOffers = new ArrayList<>();
        List<FormattedOffer> formattedProposals = new ArrayList<>();
        List<String> formattedComments = new ArrayList<>();

        acceptedOffers.forEach(offer -> formattedAcceptedOffers.add(new FormattedOffer(offer.getId(), offer.getStartDate().format(formatter), offer.getVotesCount())));
        proposals.forEach(proposal -> formattedProposals.add(new FormattedOffer(proposal.getId(), proposal.getStartDate().format(formatter), proposal.getVotesCount())));
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
                .postDate(postDate.format(formatter))
                .build();

        String request = gson.toJson(newCommentRequest);
        System.out.println(request);
        String response = client.sendRequestRecResponse(request);
        System.out.println(response);

        // fake response:

        /*String fakeResponse = "{\n" +
                "  \"flag\": \"COMMENT\",\n" +
                "  \"commentId\": 603\n" +
                "}";*/

        NewCommentResponse newCommentResponse = gson.fromJson(response, NewCommentResponse.class);

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

        String formattedComment = postDate.format(formatter) + " " + user.getUsername() + ": " + comment;
        commentsList.getItems().add(formattedComment);
    }

    private void newOffer(String offer, RequestFlag flag) {

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        String delimiters = "[-: ]";
        String[] parameters = offer.split(delimiters);
        List<Integer> convertedParameters = new ArrayList<>();
        for (String parameter : parameters) {
            convertedParameters.add(Integer.parseInt(parameter));
        }

        LocalDateTime proposalDate = LocalDateTime.of(convertedParameters.get(0), convertedParameters.get(1),
                convertedParameters.get(2), convertedParameters.get(3), convertedParameters.get(4));

        OfferRequest offerRequest = OfferRequest.builder()
                .flag(flag.toString())
                .eventId(pickedEvent.getId())
                .userId(user.getId())
                .date(proposalDate.format(formatter))
                .build();

        String request = gson.toJson(offerRequest);
        System.out.println(request);
        String response = client.sendRequestRecResponse(request);

        // fake response:

        String fakeResponse = "{\n" +
                "  \"flag\": \"PROPOFR\",\n" +
                "  \"offerId\": 69\n" +
                "}";

        OfferResponse offerResponse = gson.fromJson(response, OfferResponse.class);

        if(offerResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Error response for " + flag.toString());
            alert.show();
            return;
        }

        if (flag == RequestFlag.PROPOFR) {
            proposals.add(
                    Offer.builder()
                            .id(offerResponse.getOfferId())
                            .startDate(proposalDate)
                            .votesCount(0)
                            .acceptedOffer(false)
                            .build()
            );
            FormattedOffer formattedOffer = new FormattedOffer(offerResponse.getOfferId(), proposalDate.format(formatter), 0);
            proposalsTable.getItems().add(formattedOffer);
        }
        else if (flag == RequestFlag.MAKEOFR) {
            acceptedOffers.add(
                    Offer.builder()
                            .id(offerResponse.getOfferId())
                            .startDate(proposalDate)
                            .votesCount(0)
                            .acceptedOffer(true)
                            .build()
            );
            FormattedOffer formattedOffer = new FormattedOffer(offerResponse.getOfferId(), proposalDate.format(formatter), 0);
            offersTable.getItems().add(formattedOffer);
        }
    }

    @FXML
    public void createClicked() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Offer");
        dialog.setHeaderText("Add new offer to " + pickedEvent.getName());
        dialog.setContentText("Date format: 2001-01-01 01:01");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(offer -> newOffer(offer, RequestFlag.MAKEOFR));
    }

    @FXML
    public void acceptClicked() {

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        OfferAcceptRequest offerAcceptRequest = OfferAcceptRequest.builder()
                .flag(RequestFlag.OFRACPT.toString())
                .offerId(pickedProposal.getId())
                .build();

        String request = gson.toJson(offerAcceptRequest);
        System.out.println(request);

        String response = client.sendRequestRecResponse(request);

        // fake response
        /*String response = "{\n" +
                "  \"flag\": \"OFRACPT\"\n" +
                "}";*/

        FlagResponse flagResponse = gson.fromJson(response, FlagResponse.class);

        if(flagResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Error response for OFRACPT");
            alert.show();
            return;
        }

        refreshClicked();
    }

    @FXML
    public void proposeClicked() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Proposal");
        dialog.setHeaderText("Add new proposal to " + pickedEvent.getName());
        dialog.setContentText("Date format: 2001-01-01 01:01");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(offer -> newOffer(offer, RequestFlag.PROPOFR));
    }

    @FXML
    public void voteClicked() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        // TODO moze byc pickedOffer zamiast pickedProposal
        VoteRequest voteRequest = VoteRequest.builder()
                .flag(RequestFlag.NEWVOTE.toString())
                .offerId(pickedProposal.getId())
                .userId(user.getId())
                .build();

        String request = gson.toJson(voteRequest);
        String response = client.sendRequestRecResponse(request);
        System.out.println(response);

        // fake response
        /*String response = "{\n" +
                "  \"flag\": \"NEWVOTE\"\n" +
                "}";*/

        FlagResponse flagResponse = gson.fromJson(response, FlagResponse.class);

        if(flagResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setHeaderText(null);
            alert.setContentText("Error response for NEWVOTE");
            alert.show();
            return;
        }

        refreshClicked();
    }

    @FXML
    public void commentClicked() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Comment");
        dialog.setHeaderText("Add new comment to " + pickedEvent.getName());

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::addComment);
    }

    @FXML
    public void acceptedOffersTableClicked() {
        if(offersTable.getSelectionModel().getSelectedItem() != null &&
                offersTable.getSelectionModel().getSelectedItem() != pickedOffer) {

            pickedOffer = offersTable.getSelectionModel().getSelectedItem();
            voteButton.setDisable(false);
        }
    }

    @FXML
    public void proposalsTableClicked() {
        if(proposalsTable.getSelectionModel().getSelectedItem() != null &&
                proposalsTable.getSelectionModel().getSelectedItem() != pickedProposal) {

            pickedProposal = proposalsTable.getSelectionModel().getSelectedItem();
            if (user.getSystemRole() == SystemRole.TEAM_LEADER) acceptButton.setDisable(false);
            voteButton.setDisable(false);
        }
    }

    public class FormattedOffer {

        private long id;

        private String startDate;

        private int votesCount;

        FormattedOffer(long id, String startDate, int votesCount) {
            this.id = id;
            this.startDate = startDate;
            this.votesCount = votesCount;
        }

        public long getId() {
            return id;
        }

        public String getStartDate() {
            return startDate;
        }

        public int getVotesCount() {
            return votesCount;
        }
    }
}
