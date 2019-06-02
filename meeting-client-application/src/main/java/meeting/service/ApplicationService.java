package meeting.service;


import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import meeting.Main;
import meeting.api.ConnectionManager;
import meeting.controller.*;

import javafx.event.ActionEvent;
import meeting.model.Group;
import meeting.model.User;

import java.util.Optional;

public class ApplicationService {

    public static void loadStage(Stage stage, FXMLLoader fxmlLoader)
    {
        try {
            AnchorPane root = fxmlLoader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().addAll(Main.class.getResource("/css/background.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("meeTINg ConnectionManager Application");
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void signOut(ActionEvent actionEvent, Class theClass, ConnectionManager connectionManager) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(null);
        alert.setContentText("Do you want to sign out?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            ButtonType reloadedResult = result.get();
            if (reloadedResult == ButtonType.OK){
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(theClass.getResource("/fxml/LoginWindow.fxml"));
                    loadStage((Stage)((Node) actionEvent.getSource()).getScene().getWindow(), fxmlLoader);
                    LoginWindowController loginWindowController = fxmlLoader.getController();
                    loginWindowController.setConnectionManager(connectionManager);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    public static void showInformationAlert(String title, String header) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    public static void loadPreviousWindow(ActionEvent actionEvent, Class theClass, ConnectionManager connectionManager, User user, Group group, String resource) {
        FXMLLoader fxmlLoader = new FXMLLoader(theClass.getResource(resource));
        ApplicationService.loadStage((Stage)((Node) actionEvent.getSource()).getScene().getWindow(), fxmlLoader);
        if (theClass == AllGroupsWindowController.class || theClass == EventsWindowController.class || theClass == RequestsReviewWindowController.class) {
            prepareGroupsWindowController(fxmlLoader, connectionManager, user);
        }
        else if (theClass == OffersWindowController.class) {
            prepareEventsWindowController(fxmlLoader, connectionManager, user, group);
        }
    }

    private static void prepareGroupsWindowController(FXMLLoader fxmlLoader, ConnectionManager connectionManager, User user) {
        GroupsWindowController groupsWindowController = fxmlLoader.getController();
        groupsWindowController.setConnectionManager(connectionManager);
        groupsWindowController.setUser(user);
    }

    private static void prepareEventsWindowController(FXMLLoader fxmlLoader, ConnectionManager connectionManager, User user, Group group) {
        EventsWindowController eventsWindowController = fxmlLoader.getController();
        eventsWindowController.setPickedGroup(group);
        eventsWindowController.setConnectionManager(connectionManager);
        eventsWindowController.setUser(user);
    }
}
