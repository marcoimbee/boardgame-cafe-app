package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class ControllerObjectCreateBoardgame {

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
    @FXML
    private ListView<String> categoriesListView;
    @FXML
    private ListView<String> designersListView;
    @FXML
    private ListView<String> publishersListView;

    private StageManager stageManager;
    private final List<String> listViewCategories = new ArrayList<>();
    private final List<String> listViewDesigners = new ArrayList<>();
    private final List<String> listViewPublishers = new ArrayList<>();

    @Autowired
    @Lazy
    public ControllerObjectCreateBoardgame(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    public ControllerObjectCreateBoardgame() {}

    @FXML
    private void initialize() {
        // Numerical filter for fields that need to contain just numbers
        addNumericValidation(yearOfPublicationTextField);
        addNumericValidation(playingTimeTextField);
        addNumericValidation(minPlayersTextField);
        addNumericValidation(maxPlayersTextField);
        addNumericValidation(minAgeTextField);

        // Validating and formatting input fields for categories, designers and publishers
        setupTextFieldValidation(categoryTextField);
        setupTextFieldValidation(designerTextField);
        setupTextFieldValidation(publisherTextField);

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
            if (!newValue.matches("\\d*")) {    // Allowing only numbers
                textField.setText(oldValue);          // Resetting to the old value
            }
        });
    }

    private void setupTextFieldValidation(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^[a-zA-Z\\s\\-\\'&/_]*$") || newValue.contains(",")) {
                textField.setText(oldValue);        // resetting to the old value
                showErrorMessage(
                        "Invalid input!",
                        "Only letters, spaces, and common special characters (-'&/_) are allowed. " +
                                "Commas are not allowed."
                );
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

    public void onClickCancelButton(){}

    public void onClickUploadButton(){}

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
