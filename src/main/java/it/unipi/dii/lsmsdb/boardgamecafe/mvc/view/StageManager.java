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

//--- La classe StageManager è responsabile della gestione delle finestre e delle scene dell'applicazione.
// Fornisce metodi per caricare le viste, mostrare le finestre e visualizzare eventuali messaggi di errore.
// Utilizza la classe SpringFXMLLoader per caricare i file FXML e creare scene per le viste dell'applicazione ---
public class StageManager {

    private final Stage primaryStage;
    private final SpringFXMLLoader springFXMLLoader;
    private final static Logger logger = LoggerFactory.getLogger(StageManager.class);

    public StageManager(SpringFXMLLoader springFXMLLoader, Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.springFXMLLoader = springFXMLLoader;
    }

    public void switchScene(final FxmlView view) {
//        Parent viewRoot = loadViewNode(view.getFxmlFile());
//        show(viewRoot, view.getTitle());


        Parent viewRoot = loadViewNode(view.getFxmlFile());

        // Ottieni lo stage corrente dall’elemento attuale
        Stage currentStage = (Stage) Stage.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);

        if (currentStage != null) {
            currentStage.close(); // Chiude l'attuale
        }

        // Crea un nuovo stage per la nuova scena
        Stage newStage = new Stage();
        Scene newScene = new Scene(viewRoot);
        newStage.setScene(newScene);
        newStage.setTitle(view.getTitle());
        newStage.show();
    }

    public Parent loadViewNode(String fxmlFilePath) {

        Parent rootNode = null;

        try {
            rootNode = springFXMLLoader.load(fxmlFilePath);
            Objects.requireNonNull(rootNode, "A Root FXML node must not be null");
        } catch (Exception e){
            logger.error("Exception occurred: " + e.getLocalizedMessage());
        }
        return rootNode;
    }

    private void show(final Parent rootNode, String title) {

        Scene scene = prepareScene(rootNode);

        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        try {
            primaryStage.show();
        } catch (Exception e) {
            logger.error("Exception occurred: " + e.getLocalizedMessage());
        }
    }

    private Scene prepareScene(Parent rootNode) {

        Scene scene = primaryStage.getScene();

        if (scene == null) {
            scene = new Scene(rootNode);
        }
        scene.setRoot(rootNode);
        return scene;
    }

    public void showWindow(final FxmlView window) {

        try {
            Parent viewRoot = loadViewNode(window.getFxmlFile());
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(window.getTitle());
            stage.setScene(new Scene(viewRoot));
            stage.show();

        } catch (Exception e) {
            logger.error("Exception occurred: " + e.getLocalizedMessage());
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
        // Imposta il font e il colore del testo
        label.setFont(Font.font("Georgia Pro Cond Black", FontWeight.BOLD, FontPosture.REGULAR, 16));
        label.setTextFill(Color.rgb(24,46,88));
        label.setAlignment(Pos.CENTER); // Centra il testo del messaggio

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> window.close());

        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(label, closeButton);
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox);
        window.setScene(scene);
        window.show();
    }

    public boolean showDiscardPostInfoMessage() {
        String title = "ATTENTION";
        String message = "Discard the post? You will lose what you were writing.";

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

        AtomicBoolean discard = new AtomicBoolean(false);

        Button backButton = new Button("Return to post");
        backButton.setOnAction(e -> {
            window.close();
            discard.set(false);
        });

        Button discardPostButton = new Button("Discard post");
        discardPostButton.setOnAction(e -> {
            window.close();
            discard.set(true);
        });

        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(label, backButton, discardPostButton);
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox);
        window.setScene(scene);
        window.showAndWait();

        return discard.get();
    }

    public void closeStageButton(Button button) {
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }

    public void closeStageMouseEvent(MouseEvent click) {

        Stage stage = (Stage) ((Node) click.getSource()).getScene().getWindow();
        stage.close();
    }

    public void setNullList(List<ImageView> imageViews, List<Label> labels) {
        for (int i = 0;i <labels.size();i++) {
            imageViews.get(i).setImage(null);
            labels.get(i).setText("");
        }
    }

    public int getElemIndexGridPane(MouseEvent event) {
        String id = event.getPickResult().getIntersectedNode().getId();
        String[] value = id.split("image");
        return Integer.parseInt(value[1]);
    }

}