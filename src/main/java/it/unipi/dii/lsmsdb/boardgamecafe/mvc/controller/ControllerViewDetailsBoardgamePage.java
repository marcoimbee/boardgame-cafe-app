package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.BoardgameDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.ReviewDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.services.BoardgameService;
import it.unipi.dii.lsmsdb.boardgamecafe.services.ReviewService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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

    private final String NO_RATING = "-----";
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

    private static UserModelMongo currentUser;

    private static int totalReviewsCounter;

    //Utils Variables
    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;
    private final static int SKIP = 10; //how many posts to skip per time
    private final static int LIMIT = 10; //how many posts to show for each page

    private final String promptTextFullCategories = "See Full Categories";
    private final String promptTextFullDesigners = "See Full Designers";
    private final String promptTextFullPublishers = "See Full Publishers";


    @Autowired
    @Lazy
    private StageManager stageManager;

    private Consumer<String> deletedReviewCallback;

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

        totalReviewsCounter = boardgame.getReviews().size();
        System.out.println("[INFO] Found " + totalReviewsCounter + " reviews for '" + boardgame.getBoardgameName() + "'");
        reviews.addAll(getData(this.boardgame.getBoardgameName()));
        prepareScene();
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
        Double ratingFromTop = ControllerViewRegUserBoardgamesPage.getBgameRating(boardgame);
        if (ratingFromTop == null)
            ratingFromTop = reviewMongoOp.getAvgRatingByBoardgameName(boardgame.getBoardgameName());
        String ratingAsString = (ratingFromTop != null) ? String.format("%.1f", ratingFromTop) : NO_RATING;

        if (ratingAsString.equals(NO_RATING))
            this.tooltipLblRating.setShowDelay(Duration.ZERO);
        else
            this.averageRatingLabel.setTooltip(null);

        this.averageRatingLabel.setText(ratingAsString);

        //System.out.println("[DEBUG] avg rating set to: " + ratingAsString);
    }

    private void prepareScene() {
        setAverageRating();
        this.counterReviewsLabel.setText(String.valueOf(totalReviewsCounter));
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

        if (!categories.isEmpty())
            this.firstCategoryLabel.setText(categories.get(0));
        else
            this.firstCategoryLabel.setText("");
        if (!designers.isEmpty())
            this.firstDesignerLabel.setText(designers.get(0));
        else
            this.firstDesignerLabel.setText("");
        if (!publishers.isEmpty())
            this.firstPublisherLabel.setText(publishers.get(0));
        else
            this.firstPublisherLabel.setText("");
        initComboBox(categories, designers, publishers);
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
            fillGridPane();
        }
    }

    // Called whenever the author user of a review decides to delete that review. This method updates the review list and updates UI
    public void updateUIAfterReviewDeletion(String deletedReviewId) {
        reviews.removeIf(review -> review.getId().equals(deletedReviewId));
        totalReviewsCounter--;
        this.counterReviewsLabel.setText(String.valueOf(totalReviewsCounter));
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

    public void onClickEditBoardgameButton() {
//        stageManager.showWindow(FxmlView.EDIT_POST);            // Do not close underlying page, just show the little post editing window
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

    public void onClickAddReviewButton() {
        try {
            this.addReviewButton.setDisable(true);
            Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTCREATEREVIEW.getFxmlFile());

            Slider ratingSlider = (Slider) loadViewItem.lookup("#ratingSlider");
            TextField reviewBodyArea = (TextField) loadViewItem.lookup("#bodyTextLabel");
            Button submitReviewButton = (Button) loadViewItem.lookup("#submitButton");
            Button cancelReviewButton = (Button) loadViewItem.lookup("#cancelButton");

            AnchorPane addReviewBox = new AnchorPane();
            addReviewBox.getChildren().add(loadViewItem);

            if (reviews.isEmpty()) {
                resetPage();
                reviewsGridPane.add(addReviewBox, 0, rowGridPane);
            } else {
                resetPage();
                reviewsGridPane.add(addReviewBox, 0, 0);
            }
            GridPane.setMargin(addReviewBox, new Insets(8, 5, 10, 90));

            // Submit review button behavior
            submitReviewButton.setOnAction(e -> {
                String reviewText = reviewBodyArea.getText();
                int reviewRating = (int) ratingSlider.getValue();

                ReviewModelMongo newReview = new ReviewModelMongo(
                        boardgame.getBoardgameName(),
                        currentUser.getUsername(),
                        reviewRating,
                        reviewText,
                        new Date()
                );

                boolean savedReview = serviceReview.insertReview(
                        newReview,
                        boardgame,
                        currentUser
                );

                if (savedReview) {
                    stageManager.showInfoMessage("Success", "Review added successfully");

                    setAverageRating();
                    reviews.add(0, newReview);
                    cleanFetchAndFill();

                    this.addReviewButton.setDisable(false);
                    modelBean.putBean(Constants.ADDED_REVIEW, newReview);

                    totalReviewsCounter++;
                    this.counterReviewsLabel.setText(String.valueOf(totalReviewsCounter));
                }
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
        columnGridPane = 0;       // Needed to correctly position a single element in the gridpane
        if (reviews.size() == 1) {
            rowGridPane = 0;
        } else {
            rowGridPane = 1;
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
        deletedReviewCallback = this::updateUIAfterReviewDeletion;

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

        GridPane.setMargin(reviewNode, new Insets(4, 5, 10, 90));
    }

    private void cleanFetchAndFill() {
        resetPage();
        reviews.addAll(getData(this.boardgame.getBoardgameName()));
        fillGridPane();
    }

    private void initComboBox(List<String> categories,
                              List<String> designers,
                              List<String> publishers) {

        this.comboBoxFullCategories.getItems().addAll(categories);
        this.comboBoxFullDesigners.getItems().addAll(designers);
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
}