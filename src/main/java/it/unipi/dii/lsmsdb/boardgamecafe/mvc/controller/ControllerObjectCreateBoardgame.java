package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ControllerObjectCreateBoardgame {

    // *********** Buttons ***********
    @FXML
    private Button addCategoryButton;
    @FXML
    private Button removeCategoryButton;
    @FXML
    private Button addDesignerButton;
    @FXML
    private Button removeDesignerButton;
    @FXML
    private Button addPublisherButton;
    @FXML
    private Button removePublisherButton;
    @FXML
    private Button uploadButton;
    @FXML
    private Button cancelButton;

    // *********** Text Fields ***********
    @FXML
    private TextField descriptionTextField;
    @FXML
    private TextField boardgameNameTextField;
    @FXML
    private TextField yearOfPublicationTextField;
    @FXML
    private TextField playingTimeTextField;
    @FXML
    private TextField minPlayersTextField;
    @FXML
    private TextField maxPlayersTextField;
    @FXML
    private TextField minAgeTextField;
    @FXML
    private TextField imageLinkTextField;
    @FXML
    private TextField categoryTextField;
    @FXML
    private TextField designerTextField;
    @FXML
    private TextField publisherTextField;

    // *********** List Views ***********
    @FXML
    private ListView<String> categoriesListView;
    @FXML
    private ListView<String> designersListView;
    @FXML
    private ListView<String> publishersListView;

    // *********** Utils ***********
    private final List<String> listViewCategories = new ArrayList<>();
    private final List<String> listViewDesigners = new ArrayList<>();
    private final List<String> listViewPublishers = new ArrayList<>();

    private StageManager stageManager;

    @Autowired
    @Lazy
    public ControllerObjectCreateBoardgame(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    public ControllerObjectCreateBoardgame() {}


    @FXML
    private void initialize() {

        // Filtro numerico per i campi che devono contenere solo numeri
        addNumericValidation(yearOfPublicationTextField);
        addNumericValidation(playingTimeTextField);
        addNumericValidation(minPlayersTextField);
        addNumericValidation(maxPlayersTextField);
        addNumericValidation(minAgeTextField);

        // Valida e formatta i campi di input per categorie, designer e publisher
        setupTextFieldValidation(categoryTextField);
        setupTextFieldValidation(designerTextField);
        setupTextFieldValidation(publisherTextField);

        // Collega i pulsanti ai metodi
        addCategoryButton.setOnAction(event -> onClickAddCategoryButton());
        removeCategoryButton.setOnAction(event -> onClickRemoveCategoryButton());
        addDesignerButton.setOnAction(event -> onClickAddDesignerButton());
        removeDesignerButton.setOnAction(event -> onClickRemoveDesignerButton());
        addPublisherButton.setOnAction(event -> onClickAddPublisherButton());
        removePublisherButton.setOnAction(event -> onClickRemovePublisherButton());
    }

    public void onClickAddCategoryButton() {
        String category = categoryTextField.getText().trim();
        if (!category.isEmpty()) {
            listViewCategories.add(category);
            categoriesListView.getItems().add(category);
            categoryTextField.clear();
        } else {
            stageManager.showInfoMessage("INFO", "Category field cannot be empty.");
        }
    }

    public void onClickRemoveCategoryButton() {
        String selectedCategory = categoriesListView.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            listViewCategories.remove(selectedCategory);
            categoriesListView.getItems().remove(selectedCategory);
        } else {
            stageManager.showInfoMessage("INFO", "Please select a category to remove.");
        }
    }

    public void onClickAddDesignerButton() {
        String designer = designerTextField.getText().trim();
        if (!designer.isEmpty()) {
            listViewDesigners.add(designer);
            designersListView.getItems().add(designer);
            designerTextField.clear();
        } else {
            stageManager.showInfoMessage("INFO", "Designer field cannot be empty.");
        }
    }

    public void onClickRemoveDesignerButton() {
        String selectedDesigner = designersListView.getSelectionModel().getSelectedItem();
        if (selectedDesigner != null) {
            listViewDesigners.remove(selectedDesigner);
            designersListView.getItems().remove(selectedDesigner);
        } else {
            stageManager.showInfoMessage("INFO", "Please select a designer to remove.");
        }
    }

    public void onClickAddPublisherButton() {
        String publisher = publisherTextField.getText().trim();
        if (!publisher.isEmpty()) {
            listViewPublishers.add(publisher);
            publishersListView.getItems().add(publisher);
            publisherTextField.clear();
        } else {
            stageManager.showInfoMessage("INFO", "Publisher field cannot be empty.");
        }
    }

    public void onClickRemovePublisherButton() {
        String selectedPublisher = publishersListView.getSelectionModel().getSelectedItem();
        if (selectedPublisher != null) {
            listViewPublishers.remove(selectedPublisher);
            publishersListView.getItems().remove(selectedPublisher);
        } else {
            stageManager.showInfoMessage("INFO", "Please select a publisher to remove.");
        }
    }

    private void addNumericValidation(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) { // Permette solo numeri
                textField.setText(oldValue); // Ripristina il valore precedente
            }
        });
    }

    private void setupTextFieldValidation(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^[a-zA-Z\\s\\-\\'&/_]*$") || newValue.contains(",")) {
                // Permette lettere, spazi, trattini (-), apostrofi ('), punti (.), punti esclamativi (!) e interrogativi (?), blocca la virgola
                textField.setText(oldValue); // Ripristina il valore precedente
                showErrorMessage("Invalid input!",
                        "Only letters, spaces, and common special characters (-'&/_) are allowed. " +
                                "Commas are not allowed.");
            }
        });
    }


    private void showErrorMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void onClickCancelButton(){};
    public void onClickUploadButton(){};

    // Metodo per ottenere le liste
    public List<String> getCategories() {
        return listViewCategories;
    }

    public List<String> getDesigners() {
        return listViewDesigners;
    }

    public List<String> getPublishers() {
        return listViewPublishers;
    }
}
