package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
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
import javafx.stage.Stage;
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
    private ControllerObjectReviewBlankBody controllerObjectReviewBlankBody;
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

    private static GenericUserModelMongo currentUser;

    //private static int totalReviewsCounter;

    //Utils Variables
    private boolean shiftDownSingleObjectGridPane; // false: no - true: yes
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

    private Consumer<String> deletedReviewCallback;

    public ControllerViewDetailsBoardgamePage() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[INFO] Loaded ControllerViewDetailsBoardgamePage");
        currentUser = (GenericUserModelMongo) modelBean.getBean(Constants.CURRENT_USER);

        this.previousButton.setDisable(true);
        this.nextButton.setDisable(true);
        resetPage();

        if (!currentUser.get_class().equals("admin")) {
            currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
            editBoardgameButton.setVisible(false);       // Making the edit button invisible
            deleteButton.setVisible(false);     // Making the delete button invisible
        } else {
            currentUser = (AdminModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        }
        prepareScene();

        System.out.println("[INFO] Found " + boardgame.getReviewCount() + " reviews for '" + boardgame.getBoardgameName() + "'");
        reviews.addAll(getData(this.boardgame.getBoardgameName()));

        fillGridPane();

        // Page focus listener - needed to potentially update UI when coming back from a review update window
        reviewsGridPane.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.focusedProperty().addListener((observableFocus, wasFocused, isNowFocused) -> {
                    if (isNowFocused) {
                        onFocusGained();            // Update UI after review updates
                    }
                });
            }
        });
    }

    private void setAverageRating() {
        Double avgRating = boardgame.getAvgRating(); //ControllerViewRegUserBoardgamesPage.getBgameRating(boardgame);
        //if (avgRating == null)
        //    ratingFromTop = ; // reviewMongoOp.getAvgRatingByBoardgameName(boardgame.getBoardgameName());
        String ratingAsString = (avgRating != -1.0) ? String.format("%.1f", avgRating) : NO_RATING;

        if (ratingAsString.equals(NO_RATING))
            this.tooltipLblRating.setShowDelay(Duration.ZERO);
        else
            this.averageRatingLabel.setTooltip(null);

        this.averageRatingLabel.setText(ratingAsString);

        //System.out.println("[DEBUG] avg rating set to: " + ratingAsString);
    }

    private void prepareScene() {
        this.shiftDownSingleObjectGridPane = false;
        clearFields();
        resetPage();
        this.selectedOperation = UserActivity.NO_EDIT;
        String boardgameId = (String) modelBean.getBean(Constants.SELECTED_BOARDGAME);
        boardgame = boardgameDBMongo.findBoardgameById(boardgameId).get();
        //totalReviewsCounter = boardgame.getReviewCount(); // boardgame.getReviews().size();
        setAverageRating();
        this.counterReviewsLabel.setText(String.valueOf(boardgame.getReviewCount()));
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

        // Popolamento delle liste e delle ListView
        if (!boardgame.getBoardgameCategory().isEmpty()) {
            this.categories.addAll(boardgame.getBoardgameCategory());
            this.categoriesListView.getItems().addAll(categories);
            this.listStringsCategories.addAll(categories);
            this.firstCategoryLabel.setText(categories.get(0));
        } else {
            this.firstCategoryLabel.setText("");
        }

        if (!boardgame.getBoardgameDesigner().isEmpty()) {
            this.designers.addAll(boardgame.getBoardgameDesigner());
            this.designersListView.getItems().addAll(designers);
            this.listStringsDesigners.addAll(designers);
            this.firstDesignerLabel.setText(designers.get(0));
        } else {
            this.firstDesignerLabel.setText("");
        }

        if (!boardgame.getBoardgamePublisher().isEmpty()) {
            this.publishers.addAll(boardgame.getBoardgamePublisher());
            this.publishersListView.getItems().addAll(publishers);
            this.listStringsPublishers.addAll(publishers);
            this.firstPublisherLabel.setText(publishers.get(0));
        } else {
            this.firstPublisherLabel.setText("");
        }
        initComboBox(categories, designers, publishers);
        setEditFieldsVisibility(false);
        prevNextButtonsCheck(boardgame.getReviewCount());
    }

    private void setImage() {
        Image imageInCache = ControllerObjectBoardgame.getImageFromCache(this.boardgame.getImage());
        if (imageInCache != null)
        {
            this.imageBoardgame.setImage(imageInCache);
            System.out.println("[INFO] Image loaded from cache.");
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
            String imagePath = getClass().getResource("/images/noImage.jpg").toExternalForm();
            Image image = new Image(imagePath);
            this.imageBoardgame.setImage(image);
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

    private void onFocusGained() {
        System.out.println("[DEBUG] reviews size: " + reviews.size());
        // Potentially update a review
        ReviewModelMongo updatedReview = (ReviewModelMongo) modelBean.getBean(Constants.UPDATED_REVIEW);
        if (updatedReview != null) {
            modelBean.putBean(Constants.UPDATED_REVIEW, null);
            reviews.replaceAll(review -> review.getId().equals(updatedReview.getId()) ? updatedReview : review);
            //boardgame.getReviews().replaceAll(review -> review.getId().equals(updatedReview.getId()) ? updatedReview : review);
            fillGridPane();
            setAverageRating();
        }
    }

    // Called whenever the author user of a review decides to delete that review. This method updates the review list and updates UI
    public void updateUIAfterReviewDeletion(String deletedReviewId) {
        reviews.removeIf(review -> review.getId().equals(deletedReviewId));
        boardgame = boardgameDBMongo.findBoardgameById(boardgame.getId()).get();
        //boardgame.getReviews().removeIf(review -> review.getId().equals(deletedReviewId));
        //totalReviewsCounter--;
        this.counterReviewsLabel.setText(String.valueOf(boardgame.getReviewCount()));
        fillGridPane();
        setAverageRating();
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

    public void onClickRefreshButton() {
        cleanFetchAndFill();
    }

    public void onClickCloseButton() {
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
        reviews.clear();
//        categories.clear();
//        designers.clear();
//        publishers.clear();
        skipCounter = 0;
        previousButton.setDisable(true);
        nextButton.setDisable(true);
        scrollSet.setVvalue(0);
        // Pulizia delle liste e delle ListView
        this.categories.clear();
        this.designers.clear();
        this.publishers.clear();
        this.categoriesListView.getItems().clear();
        this.designersListView.getItems().clear();
        this.publishersListView.getItems().clear();
        this.listStringsCategories.clear();
        this.listStringsDesigners.clear();
        this.listStringsPublishers.clear();
        shiftDownSingleObjectGridPane = false;
    }

    void prevNextButtonsCheck(int reviewsCount) {
        if ((reviewsCount > 0)) {
            if ((reviewsCount < LIMIT)) {
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
        prevNextButtonsCheck(reviews.size());

        return reviews;
    }

    void setGridPaneColumnAndRow() {
        columnGridPane = 0;
        rowGridPane = 1;
    }

    public void onClickAddReviewButton() {
        try {
            UserModelMongo user = (UserModelMongo) currentUser;
            scrollSet.setVvalue(0);
            this.addReviewButton.setDisable(true);
            Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTCREATEREVIEW.getFxmlFile());

            Slider ratingSlider = (Slider) loadViewItem.lookup("#ratingSlider");
            TextField reviewBodyArea = (TextField) loadViewItem.lookup("#bodyTextLabel");
            Button submitReviewButton = (Button) loadViewItem.lookup("#submitButton");
            Button cancelReviewButton = (Button) loadViewItem.lookup("#cancelButton");

            AnchorPane addReviewBox = new AnchorPane();
            addReviewBox.getChildren().add(loadViewItem);

            if (!reviews.isEmpty()) {
                if (reviews.size() == 1)
                    shiftDownSingleObjectGridPane = true;
                else
                    shiftDownSingleObjectGridPane = false;
                fillGridPane();
                reviewsGridPane.add(addReviewBox, 0, 1);
            } else {
                reviewsGridPane.getChildren().clear();
                reviewsGridPane.add(addReviewBox, 0, 1);
            }
            GridPane.setMargin(addReviewBox, new Insets(10, 5, 10, 90));

            // Submit review button behavior
            submitReviewButton.setOnAction(e -> {
                String reviewText = reviewBodyArea.getText();
                int reviewRating = (int) ratingSlider.getValue();

                ReviewModelMongo newReview = new ReviewModelMongo(
                        boardgame.getBoardgameName(),
                        user.getUsername(),
                        reviewRating,
                        reviewText,
                        new Date()
                );


                boolean savedReview = serviceReview.insertReview(
                        newReview,
                        boardgame,
                        user
                );

                if (savedReview) {
                    stageManager.showInfoMessage("Success", "Review added successfully");

                    setAverageRating();
                    reviews.add(0, newReview);
                    cleanFetchAndFill();

                    this.addReviewButton.setDisable(false);
                    modelBean.putBean(Constants.ADDED_REVIEW, newReview);

                    this.counterReviewsLabel.setText(String.valueOf(boardgame.getReviewCount()));
                }
                prevNextButtonsCheck(reviews.size());
            });

            // Discard review button behavior
            cancelReviewButton.setOnAction(e -> {
                boolean userChoice = stageManager.showDiscardReviewInfoMessage();
                if (userChoice) {
                    this.addReviewButton.setDisable(false);
                    cleanFetchAndFill();
                }
            });
        } catch (Exception ex) {
            stageManager.showInfoMessage("ERROR", "Something went wrong. Please try again in a while.");
            System.err.println("[ERROR] onClickAddReviewButton@ControllerViewDetailsBoardgamePage.java raised an exception: " + ex.getMessage());
        }
    }

    private void loadViewMessageInfo() {
        Parent loadViewItem = stageManager.loadViewNode(FxmlView.INFOMSGREVIEWS.getFxmlFile());
        AnchorPane noContentsYet = new AnchorPane();
        noContentsYet.getChildren().add(loadViewItem);

        reviewsGridPane.add(noContentsYet, 0, 1);
        GridPane.setMargin(noContentsYet, new Insets(18, 5, 15, 290));
    }

    @FXML
    void fillGridPane() {
        columnGridPane = 0;       // Needed to correctly position a single element in the gridpane
        if (reviews.size() == 1) {
            if (shiftDownSingleObjectGridPane)
                rowGridPane = 2;
            else
                rowGridPane = 0;
        } else {
            rowGridPane++;
        }

        reviewsGridPane.getChildren().clear();
        try {
            if (reviews.isEmpty()) {
                loadViewMessageInfo();
            } else {
                for (ReviewModelMongo review : reviews) {
                    AnchorPane reviewNode = createReviewViewNode(review);
                    addReviewToGridPane(reviewNode);
                }
            }
        } catch (Exception e) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Try again in a while.");
            System.err.println("[ERROR] fillGridPane@ControllerViewDetailsBoardgamePage.java raised an exception: " + e.getMessage());
        }
    }

    private AnchorPane createReviewViewNode(ReviewModelMongo review) {
        Parent loadViewItem;
        deletedReviewCallback = this::updateUIAfterReviewDeletion;
        if (review.getBody().isEmpty()) {
            loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTREVIEWBLANKBODY.getFxmlFile());
            // Setting review data - including callbacks for actions to be taken upon review deletion
            controllerObjectReviewBlankBody.setData(review, deletedReviewCallback);
        } else {
            loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTREVIEW.getFxmlFile());
            // Setting review data - including callbacks for actions to be taken upon review deletion
            controllerObjectReview.setData(review, deletedReviewCallback);
        }
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().add(loadViewItem);
        // Setting up what should be called upon review deletion using the delete review button


        return anchorPane;
    }

    private void addReviewToGridPane(AnchorPane reviewNode) {
        if (columnGridPane == 1) {
            columnGridPane = 0;
            rowGridPane++;
        }

        reviewsGridPane.add(reviewNode, columnGridPane++, rowGridPane); //(child,column,row)

        reviewsGridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
        reviewsGridPane.setPrefWidth(500);
        reviewsGridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);

        reviewsGridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
        reviewsGridPane.setPrefHeight(400);
        reviewsGridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);

        GridPane.setMargin(reviewNode, new Insets(10, 5, 10, 90));
    }

    private void cleanFetchAndFill() {
        resetPage();
        reviews.addAll(getData(this.boardgame.getBoardgameName()));
        fillGridPane();
        prevNextButtonsCheck(reviews.size());
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

    public void onClickSaveChangesButton() {

        if (boardgame != null) {
            String oldBoardgameName = this.boardgameNameLabel.getText();

            // Ottenere i dati dai campi di input
            String boardgameName = this.updateBgNameTextField.getText();
            String description = this.updateDescriptionTextField.getText()
                    .replaceAll("&#[0-9]+;", "")
                    .replaceAll("&[a-zA-Z0-9]+;", "");
            String minPlayerStr = this.updateMinPlayersTextField.getText();
            String maxPlayerStr = this.updateMaxPlayersTextField.getText();
            String playingTimeStr = this.updatePlayingTimeTextField.getText();
            String yearPublishedStr = this.updateYearOfPublicationTextField.getText();
            String minAgeStr = this.updateMinAgeTextField.getText();
            String image = this.updateImageLinkTextField.getText();

            // Verifica se la lista delle categorie è cambiata
            boolean isCategoryListChanged = !new HashSet<>(listStringsCategories).
                    containsAll(boardgame.getBoardgameCategory())
                    || !new HashSet<>(boardgame.getBoardgameCategory()).
                    containsAll(listStringsCategories);
            // Verifica se la lista dei designer è cambiata
            boolean isDesignerListChanged = !new HashSet<>(listStringsDesigners).
                    containsAll(boardgame.getBoardgameDesigner())
                    || !new HashSet<>(boardgame.getBoardgameDesigner()).
                    containsAll(listStringsDesigners);
            // Verifica se la lista dei publisher è cambiata
            boolean isPublisherListChanged = !new HashSet<>(listStringsPublishers).
                    containsAll(boardgame.getBoardgamePublisher())
                    || !new HashSet<>(boardgame.getBoardgamePublisher()).
                    containsAll(listStringsPublishers);


            // Creare un elenco di stringhe delle liste modificate per recuperare il
            List<String> modifiedLists = new ArrayList<>();
            if (isCategoryListChanged) modifiedLists.add("Categories List");
            if (isDesignerListChanged) modifiedLists.add("Designer List");
            if (isPublisherListChanged) modifiedLists.add("Publisher List");

            // Verifica se tutti i campi sono vuoti (escludendo le liste)
            if (boardgameName.isEmpty() && description.isEmpty() && minPlayerStr.isEmpty() &&
                    maxPlayerStr.isEmpty() && playingTimeStr.isEmpty() && yearPublishedStr.isEmpty() &&
                    minAgeStr.isEmpty() && image.isEmpty() && modifiedLists.isEmpty()) {

                stageManager.showInfoMessage("Update Error",
                        "All fields and lists are unchanged. " +
                                "Please modify at least one field or list.");
                return;
            }

            // Identifica se ci sono campi vuoti
            boolean hasEmptyFields = boardgameName.isEmpty() || description.isEmpty()
                    || minPlayerStr.isEmpty() || maxPlayerStr.isEmpty()
                    || playingTimeStr.isEmpty() || yearPublishedStr.isEmpty()
                    || minAgeStr.isEmpty() || image.isEmpty();

            // Messaggio relativizzato al caso di aggiornamento di solo liste
            if (hasEmptyFields && !modifiedLists.isEmpty()) {
                String modifiedListName = modifiedLists.size() == 1
                        ? modifiedLists.get(0)
                        : modifiedLists.size() == 3
                        ? "All Lists"
                        : String.join(", ", modifiedLists);
                boolean userConfirmed = stageManager.
                                        showConfirmUpdateBoardgameListInfoMessage(modifiedListName);
                if (!userConfirmed) {
                    return;
                }
            } else if (hasEmptyFields) {
                boolean userConfirmed = stageManager.showConfirmUpdateBoardgameInfoMessage();
                if (!userConfirmed) {
                    return;
                }
            }

            // Validazione e conversione dei campi numerici
            Integer minPlayer = validateIntegerField(minPlayerStr, "Minimum Players");
            Integer maxPlayer = validateIntegerField(maxPlayerStr, "Maximum Players");
            Integer playingTime = validateIntegerField(playingTimeStr, "Playing Time");
            Integer yearPublished = validateIntegerField(yearPublishedStr, "Year Published");
            Integer minAge = validateIntegerField(minAgeStr, "Minimum Age");

            // Variabile di validazione
            boolean isValid = true;

            // Controlla se un campo numerico contiene un valore non valido (ad es. testo non numerico)
            if (minPlayerStr.isEmpty() && maxPlayerStr.isEmpty()&& playingTimeStr.isEmpty()
                    && yearPublishedStr.isEmpty() && minAgeStr.isEmpty()) {
                // Passa direttamente senza errori
                isValid = true;
            } else {
                if (yearPublished != null && yearPublishedStr.length() > 4) {
                    stageManager.showInfoMessage("Validation Error",
                            "Year Published cannot contain more than 4 digits.");
                    isValid = false;
                }
                if (minPlayer != null && minPlayer == 0 || maxPlayer != null && maxPlayer == 0 ||
                        playingTime != null && playingTime == 0 || minAge != null && minAge == 0) {
                    stageManager.showInfoMessage("Validation Error",
                            "Numeric fields cannot contain the value 0.");
                    isValid = false;
                }
                if (minAge != null && minAge > 100) {
                    stageManager.showInfoMessage("Validation Error",
                            "Minimum Age cannot exceed 100.");
                    isValid = false;
                }
                if (minPlayer != null && maxPlayer != null && minPlayer > maxPlayer) {
                    stageManager.showInfoMessage("Validation Error",
                            "Minimum Players cannot be greater than Maximum Players.");
                    isValid = false;
                }
            }

            if (isValid) {
                // Esegui l'aggiornamento del modello del gioco
                BoardgameModelMongo updatedBoardgame = new BoardgameModelMongo();
                updatedBoardgame.setId(boardgame.getId()); // Mantieni lo stesso ID

                // Aggiorna solo i campi riempiti o modificati
                updatedBoardgame.setImage(image.isEmpty()
                                                  ? boardgame.getImage() : image);
                updatedBoardgame.setBoardgameName(boardgameName.isEmpty()
                                                  ? boardgame.getBoardgameName() : boardgameName);
                updatedBoardgame.setDescription(description.isEmpty()
                                                  ? boardgame.getDescription() : description);
                updatedBoardgame.setMinPlayers(minPlayer != null
                                                  ? minPlayer : boardgame.getMinPlayers());
                updatedBoardgame.setMaxPlayers(maxPlayer != null
                                                  ? maxPlayer : boardgame.getMaxPlayers());
                updatedBoardgame.setPlayingTime(playingTime != null
                                                  ? playingTime : boardgame.getPlayingTime());
                updatedBoardgame.setYearPublished(yearPublished != null
                                                  ? yearPublished : boardgame.getYearPublished());
                updatedBoardgame.setMinAge(minAge != null
                                                  ? minAge : boardgame.getMinAge());

                updatedBoardgame.setBoardgameCategory(
                        isCategoryListChanged ? new ArrayList<>(listStringsCategories)
                                              : boardgame.getBoardgameCategory());
                updatedBoardgame.setBoardgameDesigner(
                        isDesignerListChanged ? new ArrayList<>(listStringsDesigners)
                                              : boardgame.getBoardgameDesigner());
                updatedBoardgame.setBoardgamePublisher(
                        isPublisherListChanged ? new ArrayList<>(listStringsPublishers)
                                               : boardgame.getBoardgamePublisher());

                updatedBoardgame.setAvgRating(boardgame.getAvgRating());
                updatedBoardgame.setReviewCount(boardgame.getReviewCount());


                modelBean.putBean(Constants.UPDATED_BOARDGAME, BoardgameModelNeo4j.castBoardgameMongoInBoardgameNeo(updatedBoardgame));

                // Esegui l'aggiornamento verso il database e in Grafica se tutto va bene
                if (updateDbms(updatedBoardgame, oldBoardgameName)) {
                    modelBean.putBean(Constants.SELECTED_BOARDGAME, updatedBoardgame.getId());
                    stageManager.showInfoMessage("Update Info",
                            "The boardgame information has been successfully updated!");
                    prepareScene();
                    onClickRefreshButton();
                }
            }
        } else {
            stageManager.showInfoMessage("Update Error",
                    "There is no selected boardgame to update.");
        }
    }

    private boolean updateDbms(BoardgameModelMongo newBoardgame, String oldBoardgameName){

        boolean updateBoardgameOperation = serviceBoardgame.updateBoardgame(newBoardgame, oldBoardgameName);

        if (!updateBoardgameOperation) {
            modelBean.putBean(Constants.UPDATED_BOARDGAME, null);
            stageManager.showInfoMessage("Update Error: ",
                    "There was an error updating Boardgame information. " +
                            "Please try again.");
            prepareScene();
            return false;
        }
        return true;
    }

    // Metodo per validare e convertire i campi numerici
    private Integer validateIntegerField(String value, String fieldName) {
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            stageManager.showInfoMessage("Validation Error",
                    fieldName + " must be a valid number.");
            return null;
        }
    }


    public void onClickEditBoardgameButton() {
        this.selectedOperation = UserActivity.EDIT_INFO;
        this.cancelButton.setVisible(true);
        this.saveChangesButton.setVisible(true);
        scrollSet.setVvalue(0);
        setEditFieldsVisibility(true);
    }

    public void onClickCancelButton(){
        boolean userConfirmed = stageManager.showConfirmDiscardEditBoardgameInfoMessage();
        if (!userConfirmed) {
            return;
        }
        clearFields();
        prepareScene();
        prevNextButtonsCheck(boardgame.getReviewCount());
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
        if(!isVisible && currentUser.get_class().equals("admin")){
            this.addReviewButton.setDisable(true);
        } else {
            this.addReviewButton.setDisable(isVisible);
        }
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