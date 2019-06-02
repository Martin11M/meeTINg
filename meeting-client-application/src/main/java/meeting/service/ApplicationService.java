package meeting.service;


import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import meeting.Main;
import meeting.controller.*;

import javafx.event.ActionEvent;
import meeting.model.Group;
import meeting.model.User;
import meeting.serializer.Serializer;

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

    public static void signOut(ActionEvent actionEvent, Class theClass, Serializer serializer) {
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
                    loginWindowController.setSerializer(serializer);
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

    public static void loadPreviousWindow(ActionEvent actionEvent, Class theClass, Serializer serializer, User user, Group group, String resource) {
        FXMLLoader fxmlLoader = new FXMLLoader(theClass.getResource(resource));
        ApplicationService.loadStage((Stage)((Node) actionEvent.getSource()).getScene().getWindow(), fxmlLoader);
        if (theClass == AllGroupsWindowController.class || theClass == EventsWindowController.class || theClass == RequestsReviewWindowController.class) {
            prepareGroupsWindowController(fxmlLoader, serializer, user);
        }
        else if (theClass == OffersWindowController.class) {
            prepareEventsWindowController(fxmlLoader, serializer, user, group);
        }
    }

    private static void prepareGroupsWindowController(FXMLLoader fxmlLoader, Serializer serializer, User user) {
        GroupsWindowController groupsWindowController = fxmlLoader.getController();
        groupsWindowController.setSerializer(serializer);
        groupsWindowController.setUser(user);
    }

    private static void prepareEventsWindowController(FXMLLoader fxmlLoader, Serializer serializer, User user, Group group) {
        EventsWindowController eventsWindowController = fxmlLoader.getController();
        eventsWindowController.setPickedGroup(group);
        eventsWindowController.setSerializer(serializer);
        eventsWindowController.setUser(user);
    }
}
