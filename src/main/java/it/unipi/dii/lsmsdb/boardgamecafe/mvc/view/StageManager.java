package it.unipi.dii.lsmsdb.boardgamecafe.mvc.view;

import it.unipi.dii.lsmsdb.boardgamecafe.utils.config.SpringFXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.stage.Window;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/*
    This class is responsible for the management of the windows and scenes of the application.
    Provides methods to create views, show windows and visualize potential error messages.
    SpringFXMLLoader is employed to load FXML files and create scenes for the application's views.
 */
public class StageManager {

    private final Stage primaryStage;
    private final SpringFXMLLoader springFXMLLoader;

    public StageManager(SpringFXMLLoader springFXMLLoader, Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.springFXMLLoader = springFXMLLoader;
    }

    public void switchScene(final FxmlView view) {
        Parent viewRoot = loadViewNode(view.getFxmlFile());

        // Getting the current stage of the current element
        Stage currentStage = (Stage) Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);

        if (currentStage != null) {
            currentStage.close();
        }

        // Creating a new stage for the new scene
        Stage newStage = new Stage();
        Scene newScene = new Scene(viewRoot);
        newStage.setScene(newScene);
        newStage.setTitle(view.getTitle());
        newStage.show();
    }

    public void closeStage() {
        Stage currentStage = (Stage) Stage.getWindows().stream()
                .filter(window -> window.isFocused())
                .findFirst()
                .orElse(null);

        if (currentStage != null) {
            currentStage.close();
        }
    }

    public Parent loadViewNode(String fxmlFilePath) {
        Parent rootNode = null;
        try {
            rootNode = springFXMLLoader.load(fxmlFilePath);
            Objects.requireNonNull(rootNode, "A Root FXML node must not be null");
        } catch (Exception e){
            System.err.println("[ERROR] loadViewNode()@StageManager.java raised an exception: " + e.getMessage());
        }
        return rootNode;
    }

    public Stage showWindow(final FxmlView window) {
        try {
            Parent viewRoot = loadViewNode(window.getFxmlFile());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(window.getTitle());
            stage.setScene(new Scene(viewRoot));
            stage.show();
            return stage;
        } catch (Exception e) {
            System.err.println("[ERROR] showWindow()@StageManager.java raised an exception: " + e.getMessage());
            return null;
        }
    }

    public void showInfoMessage(String title, String message) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(650);
        window.setMinHeight(200);
        window.setOnCloseRequest(e -> window.close());

        Label label = new Label();
        label.setText(message);
        label.setFont(Font.font("Georgia Pro Cond Black", FontWeight.BOLD, FontPosture.REGULAR, 16));
        label.setTextFill(Color.rgb(24,46,88));
        label.setAlignment(Pos.CENTER);

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> window.close());

        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(label, closeButton);
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox);
        window.setScene(scene);
        window.show();
    }

    public boolean showDeleteBoardgameInfoMessage() {
        String title = "ATTENTION";
        String message = "Delete this boardgame? This action cannot be undone.";
        String okButtonString = "Delete Boardgame";
        String backButtonString = "Return to the Boardgames page";

        return displayInfoMessageAfterContentEditingOrDeletion(title, message, okButtonString, backButtonString);
    }

    public boolean showConfirmUpdateBoardgameInfoMessage() {
        String title = "ATTENTION";
        String message = "Some fields are empty. Do you want to update only the filled fields?";
        String okButtonString = "Save Changes";
        String backButtonString = "Return to Editing";

        return displayInfoMessageAfterContentEditingOrDeletion(title, message, okButtonString, backButtonString);
    }

    public boolean showConfirmUpdateBoardgameListInfoMessage(String listName) {
        String title = "ATTENTION";
        String message = "Some fields are empty. Do you want to update only " + listName + " and filled fields?";
        String okButtonString = "Save Changes";
        String backButtonString = "Return to Editing";

        return displayInfoMessageAfterContentEditingOrDeletion(title, message, okButtonString, backButtonString);
    }

    public boolean showConfirmDiscardEditBoardgameInfoMessage() {
        String title = "ATTENTION";
        String message = "Are you sure you want to discard changes? Your updates will be lost.";
        String okButtonString = "Discard Changes";
        String backButtonString = "Return to Editing";

        return displayInfoMessageAfterContentEditingOrDeletion(title, message, okButtonString, backButtonString);
    }

    public boolean showUpdatePostInfoMessage() {
        String title = "ATTENTION";
        String message = "Discard changes? Your updates will be lost.";
        String okButtonString = "Discard changes";
        String backButtonString = "Return to editing";

        return displayInfoMessageAfterContentEditingOrDeletion(title, message, okButtonString, backButtonString);
    }

    public boolean showDiscardReviewInfoMessage() {
        String title = "ATTENTION";
        String message = "Discard changes? What you wrote will be lost.";
        String okButtonString = "Discard Review";
        String backButtonString = "Return to Review";

        return displayInfoMessageAfterContentEditingOrDeletion(title, message, okButtonString, backButtonString);
    }

    public boolean showDiscardCommentInfoMessage() {
        String title = "ATTENTION";
        String message = "Discard changes? What you wrote will be lost.";
        String okButtonString = "Discard Comment";
        String backButtonString = "Return to Comment";

        return displayInfoMessageAfterContentEditingOrDeletion(title, message, okButtonString, backButtonString);
    }

    public boolean showDiscardBoardgameInfoMessage() {
        String title = "ATTENTION";
        String message = "Discard changes? What you wrote will be lost.";
        String okButtonString = "Discard Boardgame Creation";
        String backButtonString = "Return to Boardgame Creation Page";

        return displayInfoMessageAfterContentEditingOrDeletion(title, message, okButtonString, backButtonString);
    }

    public boolean showDeleteUserInfoMessage() {
        String title = "ATTENTION";
        String message = "This user will be deleted. This action cannot be undone.";
        String okButtonString = "Delete User";
        String backButtonString = "Cancel";

        return displayInfoMessageAfterContentEditingOrDeletion(title, message, okButtonString, backButtonString);
    }

    public boolean showBanUserInfoMessage() {
        String title = "ATTENTION";
        String message = "Ban this user?";
        String okButtonString = "Ban User";
        String backButtonString = "Cancel";

        return displayInfoMessageAfterContentEditingOrDeletion(title, message, okButtonString, backButtonString);
    }

    public boolean showUnBanUserInfoMessage() {
        String title = "ATTENTION";
        String message = "Unban this user?";
        String okButtonString = "Unban User";
        String backButtonString = "Cancel";

        return displayInfoMessageAfterContentEditingOrDeletion(title, message, okButtonString, backButtonString);
    }


    public boolean showDeleteCommentInfoMessage() {
        String title = "ATTENTION";
        String message = "Are you sure you want to delete this comment?";
        String okButtonString = "Delete Comment";
        String backButtonString = "Return to Comments";

        return displayInfoMessageAfterContentEditingOrDeletion(title, message, okButtonString, backButtonString);
    }

    public boolean showDeleteReviewInfoMessage() {
        String title = "ATTENTION";
        String message = "Are you sure you want to delete this review?";
        String okButtonString = "Delete Review";
        String backButtonString = "Return to Reviews";

        return displayInfoMessageAfterContentEditingOrDeletion(title, message, okButtonString, backButtonString);
    }

    public boolean showDeletePostInfoMessage() {
        String title = "ATTENTION";
        String message = "Are you sure you want to delete this post?";
        String okButtonString = "Delete Post";
        String backButtonString = "Return to Post";

        return displayInfoMessageAfterContentEditingOrDeletion(title, message, okButtonString, backButtonString);
    }

    public boolean showDeleteAccountInfoMessage() {
        String title = "ATTENTION";
        String message = "Are you sure you want to delete your account? This action cannot be undone.";
        String okButtonString = "Delete Account";
        String backButtonString = "Return to Account Info Page";

        return displayInfoMessageAfterContentEditingOrDeletion(title, message, okButtonString, backButtonString);
    }

    public boolean showDiscardPostInfoMessage() {
        String title = "ATTENTION";
        String message = "Discard the post? You will lose what you were writing.";
        String okButtonString = "Discard Post";
        String backButtonString = "Return to Post";

        return  displayInfoMessageAfterContentEditingOrDeletion(title, message, okButtonString, backButtonString);
    }

    private boolean displayInfoMessageAfterContentEditingOrDeletion(String title, String message, String okButtonString, String backButtonString) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(650);
        window.setMinHeight(200);
        window.setOnCloseRequest(e -> window.close());

        Label label = new Label();
        label.setText(message);
        label.setFont(Font.font("Georgia Pro Cond Black", FontWeight.BOLD, FontPosture.REGULAR, 16));
        label.setTextFill(Color.rgb(24,46,88));
        label.setAlignment(Pos.CENTER);

        AtomicBoolean userChoice = new AtomicBoolean(false);

        Button backButton = new Button(backButtonString);
        backButton.setOnAction(e -> {
            window.close();
            userChoice.set(false);
        });

        Button confirmActionButton = new Button(okButtonString);
        confirmActionButton.setOnAction(e -> {
            window.close();
            userChoice.set(true);
        });

        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(label, backButton, confirmActionButton);
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox);
        window.setScene(scene);
        window.showAndWait();

        return userChoice.get();
    }

    public void closeStageButton(Button button) {
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }
}
