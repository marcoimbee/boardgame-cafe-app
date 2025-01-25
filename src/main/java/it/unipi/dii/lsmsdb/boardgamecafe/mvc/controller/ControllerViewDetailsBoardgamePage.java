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
    @FXML
    private ListView<String> categoriesListView;
    @FXML
    private ListView<String> designersListView;
    @FXML
    private ListView<String> publishersListView;
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
    @FXML
    private ComboBox<String> comboBoxFullCategories;
    @FXML
    private ComboBox<String> comboBoxFullDesigners;
    @FXML
    private ComboBox<String> comboBoxFullPublishers;
    @FXML
    private ImageView imageBoardgame;
    @FXML
    private GridPane reviewsGridPane;
    @FXML
    private ScrollPane scrollSet;
    @FXML
    protected Tooltip tooltipLblRating;

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
    @Autowired
    @Lazy
    private StageManager stageManager;

    public enum UserActivity {
        EDIT_INFO,
        NO_EDIT
        }
    private final String NO_RATING = "-----";

    private final List<String> listStringsCategories = new ArrayList<>();
    private final List<String> listStringsDesigners = new ArrayList<>();
    private final List<String> listStringsPublishers = new ArrayList<>();

    private List<ReviewModelMongo> reviews = new ArrayList<>();
    private List<String> categories = new ArrayList<>();
    private List<String> designers = new ArrayList<>();
    private List<String> publishers = new ArrayList<>();

    private BoardgameModelMongo boardgame;
    private static GenericUserModelMongo currentUser;
    private UserActivity selectedOperation;

    private boolean shiftDownSingleObjectGridPane; // false: no - true: yes
    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;
    private final static int SKIP = 10;     // How many posts to skip per time
    private final static int LIMIT = 10;    // How many posts to show for each page

    private final String promptTextFullCategories = "See Full Categories";
    private final String promptTextFullDesigners = "See Full Designers";
    private final String promptTextFullPublishers = "See Full Publishers";

    private Consumer<String> deletedReviewCallback;

    public ControllerViewDetailsBoardgamePage() {}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        Double avgRating = boardgame.getAvgRating();
        String ratingAsString = (avgRating != -1.0) ? String.format("%.1f", avgRating) : NO_RATING;

        if (ratingAsString.equals(NO_RATING)) {
            this.tooltipLblRating.setShowDelay(Duration.ZERO);
        } else {
            this.averageRatingLabel.setTooltip(null);
        }
        this.averageRatingLabel.setText(ratingAsString);
    }

    private void prepareScene() {
        this.shiftDownSingleObjectGridPane = false;
        clearFields();
        resetPage();
        this.selectedOperation = UserActivity.NO_EDIT;
        String boardgameId = (String) modelBean.getBean(Constants.SELECTED_BOARDGAME);
        boardgame = boardgameDBMongo.findBoardgameById(boardgameId).get();
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
        if (imageInCache != null) {
            this.imageBoardgame.setImage(imageInCache);
            System.out.println("[INFO] Image loaded from cache.");
            return;
        }

        try {
            URI uri = new URI(this.boardgame.getImage());   // Creating URI
            URL url = uri.toURL();          // Converting URI to URL
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "JavaFX Application");

            try (InputStream inputStream = connection.getInputStream()) {
                byte[] imageBytes = readFullInputStream(inputStream);
                Image downloadedImage = new Image(new ByteArrayInputStream(imageBytes));
                this.imageBoardgame.setImage(downloadedImage);
            } catch (Exception e) {
                String imagePath = getClass().getResource("/images/noImage.jpg").toExternalForm();
                Image image = new Image(imagePath);
                this.imageBoardgame.setImage(image);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] setImage()@ControllerObjectBoardgame.java raised an exception: " + e.getMessage());
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
        // Potentially update a review
        ReviewModelMongo updatedReview = (ReviewModelMongo) modelBean.getBean(Constants.UPDATED_REVIEW);
        if (updatedReview != null) {
            modelBean.putBean(Constants.UPDATED_REVIEW, null);
            reviews.replaceAll(review -> review.getId().equals(updatedReview.getId()) ? updatedReview : review);
            boardgame = boardgameDBMongo.findBoardgameById(boardgame.getId()).get();
            fillGridPane();
            setAverageRating();
        }
    }

    /*
        Called whenever the author user of a review decides to delete that review.
        This method updates the review list and updates UI
     */
    public void updateUIAfterReviewDeletion(String deletedReviewId) {
        reviews.removeIf(review -> review.getId().equals(deletedReviewId));
        boardgame = boardgameDBMongo.findBoardgameById(boardgame.getId()).get();
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
            String boardgameName = this.boardgame.getBoardgameName();
            if (serviceBoardgame.deleteBoardgame(this.boardgame)){
                modelBean.putBean(Constants.DELETED_BOARDGAME, boardgameName);
                stageManager.closeStage();
                stageManager.showInfoMessage("INFO", "The boardgame was successfully deleted.");
            } else {
                modelBean.putBean(Constants.SELECTED_BOARDGAME, null);
                stageManager.closeStage();
                stageManager.showInfoMessage("INFO",
                        "Something went wrong while deleting the boardgame.");
            }
        } catch (Exception ex) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Please Try again in a while.");
            System.err.println("[ERROR] onClickDeleteButton()@ControllerViewDetailsBoardgamePage.java raised an exception: " + ex.getMessage());
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
        reviewsGridPane.getChildren().clear();
        reviews.clear();
        skipCounter += SKIP;
        reviews.addAll(getData(this.boardgame.getBoardgameName()));
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    @FXML
    void onClickPrevious() {
        reviewsGridPane.getChildren().clear();
        reviews.clear();
        skipCounter -= SKIP;
        reviews.addAll(getData(this.boardgame.getBoardgameName()));
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    void resetPage() {
        this.tooltipLblRating.hide();
        reviews.clear();
        skipCounter = 0;
        previousButton.setDisable(true);
        nextButton.setDisable(true);
        scrollSet.setVvalue(0);
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
        List<ReviewModelMongo> reviews = reviewMongoOp.findRecentReviewsByBoardgame(boardgameName, LIMIT, skipCounter);
        prevNextButtonsCheck(reviews.size());

        return reviews;
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
                if (reviews.size() == 1) {
                    shiftDownSingleObjectGridPane = true;
                } else {
                    shiftDownSingleObjectGridPane = false;
                }
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
                    stageManager.showInfoMessage("INFO", "Review added successfully");

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
            stageManager.showInfoMessage("INFO", "Something went wrong. Please try again in a while.");
            System.err.println("[ERROR] onClickAddReviewButton()@ControllerViewDetailsBoardgamePage.java raised an exception: " + ex.getMessage());
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
        columnGridPane = 0;       // Needed to correctly position a single element in the grid pane
        if (reviews.size() == 1) {
            if (shiftDownSingleObjectGridPane) {
                rowGridPane = 2;
            } else {
                rowGridPane = 0;
            }
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
            stageManager.showInfoMessage("INFO", "Something went wrong. Please try again in a while.");
            System.err.println("[ERROR] fillGridPane()@ControllerViewDetailsBoardgamePage.java raised an exception: " + e.getMessage());
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

        return anchorPane;
    }

    private void addReviewToGridPane(AnchorPane reviewNode) {
        if (columnGridPane == 1) {
            columnGridPane = 0;
            rowGridPane++;
        }

        reviewsGridPane.add(reviewNode, columnGridPane++, rowGridPane);

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

            // Verifying if lists are changed
            boolean isCategoryListChanged = !new HashSet<>(listStringsCategories).
                    containsAll(boardgame.getBoardgameCategory())
                    || !new HashSet<>(boardgame.getBoardgameCategory()).
                    containsAll(listStringsCategories);
            boolean isDesignerListChanged = !new HashSet<>(listStringsDesigners).
                    containsAll(boardgame.getBoardgameDesigner())
                    || !new HashSet<>(boardgame.getBoardgameDesigner()).
                    containsAll(listStringsDesigners);
            boolean isPublisherListChanged = !new HashSet<>(listStringsPublishers).
                    containsAll(boardgame.getBoardgamePublisher())
                    || !new HashSet<>(boardgame.getBoardgamePublisher()).
                    containsAll(listStringsPublishers);

            List<String> modifiedLists = new ArrayList<>();
            if (isCategoryListChanged) modifiedLists.add("Categories List");
            if (isDesignerListChanged) modifiedLists.add("Designer List");
            if (isPublisherListChanged) modifiedLists.add("Publisher List");

            if (boardgameName.isEmpty() && description.isEmpty() && minPlayerStr.isEmpty() &&
                    maxPlayerStr.isEmpty() && playingTimeStr.isEmpty() && yearPublishedStr.isEmpty() &&
                    minAgeStr.isEmpty() && image.isEmpty() && modifiedLists.isEmpty()) {

                stageManager.showInfoMessage("Update Error",
                        "All fields and lists are unchanged. " +
                                "Please modify at least one field or list.");
                return;
            }

            boolean hasEmptyFields = boardgameName.isEmpty() || description.isEmpty()
                    || minPlayerStr.isEmpty() || maxPlayerStr.isEmpty()
                    || playingTimeStr.isEmpty() || yearPublishedStr.isEmpty()
                    || minAgeStr.isEmpty() || image.isEmpty();

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

            Integer minPlayer = validateIntegerField(minPlayerStr, "Minimum Players");
            Integer maxPlayer = validateIntegerField(maxPlayerStr, "Maximum Players");
            Integer playingTime = validateIntegerField(playingTimeStr, "Playing Time");
            Integer yearPublished = validateIntegerField(yearPublishedStr, "Year Published");
            Integer minAge = validateIntegerField(minAgeStr, "Minimum Age");

            boolean isValid = true;

            if (minPlayerStr.isEmpty() && maxPlayerStr.isEmpty()&& playingTimeStr.isEmpty()
                    && yearPublishedStr.isEmpty() && minAgeStr.isEmpty()) {
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
                BoardgameModelMongo updatedBoardgame = new BoardgameModelMongo();
                updatedBoardgame.setId(boardgame.getId());      // Keeping the same ID

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

                if (updateDbms(updatedBoardgame, oldBoardgameName)) {
                    modelBean.putBean(Constants.SELECTED_BOARDGAME, updatedBoardgame.getId());
                    stageManager.showInfoMessage("INFO", "The boardgame has been successfully updated.");
                    prepareScene();
                    onClickRefreshButton();
                }
            }
        } else {
            stageManager.showInfoMessage("INFO", "No selected boardgame to update.");
        }
    }

    private boolean updateDbms(BoardgameModelMongo newBoardgame, String oldBoardgameName){
        boolean updateBoardgameOperation = serviceBoardgame.updateBoardgame(newBoardgame, oldBoardgameName);

        if (!updateBoardgameOperation) {
            modelBean.putBean(Constants.UPDATED_BOARDGAME, null);
            stageManager.showInfoMessage("ERROR", "Something went wrong while updating the boardgame. Please try again in a while.");
            prepareScene();
            return false;
        }
        return true;
    }

    private Integer validateIntegerField(String value, String fieldName) {
        if (value.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            stageManager.showInfoMessage("INFO", "'" + fieldName + "' must be a valid number.");
            return null;
        }
    }

    public void onClickEditBoardgameButton() {
        this.selectedOperation = UserActivity.EDIT_INFO;
        this.cancelButton.setVisible(true);
        this.saveChangesButton.setVisible(true);
        scrollSet.setVvalue(0);
        prepareScene();
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
            stageManager.showInfoMessage("INFO", "'Category' field cannot be empty.");
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
            stageManager.showInfoMessage("INFO", "'Designer' field cannot be empty.");
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
            stageManager.showInfoMessage("INFO", "'Publisher' field cannot be empty.");
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
        this.updateDescriptionTextField.clear();
        this.updateDescriptionTextField.setPromptText("Write the boardgame description here...");
        this.updateBgNameTextField.clear();
        this.updateBgNameTextField.setPromptText("Write the boardgame name here...");
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
        this.updateImageLinkTextField.setPromptText("Write the image address here...");
        this.updateCategoryTextField.clear();
        this.updateCategoryTextField.setPromptText("Write the category here...");
        this.updateDesignerTextField.clear();
        this.updateDesignerTextField.setPromptText("Write the designer here...");
        this.updatePublisherTextField.clear();
        this.updatePublisherTextField.setPromptText("Write the publisher here...");
        this.categoriesListView.getItems().clear();
        this.designersListView.getItems().clear();
        this.publishersListView.getItems().clear();
    }
}
