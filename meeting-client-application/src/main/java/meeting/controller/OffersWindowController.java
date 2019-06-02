package meeting.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import meeting.api.request.*;
import meeting.api.response.FlagResponse;
import meeting.api.response.NewCommentResponse;
import meeting.api.response.OfferListResponse;
import meeting.api.response.OfferResponse;
import meeting.api.ConnectionManager;
import meeting.enums.RequestFlag;
import meeting.enums.ResponseFlag;
import meeting.enums.SystemRole;
import meeting.model.*;
import meeting.serializer.Serializer;
import meeting.service.ApplicationService;

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
    @FXML public Button confirmButton;

    @FXML public TableView<FormattedOffer> offersTable;
    @FXML public TableColumn<FormattedOffer, String> offerDate;
    @FXML public TableColumn<FormattedOffer, Integer> offerVotes;

    @FXML public TableView<FormattedOffer> proposalsTable;
    @FXML public TableColumn<FormattedOffer, String> proposalDate;
    @FXML public TableColumn<FormattedOffer, Integer> proposalVotes;

    @FXML public ListView<String> commentsList;
    @FXML public Label roleInfoLabel;
    @FXML public Label confirmedOfferLabel;

    private Group pickedGroup;

    private Event pickedEvent;
    private ConnectionManager connectionManager;
    private User user;

    private FormattedOffer pickedOffer;

    private Serializer serializer;

    private List<Offer> acceptedOffers = new ArrayList<>();
    private List<Offer> proposals = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();
    private List<Vote> votes = new ArrayList<>();

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    void setPickedGroup(Group pickedGroup) {
        this.pickedGroup = pickedGroup;
    }

    void setPickedEvent(Event pickedEvent) {
        this.pickedEvent = pickedEvent;
    }

    void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
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
                roleInfoLabel.setText("Logged as " + user.getUsername() + " (User)");
            }
            else {
                roleInfoLabel.setText("Logged as " + user.getUsername() + " (Team Leader)");
                proposeButton.setDisable(true);
            }
            serializer = new Serializer(connectionManager);
            refreshClicked();
        });
    }

    @FXML
    public void signOutClicked(ActionEvent actionEvent) {
        ApplicationService.signOut(actionEvent, OffersWindowController.class, connectionManager);
    }

    @FXML
    public void returnClicked(ActionEvent actionEvent) {
        try {
            ApplicationService.loadPreviousWindow(actionEvent, OffersWindowController.class, connectionManager, user, pickedGroup, "/fxml/EventsWindow.fxml");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void refreshClicked() {

        OfferListRequest groupRequest = OfferListRequest.builder()
                .flag(RequestFlag.EVNTOFR.toString())
                .eventId(pickedEvent.getId())
                .userId(user.getId())
                .build();

        OfferListResponse offerListResponse = serializer.refreshOffers(groupRequest);

        if(offerListResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            ApplicationService.showErrorAlert("Error response for EVNTOFR");
            return;
        }

        acceptedOffers.clear();
        proposals.clear();
        comments.clear();
        votes.clear();

        offerListResponse.getOffers().forEach(offer -> {
            Offer o = Offer.builder()
                    .id(offer.getId())
                    .startDate(LocalDateTime.parse(offer.getStartDate(), formatter))
                    .votesCount(offer.getVotesCount())
                    .acceptedOffer(offer.isAcceptedOffer())
                    .confirmedOffer(offer.isConfirmedOffer())
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

        offerListResponse.getVotes().forEach(vote -> {
            Vote v = Vote.builder()
                    .id(vote.getId())
                    .offerId(vote.getOfferId())
                    .userId(user.getId())
                    .build();
            votes.add(v);
        });

        fillTables(acceptedOffers, proposals, comments);
        disableActionButtons();
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

        acceptedOffers.forEach(offer -> {
            if(offer.isConfirmedOffer()) confirmedOfferLabel.setText("Meeting at: " + offer.getStartDate().format(formatter));
            formattedAcceptedOffers.add(new FormattedOffer(offer.getId(), offer.getStartDate().format(formatter), offer.getVotesCount(), offer.isConfirmedOffer()));
        });
        proposals.forEach(proposal -> formattedProposals.add(new FormattedOffer(proposal.getId(), proposal.getStartDate().format(formatter), proposal.getVotesCount(), proposal.isConfirmedOffer())));
        comments.forEach(comment -> formattedComments.add(comment.getPostDate().format(formatter) + " " + comment.getUsername() + ": " + comment.getMessage()));

        offersTable.getItems().clear();
        proposalsTable.getItems().clear();
        commentsList.getItems().clear();

        offersTable.getItems().addAll(formattedAcceptedOffers);
        proposalsTable.getItems().addAll(formattedProposals);
        commentsList.getItems().addAll(formattedComments);
    }

    private void addComment(String comment) {

        LocalDateTime postDate = LocalDateTime.now();

        NewCommentRequest newCommentRequest = NewCommentRequest.builder()
                .flag(RequestFlag.COMMENT.toString())
                .userId(user.getId())
                .eventId(pickedEvent.getId())
                .message(comment)
                .postDate(postDate.format(formatter))
                .build();

        NewCommentResponse newCommentResponse = serializer.createComment(newCommentRequest);

        if(newCommentResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            ApplicationService.showErrorAlert("Error response for COMMENT");
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

        OfferResponse offerResponse = serializer.createOffer(offerRequest);

        if(offerResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            ApplicationService.showErrorAlert("Error response for " + flag.toString());
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
            FormattedOffer formattedOffer = new FormattedOffer(offerResponse.getOfferId(), proposalDate.format(formatter), 0, false);
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
            FormattedOffer formattedOffer = new FormattedOffer(offerResponse.getOfferId(), proposalDate.format(formatter), 0, false);
            offersTable.getItems().add(formattedOffer);
        }

        refreshClicked();
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

        OfferAcceptRequest offerAcceptRequest = OfferAcceptRequest.builder()
                .flag(RequestFlag.OFRACPT.toString())
                .offerId(pickedOffer.getId())
                .build();

        FlagResponse flagResponse = serializer.acceptOffer(offerAcceptRequest);

        if(flagResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            ApplicationService.showErrorAlert("Error response for OFRACPT");
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

        VoteRequest voteRequest = VoteRequest.builder()
                .flag(RequestFlag.NEWVOTE.toString())
                .offerId(pickedOffer.getId())
                .userId(user.getId())
                .build();

        FlagResponse flagResponse = serializer.createVote(voteRequest);

        if(flagResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            ApplicationService.showErrorAlert("Error response for NEWVOTE");
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
    public void confirmClicked() {

        OfferConfirmRequest offerConfirmRequest = OfferConfirmRequest.builder()
                .flag(RequestFlag.CFRMOFR.toString())
                .offerId(pickedOffer.getId())
                .build();

        FlagResponse flagResponse = serializer.confirmOffer(offerConfirmRequest);

        if(flagResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            ApplicationService.showErrorAlert("Error response for CFRMOFR");
            return;
        }

        refreshClicked();
    }

    @FXML
    public void acceptedOffersTableClicked() {
        if(offersTable.getSelectionModel().getSelectedItem() != null &&
                offersTable.getSelectionModel().getSelectedItem() != pickedOffer) {

            disableActionButtons();
            pickedOffer = offersTable.getSelectionModel().getSelectedItem();
            if (user.getSystemRole() == SystemRole.TEAM_LEADER && !pickedOffer.isConfirmedOffer()) confirmButton.setDisable(false);
            if (voteAllowed()) voteButton.setDisable(false);
        }
    }

    @FXML
    public void proposalsTableClicked() {
        if(proposalsTable.getSelectionModel().getSelectedItem() != null &&
                proposalsTable.getSelectionModel().getSelectedItem() != pickedOffer) {

            disableActionButtons();
            pickedOffer = proposalsTable.getSelectionModel().getSelectedItem();
            if (user.getSystemRole() == SystemRole.TEAM_LEADER) acceptButton.setDisable(false);
            if (voteAllowed()) {
                voteButton.setDisable(false);
            }
        }
    }

    private boolean voteAllowed() {
        for (Vote vote : votes) {
            if (vote.getOfferId() == pickedOffer.getId()) {
                return false;
            }
        }
        return true;
    }

    private void disableActionButtons() {
        voteButton.setDisable(true);
        acceptButton.setDisable(true);
        confirmButton.setDisable(true);
    }

    public class FormattedOffer {

        private long id;

        private String startDate;

        private int votesCount;

        private boolean confirmedOffer;

        FormattedOffer(long id, String startDate, int votesCount, boolean confirmedOffer) {
            this.id = id;
            this.startDate = startDate;
            this.votesCount = votesCount;
            this.confirmedOffer = confirmedOffer;
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

        public boolean isConfirmedOffer() {
            return confirmedOffer;
        }
    }
}
