package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.BoardgameDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.ReviewDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.services.BoardgameService;
import it.unipi.dii.lsmsdb.boardgamecafe.services.ReviewService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.function.Consumer;

@Component
public class ControllerViewDetailsBoardgamePage implements Initializable {

    public enum UserActivity {
        EDIT_INFO, NO_EDIT
    }
    private final String NO_RATING = "-----";

    //********** Edit Operation Components **********
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
    private Button saveChangesButton;
    @FXML
    private Button cancelButton;

    // *********** Text Fields ***********
    @FXML
    private TextField updateDescriptionTextField;
    @FXML
    private TextField updateBgNameTextField;
    @FXML
    private TextField updateYearOfPublicationTextField;
    @FXML
    private TextField updatePlayingTimeTextField;
    @FXML
    private TextField updateMinPlayersTextField;
    @FXML
    private TextField updateMaxPlayersTextField;
    @FXML
    private TextField updateMinAgeTextField;
    @FXML
    private TextField updateImageLinkTextField;
    @FXML
    private TextField updateCategoryTextField;
    @FXML
    private TextField updateDesignerTextField;
    @FXML
    private TextField updatePublisherTextField;

    // *********** List Views ***********
    @FXML
    private ListView<String> categoriesListView;
    @FXML
    private ListView<String> designersListView;
    @FXML
    private ListView<String> publishersListView;

    // *********** Utils ***********
    private final List<String> listStringsCategories = new ArrayList<>();
    private final List<String> listStringsDesigners = new ArrayList<>();
    private final List<String> listStringsPublishers = new ArrayList<>();

    //********** Buttons **********
    @FXML
    private Button editBoardgameButton;
    @FXML
    private Button addReviewButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button previousButton;
    @FXML
    private Button closeButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button deleteButton;

    //********** Labels **********
    @FXML
    protected Label averageRatingLabel;
    @FXML
    protected Label boardgameNameLabel;
    @FXML
    private Label counterReviewsLabel;
    @FXML
    protected TextArea descriptionTextArea;
    @FXML
    protected Label minPlayerLabel;
    @FXML
    protected Label maxPlayerLabel;
    @FXML
    protected Label playingTimeLabel;
    @FXML
    protected Label yearPublishedLabel;
    @FXML
    protected Label minAgeLabel;
    @FXML
    protected Label firstCategoryLabel;
    @FXML
    protected Label firstDesignerLabel;
    @FXML
    protected Label firstPublisherLabel;
    @FXML
    protected Label minutesLabel;
    @FXML
    protected Label plusLabel;
    //********** Combo Boxes **********
    @FXML
    private ComboBox<String> comboBoxFullCategories;
    @FXML
    private ComboBox<String> comboBoxFullDesigners;
    @FXML
    private ComboBox<String> comboBoxFullPublishers;

    //********** Useful Variables **********
    @FXML
    private ImageView imageBoardgame;

    //********** Useful Variables **********
    @FXML
    private GridPane reviewsGridPane;
    @FXML
    private ScrollPane scrollSet;
    @Autowired
    private ReviewDBMongo reviewMongoOp;
    @Autowired
    private BoardgameDBMongo boardgameDBMongo;
    @Autowired
    private BoardgameService serviceBoardgame;
    @Autowired
    private ControllerObjectReview controllerObjectReview;
    @Autowired
    private ModelBean modelBean;
    @Autowired
    private ReviewService serviceReview;
    @FXML
    protected Tooltip tooltipLblRating;

    private List<ReviewModelMongo> reviews = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private List<String> designers = new ArrayList<>();
    private List<String> publishers = new ArrayList<>();

    private BoardgameModelMongo boardgame;

    private static UserModelMongo currentUser;

    //Utils Variables
    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;
    private final static int SKIP = 10; //how many posts to skip per time
    private final static int LIMIT = 10; //how many posts to show for each page

    private final String promptTextFullCategories = "See Full Categories";
    private final String promptTextFullDesigners = "See Full Designers";
    private final String promptTextFullPublishers = "See Full Publishers";

    private UserActivity selectedOperation;


    @Autowired
    @Lazy
    private StageManager stageManager;

    public ControllerViewDetailsBoardgamePage() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[INFO] Loaded ControllerViewDetailsBoardgamePage");
        currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);

        this.previousButton.setDisable(true);
        this.nextButton.setDisable(true);
        resetPage();

        boardgame = (BoardgameModelMongo) modelBean.getBean(Constants.SELECTED_BOARDGAME);

        // Setting up buttons depending on if the current user is who created the post that's being visualized
        if (!currentUser.get_class().equals("user")) {
            editBoardgameButton.setVisible(false);       // Making the edit button invisible
            deleteButton.setVisible(false);     // Making the delete button invisible
        }
        prepareScene();
        reviews.addAll(getData(this.boardgame.getBoardgameName()));
        fillGridPane();
    }

    private void prepareScene() {

        this.selectedOperation = UserActivity.NO_EDIT;
        Double ratingFromTop = ControllerViewRegUserBoardgamesPage.getBgameRating(boardgame);
        if (ratingFromTop == null)
            ratingFromTop = reviewMongoOp.getAvgRatingByBoardgameName(boardgame.getBoardgameName());
        String ratingAsString = (ratingFromTop != null) ? String.format("%.1f", ratingFromTop) : NO_RATING;

        if (ratingAsString.equals(NO_RATING))
            this.tooltipLblRating.setShowDelay(Duration.ZERO);
        else
            this.averageRatingLabel.setTooltip(null);

        this.averageRatingLabel.setText(ratingAsString);
        this.counterReviewsLabel.setText(String.valueOf(boardgame.getReviews().size()));
        this.setImage();
        this.boardgameNameLabel.setText(this.boardgame.getBoardgameName());
        this.descriptionTextArea.setText(this.boardgame.getDescription()
                .replaceAll("&#[0-9]+;", "").replaceAll("&[a-zA-Z0-9]+;", ""));
        this.minPlayerLabel.setText(String.valueOf(this.boardgame.getMinPlayers()));
        this.maxPlayerLabel.setText(String.valueOf(this.boardgame.getMaxPlayers()));
        this.playingTimeLabel.setText(String.valueOf(this.boardgame.getPlayingTime()));
        this.yearPublishedLabel.setText(String.valueOf(this.boardgame.getYearPublished()));
        this.minAgeLabel.setText(String.valueOf(this.boardgame.getMinAge()));
        //*********** Categories, Designers and Publishers management ***********
        this.categories.addAll(boardgame.getBoardgameCategory());
        this.designers.addAll(boardgame.getBoardgameDesigner());
        this.publishers.addAll(boardgame.getBoardgamePublisher());

        if (!categories.isEmpty()){
            this.firstCategoryLabel.setText(categories.get(0));
            this.categoriesListView.getItems().addAll(categories);
            this.listStringsCategories.addAll(categories);
        } else {
            this.firstCategoryLabel.setText("");
        }
        if (!designers.isEmpty()) {
            this.firstDesignerLabel.setText(designers.get(0));
            this.designersListView.getItems().addAll(designers);
            this.listStringsDesigners.addAll(designers);
        } else {
            this.firstDesignerLabel.setText("");
        }
        if (!publishers.isEmpty()) {
            this.firstPublisherLabel.setText(publishers.get(0));
            this.publishersListView.getItems().addAll(publishers);
            this.listStringsPublishers.addAll(publishers);
        } else {
            this.firstPublisherLabel.setText("");
        }
        initComboBox(categories, designers, publishers);
        setEditFieldsVisibility(false);
    }

    private void setImage() {
        Image imageInCache = ControllerObjectBoardgame.getImageFromCache(this.boardgame.getImage());
        if (imageInCache != null)
        {
            this.imageBoardgame.setImage(imageInCache);
            System.out.println("Trovata in cache");
            return;
        }
        try {
            URI uri = new URI(this.boardgame.getImage()); // Crea URI
            URL url = uri.toURL(); // Converti a URL
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "JavaFX Application");

            try (InputStream inputStream = connection.getInputStream()) {
                byte[] imageBytes = readFullInputStream(inputStream);
                Image downloadedImage = new Image(new ByteArrayInputStream(imageBytes));
                this.imageBoardgame.setImage(downloadedImage);
            }
            catch (Exception e)
            {
                String imagePath = getClass().getResource("/images/noImage.jpg").toExternalForm();
                Image image = new Image(imagePath);
                this.imageBoardgame.setImage(image);
            }
        } catch (Exception e) {
            System.out.println("ControllerViewBoardgameDetails: download boardgame image failed");
        }
    }

    private byte[] readFullInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public void onClickDeleteButton() {
        boolean userChoice = stageManager.showDeleteBoardgameInfoMessage();
        if (!userChoice) {
            return;
        }
        try {
            if (serviceBoardgame.deleteBoardgame(this.boardgame)){
                modelBean.putBean(Constants.DELETED_BOARDGAME, this.boardgame.getBoardgameName());
                stageManager.closeStage();
                stageManager.showInfoMessage("Delete Operation",
                        "The Boardgame Was Successfully Deleted From BoardGame-Cafè App.");
            } else {
                modelBean.putBean(Constants.SELECTED_BOARDGAME, null);
                stageManager.closeStage();
                stageManager.showInfoMessage("Delete Operation",
                        "An Unexpected Error Occurred While Deleting The Boardgame From BoardGame-Café_App." +
                        "\n\n\t\t\tPlease contact the administrator.");
            }
        } catch (Exception ex) {
            stageManager.showInfoMessage("Exception Info", "Something went wrong. Try again in a while.");
            System.err.println("[ERROR] onClickDeleteButton@ControllerViewDetailsBoardgamePage.java raised an exception: " + ex.getMessage());
        }
    }

    public void onClickRefreshButton(ActionEvent event) {
        cleanFetchAndFill();
    }

    public void onClickCloseButton(ActionEvent event) {
        stageManager.closeStageButton(this.closeButton);
    }

    @FXML
    void onClickNext() {
        //clear variables
        reviewsGridPane.getChildren().clear();
        reviews.clear();
        //update the skipcounter
        skipCounter += SKIP;
        //retrieve boardgames
        reviews.addAll(getData(this.boardgame.getBoardgameName()));
        //put all boardgames in the Pane
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    @FXML
    void onClickPrevious() {
        //clear variables
        reviewsGridPane.getChildren().clear();
        reviews.clear();
        //update the skipcounter
        skipCounter -= SKIP;
        //retrieve boardgames
        reviews.addAll(getData(this.boardgame.getBoardgameName()));
        //put all boardgames in the Pane
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    void resetPage() {
        //clear variables
        this.tooltipLblRating.hide();
        reviewsGridPane.getChildren().clear();
        reviews.clear();
        categories.clear();
        designers.clear();
        publishers.clear();
        skipCounter = 0;
        previousButton.setDisable(true);
        nextButton.setDisable(true);
        scrollSet.setVvalue(0);
    }

    void prevNextButtonsCheck(List<ReviewModelMongo> reviews) {
        if ((reviews.size() > 0)) {
            if ((reviews.size() < LIMIT)) {
                if (skipCounter <= 0) {
                    previousButton.setDisable(true);
                    nextButton.setDisable(true);
                } else {
                    previousButton.setDisable(false);
                    nextButton.setDisable(true);
                }
            } else {
                if (skipCounter <= 0) {
                    previousButton.setDisable(true);
                    nextButton.setDisable(false);
                } else {
                    previousButton.setDisable(false);
                    nextButton.setDisable(false);
                }
            }
        } else {
            if (skipCounter <= 0) {
                previousButton.setDisable(true);
                nextButton.setDisable(true);
            } else {
                previousButton.setDisable(false);
                nextButton.setDisable(true);
            }
        }
    }

    private List<ReviewModelMongo> getData(String boardgameName) {

        List<ReviewModelMongo> reviews = reviewMongoOp.
                findRecentReviewsByBoardgame(boardgameName, LIMIT, skipCounter);
        prevNextButtonsCheck(reviews);

        return reviews;
    }

    void setGridPaneColumnAndRow() {
        columnGridPane = 0;
        rowGridPane = 1;
    }

    public void onClickAddReviewButton() {
    }

    private void loadViewMessageInfo() {
        Parent loadViewItem = stageManager.loadViewNode(FxmlView.INFOMSGREVIEWS.getFxmlFile());
        AnchorPane noContentsYet = new AnchorPane();
        noContentsYet.getChildren().add(loadViewItem);

        if (!reviews.isEmpty()) {
            resetPage();
            reviewsGridPane.add(noContentsYet, 0, rowGridPane);
        } else {
            resetPage();
            reviewsGridPane.add(noContentsYet, 0, 0);
        }
        GridPane.setMargin(noContentsYet, new Insets(330, 100, 100, 287));
    }

    @FXML
    void fillGridPane() {

        //per mettere un solo elemento correttamente nel gridpane
        if (reviews.size() == 1) {
            columnGridPane = 0;
            rowGridPane = 0;
        } else {
            setGridPaneColumnAndRow();
        }

        try {
            if (reviews.isEmpty()) {
                loadViewMessageInfo();
            } else {
                for (ReviewModelMongo review : reviews) {
                    Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTREVIEW.getFxmlFile());
                    AnchorPane anchorPane = new AnchorPane();
                    anchorPane.getChildren().add(loadViewItem);

                    // Setting comment data - including callbacks for actions to be taken upon comment modification or deletion
                    controllerObjectReview.setData(review);

                    //choice number of column
                    if (columnGridPane == 1) {
                        columnGridPane = 0;
                        rowGridPane++;
                    }
                    reviewsGridPane.add(anchorPane, columnGridPane++, rowGridPane); //(child,column,row)
                    //DISPLAY SETTINGS
                    //set grid width
                    reviewsGridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
                    reviewsGridPane.setPrefWidth(500);
                    reviewsGridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);
                    //set grid height
                    reviewsGridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
                    reviewsGridPane.setPrefHeight(400);
                    reviewsGridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
                    //GridPane.setMargin(anchorPane, new Insets(25));
                    GridPane.setMargin(anchorPane, new Insets(4, 5, 10, 90));
                }
            }
        } catch (Exception e) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Try again in a while.");
            System.err.println("[ERROR] fillGridPane@ControllerViewDetailsBoardgamePage.java raised an exception: " + e.getMessage());
        }
    }

    private void cleanFetchAndFill() {
        resetPage();
        reviews.addAll(getData(this.boardgame.getBoardgameName()));
        fillGridPane();
    }

    private void initComboBox(List<String> categories,
                              List<String> designers,
                              List<String> publishers) {

        this.comboBoxFullCategories.getItems().clear();
        this.comboBoxFullCategories.getItems().addAll(categories);
        this.comboBoxFullDesigners.getItems().clear();
        this.comboBoxFullDesigners.getItems().addAll(designers);
        this.comboBoxFullPublishers.getItems().clear();
        this.comboBoxFullPublishers.getItems().addAll(publishers);

        comboBoxFullCategories.setPromptText(promptTextFullCategories);
        comboBoxFullCategories.setEditable(false);
        comboBoxFullDesigners.setPromptText(promptTextFullDesigners);
        comboBoxFullDesigners.setEditable(false);
        comboBoxFullPublishers.setPromptText(promptTextFullPublishers);
        comboBoxFullPublishers.setEditable(false);

        comboBoxFullCategories.getSelectionModel().selectedItemProperty().
                addListener((observable, oldValue, newValue) -> {
                    comboBoxFullCategories.setButtonCell(new ListCell<>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(promptTextFullCategories);
                        }
                    });
                });

        comboBoxFullDesigners.getSelectionModel().selectedItemProperty().
                addListener((observable, oldValue, newValue) -> {
                    comboBoxFullDesigners.setButtonCell(new ListCell<>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(promptTextFullDesigners);
                        }
                    });
                });

        comboBoxFullPublishers.getSelectionModel().selectedItemProperty().
                addListener((observable, oldValue, newValue) -> {
                    comboBoxFullPublishers.setButtonCell(new ListCell<>() {
                        @Override
                        protected void updateItem(String item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(promptTextFullPublishers);
                        }
                    });
                });
    }

    public void onClickSaveChangesButton(){

        //ToDo: Implementazione salvataggio dati e aggiornamento grafica
    }

    public void onClickEditBoardgameButton() {
        this.selectedOperation = UserActivity.EDIT_INFO;
        this.cancelButton.setVisible(true);
        this.saveChangesButton.setVisible(true);
        scrollSet.setVvalue(0);
        setEditFieldsVisibility(true);
    }

    public void onClickCancelButton(){
        clearFields();
        prepareScene();
        prevNextButtonsCheck(reviews);
    }
    public void onClickAddCategoryButton() {
        String category = updateCategoryTextField.getText().trim();
        if (!category.isEmpty()) {
            listStringsCategories.add(0, category);
            categoriesListView.getItems().add(0, category);
            updateCategoryTextField.clear();
        } else {
            stageManager.showInfoMessage("INFO", "Category field cannot be empty.");
        }
    }

    public void onClickRemoveCategoryButton() {
        String selectedCategory = categoriesListView.getSelectionModel().getSelectedItem();
        if (selectedCategory != null) {
            listStringsCategories.remove(selectedCategory);
            categoriesListView.getItems().remove(selectedCategory);
        } else {
            stageManager.showInfoMessage("INFO", "Please select a category to remove.");
        }
    }

    public void onClickAddDesignerButton() {
        String designer = updateDesignerTextField.getText().trim();
        if (!designer.isEmpty()) {
            listStringsDesigners.add(0, designer);
            designersListView.getItems().add(0, designer);
            updateDesignerTextField.clear();
        } else {
            stageManager.showInfoMessage("INFO", "Designer field cannot be empty.");
        }
    }

    public void onClickRemoveDesignerButton() {
        String selectedDesigner = designersListView.getSelectionModel().getSelectedItem();
        if (selectedDesigner != null) {
            listStringsDesigners.remove(selectedDesigner);
            designersListView.getItems().remove(selectedDesigner);
        } else {
            stageManager.showInfoMessage("INFO", "Please select a designer to remove.");
        }
    }

    public void onClickAddPublisherButton() {
        String publisher = updatePublisherTextField.getText().trim();
        if (!publisher.isEmpty()) {
            listStringsPublishers.add(0, publisher);
            publishersListView.getItems().add(0, publisher);
            updatePublisherTextField.clear();
        } else {
            stageManager.showInfoMessage("INFO", "Publisher field cannot be empty.");
        }
    }

    public void onClickRemovePublisherButton() {
        String selectedPublisher = publishersListView.getSelectionModel().getSelectedItem();
        if (selectedPublisher != null) {
            listStringsPublishers.remove(selectedPublisher);
            publishersListView.getItems().remove(selectedPublisher);
        } else {
            stageManager.showInfoMessage("INFO", "Please select a publisher to remove.");
        }
    }

    private void setEditFieldsVisibility(boolean isVisible) {

        if(isVisible){
            Image tempImage = new Image("/images/noImage.jpg");
            this.imageBoardgame.setImage(tempImage);
        }
        //********** Actual Labels **********
        this.averageRatingLabel.setDisable(isVisible);
        this.boardgameNameLabel.setDisable(isVisible);
        this.descriptionTextArea.setDisable(isVisible);
        this.minPlayerLabel.setDisable(isVisible);
        this.maxPlayerLabel.setDisable(isVisible);
        this.playingTimeLabel.setDisable(isVisible);
        this.minutesLabel.setDisable(isVisible);
        this.yearPublishedLabel.setDisable(isVisible);
        this.minAgeLabel.setDisable(isVisible);
        this.plusLabel.setDisable(isVisible);
        this.firstCategoryLabel.setDisable(isVisible);
        this.firstDesignerLabel.setDisable(isVisible);
        this.firstPublisherLabel.setDisable(isVisible);
        this.comboBoxFullCategories.setDisable(isVisible);
        this.comboBoxFullDesigners.setDisable(isVisible);
        this.comboBoxFullPublishers.setDisable(isVisible);
        //********** Others Actual Components **********
        this.scrollSet.setDisable(isVisible);
        this.nextButton.setDisable(isVisible);
        this.previousButton.setDisable(isVisible);
        this.refreshButton.setDisable(isVisible);
        this.addReviewButton.setDisable(isVisible);
        this.deleteButton.setDisable(isVisible);
        this.closeButton.setDisable(isVisible);
        this.editBoardgameButton.setDisable(isVisible);
        //********** Update Related TextFields **********
        this.updateDescriptionTextField.setVisible(isVisible);
        this.updateBgNameTextField.setVisible(isVisible);
        this.updateYearOfPublicationTextField.setVisible(isVisible);
        this.updatePlayingTimeTextField.setVisible(isVisible);
        this.updateMinPlayersTextField.setVisible(isVisible);
        this.updateMaxPlayersTextField.setVisible(isVisible);
        this.updateMinAgeTextField.setVisible(isVisible);
        this.updateImageLinkTextField.setVisible(isVisible);
        this.updateCategoryTextField.setVisible(isVisible);
        this.updateDesignerTextField.setVisible(isVisible);
        this.updatePublisherTextField.setVisible(isVisible);
        this.categoriesListView.setVisible(isVisible);
        this.designersListView.setVisible(isVisible);
        this.publishersListView.setVisible(isVisible);
        //********** Update Related Buttons **********
        this.cancelButton.setVisible(isVisible);
        this.saveChangesButton.setVisible(isVisible);
        this.addCategoryButton.setVisible(isVisible);
        this.removeCategoryButton.setVisible(isVisible);
        this.addDesignerButton.setVisible(isVisible);
        this.removeDesignerButton.setVisible(isVisible);
        this.addPublisherButton.setVisible(isVisible);
        this.removePublisherButton.setVisible(isVisible);
    }

    public void clearFields(){
        //********** TextFields & SubLabels **********
        this.updateDescriptionTextField.clear();
        this.updateDescriptionTextField.setPromptText("Write the Boardgame description here...");
        this.updateBgNameTextField.clear();
        this.updateBgNameTextField.setPromptText("Write the Boardgame name here...");
        this.updateYearOfPublicationTextField.clear();
        this.updateYearOfPublicationTextField.setPromptText("YYYY");
        this.updatePlayingTimeTextField.clear();
        this.updatePlayingTimeTextField.setPromptText("Minutes");
        this.updateMinPlayersTextField.clear();
        this.updateMinPlayersTextField.setPromptText("Min #");
        this.updateMaxPlayersTextField.clear();
        this.updateMaxPlayersTextField.setPromptText("Max #");
        this.updateMinAgeTextField.clear();
        this.updateMinAgeTextField.setPromptText("Age");
        this.updateImageLinkTextField.clear();
        this.updateImageLinkTextField.setPromptText("Write the Image link address here...");
        this.updateCategoryTextField.clear();
        this.updateCategoryTextField.setPromptText("Write the Category here...");
        this.updateDesignerTextField.clear();
        this.updateDesignerTextField.setPromptText("Write the Designer here...");
        this.updatePublisherTextField.clear();
        this.updatePublisherTextField.setPromptText("Write the Publisher here...");
        this.categoriesListView.getItems().clear();
        this.designersListView.getItems().clear();
        this.publishersListView.getItems().clear();
    }

}