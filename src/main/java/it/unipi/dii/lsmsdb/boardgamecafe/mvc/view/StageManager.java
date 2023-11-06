
//package it.unipi.dii.lsmsdb.boardgamecafe.mvc.view;
/*
import it.unipi.dii.lsmsdb.boardgamecafe.utils.config.SpringFXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

public class StageManager {

    private final Stage primaryStage;
    private final SpringFXMLLoader springFXMLLoader;
    private final static Logger logger = LoggerFactory.getLogger(StageManager.class);

    public StageManager(SpringFXMLLoader springFXMLLoader, Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.springFXMLLoader = springFXMLLoader;
    }

    public void switchScene(final FxmlView view) {
        Parent viewRoot = loadViewNode(view.getFxmlFile());
        show(viewRoot, view.getTitle());
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

    private Parent loadViewNode(String fxmlFilePath) {
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
//        primaryStage.setResizable(false);
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

    public void createLabelList(List<Label> labels, Label label1,  Label label2,  Label label3,  Label label4, Label label5,
                                Label label6, Label label7, Label label8, Label label9, Label label10, Label label11,
                                Label label12, Label label13, Label label14, Label label15, Label label16, Label label17,
                                Label label18) {
        labels.clear();
        labels.add(label1);
        labels.add(label2);
        labels.add(label3);
        labels.add(label4);
        labels.add(label5);
        labels.add(label6);
        labels.add(label7);
        labels.add(label8);
        labels.add(label9);
        labels.add(label10);
        labels.add(label11);
        labels.add(label12);
        labels.add(label13);
        labels.add(label14);
        labels.add(label15);
        labels.add(label16);
        labels.add(label17);
        labels.add(label18);
    }

    public void showInfoMessage(String title, String message) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(title);
        window.setMinWidth(300);
        window.setMinHeight(150);
        window.setOnCloseRequest(e -> window.close());
        Label label = new Label();
        label.setText(message);
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> window.close());
        VBox vBox = new VBox(10);
        vBox.getChildren().addAll(label, closeButton);
        vBox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(vBox);
        window.setScene(scene);
        window.show();
    }

    public void createImageViewList(List<ImageView> imageViews, ImageView image1, ImageView image2, ImageView image3,
                                    ImageView image4, ImageView image5, ImageView image6, ImageView image7,
                                    ImageView image8, ImageView image9, ImageView image10, ImageView image11,
                                    ImageView image12, ImageView image13, ImageView image14, ImageView image15,
                                    ImageView image16, ImageView image17, ImageView image18) {
        imageViews.clear();
        imageViews.add(image1);
        imageViews.add(image2);
        imageViews.add(image3);
        imageViews.add(image4);
        imageViews.add(image5);
        imageViews.add(image6);
        imageViews.add(image7);
        imageViews.add(image8);
        imageViews.add(image9);
        imageViews.add(image10);
        imageViews.add(image11);
        imageViews.add(image12);
        imageViews.add(image13);
        imageViews.add(image14);
        imageViews.add(image15);
        imageViews.add(image16);
        imageViews.add(image17);
        imageViews.add(image18);
    }

    public void clearList(List<ImageView> imageViews, List<Label> labels) {
        imageViews.clear();
        labels.clear();
    }

    public void setNullList(List<ImageView> imageViews, List<Label> labels) {
        for (int i = 0;i <labels.size();i++) {
            imageViews.get(i).setImage(null);
            labels.get(i).setText("");
        }
    }

    public int getElemIndex(MouseEvent event) {
        String id = event.getPickResult().getIntersectedNode().getId();
        String[] value = id.split("image");
        return Integer.parseInt(value[1]);
    }

    public void closeStage(Button button) {
        Stage stage = (Stage) button.getScene().getWindow();
        stage.close();
    }

    public String getErrors(String sbError) {
        List<String> errors = List.of(sbError.split(" "));
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0;i < errors.size();i++) {
            if (i == errors.size()-1) {
                stringBuilder.append(errors.get(i));
                break;
            }
            stringBuilder.append(errors.get(i)).append(", ");
        }
        return stringBuilder.toString();
    }

    public String generateStringBuilderErrorUser(String firstName, String lastName, String gender, String country,
                                                 String city, String streetName, int streetNumber, int month,
                                                 int day, String email, String username, String password,
                                                 String repeatedPassword, boolean update) {
        StringBuilder sbError = new StringBuilder();
        if (firstName.isEmpty()) sbError.append("First_Name ");
        if (lastName.isEmpty()) sbError.append("Last_Name ");
        if (gender == null) sbError.append("Gender ");
        if (country.isEmpty()) sbError.append("Country ");
        if (city.isEmpty()) sbError.append("City ");
        if (streetName.isEmpty()) sbError.append("Street_Name ");
        if (streetNumber == 0) sbError.append("Street_Number ");
        if (month == 0) sbError.append("Month ");
        if (day == 0) sbError.append("Day ");
        if (email.isEmpty()) sbError.append("E-mail ");
        if (!update) {
            if (username.isEmpty()) sbError.append("Username ");
        }
        if (password.isEmpty()) sbError.append("Password ");
        if (repeatedPassword.isEmpty()) sbError.append("Repeated_password ");
        return sbError.toString();
    }

    public String generateStringBuilderErrorPhone(String name, String brand, String picture, String body, String os,
                                                 String storage, String displaySize, String displayResolution,
                                                  String cameraPixels, String videoPixels, String ram, String chipset,
                                                  String batterySize, String batteryType, boolean isUpdate) {
        StringBuilder sbError = new StringBuilder();
        if (brand.isEmpty()) sbError.append("Brand ");
        if (picture.isEmpty()) sbError.append("Picture ");
        if (body.isEmpty()) sbError.append("Body ");
        if (os.isEmpty()) sbError.append("OS ");
        if (storage.isEmpty()) sbError.append("Storage ");
        if (displaySize.isEmpty()) sbError.append("Display_Size ");
        if (displayResolution.isEmpty()) sbError.append("Display_Resolution ");
        if (cameraPixels.isEmpty()) sbError.append("Camera_Pixels ");
        if (videoPixels.isEmpty()) sbError.append("Video_Pixels ");
        if (ram.isEmpty()) sbError.append("RAM ");
        if (chipset.isEmpty()) sbError.append("Chipset ");
        if (batterySize.isEmpty()) sbError.append("Battery_Size ");
        if (batteryType.isEmpty()) sbError.append("Battery_Type ");
        if (!isUpdate) {
            if (name.isEmpty()) sbError.append("Name ");
        }
        return sbError.toString();
    }


}
*/