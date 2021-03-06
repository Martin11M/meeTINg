package meeting.controller;

import javafx.scene.control.*;
import meeting.api.request.EventListRequest;
import meeting.api.request.NewEventRequest;
import meeting.api.response.EventListResponse;
import meeting.api.response.NewEventResponse;
import meeting.enums.RequestFlag;
import meeting.enums.ResponseFlag;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.stage.Stage;
import meeting.model.Event;
import meeting.model.Group;
import meeting.model.User;
import meeting.serializer.Serializer;
import meeting.service.ApplicationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static meeting.enums.SystemRole.USER;


public class EventsWindowController {

    @FXML public Button createButton;
    @FXML public Button refreshButton;
    @FXML public Button returnButton;
    @FXML public Button signOutButton;
    @FXML public ListView<Event> eventList;
    public Label roleInfoLabel;

    private Group pickedGroup;
    private Event pickedEvent;
    private User user;

    private Serializer serializer;

    @FXML
    public void initialize() {
        Platform.runLater(() -> {
            if(user.getSystemRole() == USER) {
                createButton.setDisable(true);
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
        ApplicationService.signOut(actionEvent, EventsWindowController.class, serializer);
    }

    @FXML
    public void returnClicked(ActionEvent actionEvent) {
        try {
            ApplicationService.loadPreviousWindow(actionEvent, EventsWindowController.class, serializer, user, null, "/fxml/GroupsWindow.fxml");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void refreshClicked() {

        EventListRequest eventListRequest = EventListRequest.builder()
                .flag(RequestFlag.GRPEVNT.toString())
                .groupId(pickedGroup.getId())
                .build();

        EventListResponse eventListResponse = serializer.refreshEvents(eventListRequest);

        if(eventListResponse.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            ApplicationService.showErrorAlert("Error response for GRPEVNT");
            return;
        }

        List<Event> events = new ArrayList<>();

        eventListResponse.getItems().forEach(element -> {
            Event e = Event.builder()
                    .id(element.getId())
                    .name(element.getName())
                    .build();
            events.add(e);
        });

        eventList.getItems().clear();
        eventList.getItems().addAll(events);
    }

    @FXML
    public void createClicked() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New event");
        dialog.setHeaderText(null);
        dialog.setContentText("Please enter event name:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            if (name.equals("")) ApplicationService.showErrorAlert("Field can not be empty!");
            else {
                sendNewEventRequest(name);
            }
        });
    }

    private void sendNewEventRequest(String name) {

        NewEventRequest newEventRequest = NewEventRequest.builder()
                .flag(RequestFlag.MAKEEVT.toString())
                .groupId(pickedGroup.getId())
                .eventName(name)
                .build();

        NewEventResponse newEventResponse = serializer.createEvent(newEventRequest);

        if(newEventRequest.getFlag().equals(ResponseFlag.__ERROR.toString())) {
            ApplicationService.showErrorAlert("Cannot do request MAKEEVT");
            return;
        }

        Event e = Event.builder()
                .id(newEventResponse.getId())
                .name(newEventResponse.getName())
                .build();

        eventList.getItems().add(e);
    }

    @FXML
    public void eventClicked(javafx.event.Event event) {
        if(eventList.getSelectionModel().getSelectedItem() != null &&
                eventList.getSelectionModel().getSelectedItem() != pickedEvent) {

            pickedEvent = eventList.getSelectionModel().getSelectedItem();

            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/OffersWindow.fxml"));
                ApplicationService.loadStage((Stage)((Node) event.getSource()).getScene().getWindow(), fxmlLoader);
                OffersWindowController offersWindowController = fxmlLoader.getController();
                offersWindowController.setPickedGroup(pickedGroup);
                offersWindowController.setPickedEvent(pickedEvent);
                offersWindowController.setSerializer(serializer);
                offersWindowController.setUser(user);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setPickedGroup(Group pickedGroup) {
        this.pickedGroup = pickedGroup;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setSerializer(Serializer serializer) {
        this.serializer = serializer;
    }
}
