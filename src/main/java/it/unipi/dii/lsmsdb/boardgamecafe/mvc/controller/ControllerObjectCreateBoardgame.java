package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import javafx.fxml.FXML;
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

    // *********** Buttons ***********
    @FXML
    private Button addCategoryButton;
    @FXML
    private Button addDesignerButton;
    @FXML
    private Button addPublisherButton;
    @FXML
    private Button uploadButton;
    @FXML
    private Button cancelButton;

    // *********** Text Fields ***********
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
    private TextField thumbnailLinkTextField;
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
        // Collega i pulsanti ai metodi
        addCategoryButton.setOnAction(event -> onClickAddCategoryButton());
        addDesignerButton.setOnAction(event -> onClickAddDesignerButton());
        addPublisherButton.setOnAction(event -> onClickAddPublisherButton());
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
