package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.BoardgameDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.ReviewDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.services.ReviewService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.application.Platform;
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
    private ControllerObjectReview controllerObjectReview;
    @Autowired
    private ModelBean modelBean;
    @Autowired
    private ReviewService serviceReview;

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


    @Autowired
    @Lazy
    private StageManager stageManager;

    private Consumer<String> deletedCommentCallback;

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
        if (!currentUser.get_class().equals("admin")) {
            editBoardgameButton.setVisible(false);       // Making the edit button invisible
            deleteButton.setVisible(false);     // Making the delete button invisible
        }
        prepareScene();

        boardgame.getReviews().sort(Comparator.comparing(ReviewModelMongo::getDateOfReview).reversed());
        reviews.addAll(boardgame.getReviews());
        fillGridPane();


//        // Page focus listener - needed to potentially update UI when coming back from a post update window
//        reviewsGridPane.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
//            if (newScene != null) {
//                Stage stage = (Stage) newScene.getWindow();
//                stage.focusedProperty().addListener((observableFocus, wasFocused, isNowFocused) -> {
//                    if (isNowFocused) {
//                        onFocusGained();            // Update UI after post updates
//                    }
//                });
//            }
//        });
    }

    private void prepareScene() {
        Double ratingFromTop = ControllerViewRegUserBoardgamesPage.getBgameRating(boardgame);
        if (ratingFromTop == null)
            ratingFromTop = reviewMongoOp.getAvgRatingByBoardgameName(boardgame.getBoardgameName());
        String ratingAsString = String.format("%.1f", ratingFromTop);
        this.averageRatingLabel.setText(ratingAsString);
        this.setImage();
        this.boardgameNameLabel.setText(this.boardgame.getBoardgameName());
        this.descriptionTextArea.setText(this.boardgame.getDescription());
        this.minPlayerLabel.setText(String.valueOf(this.boardgame.getMinPlayers()));
        this.maxPlayerLabel.setText(String.valueOf(this.boardgame.getMaxPlayers()));
        this.playingTimeLabel.setText(String.valueOf(this.boardgame.getPlayingTime()));
        this.yearPublishedLabel.setText(String.valueOf(this.boardgame.getYearPublished()));
        this.minAgeLabel.setText(String.valueOf(this.boardgame.getMinAge()));
        //*********** Categories, Designers and Publishers management ***********
        this.categories.addAll(boardgame.getBoardgameCategory());
        this.designers.addAll(boardgame.getBoardgameDesigner());
        this.publishers.addAll(boardgame.getBoardgamePublisher());
        this.firstCategoryLabel.setText(categories.get(0));
        this.firstDesignerLabel.setText(designers.get(0));
        this.firstPublisherLabel.setText(publishers.get(0));
        initComboBox(categories, designers, publishers);
    }

    private void setImage() {
        Image imageInCache = ControllerObjectBoardgame.getImageFromCache(this.boardgame.getImage());
        if (imageInCache != null)
        {
            this.imageBoardgame.setImage(imageInCache);
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

//    public void onFocusGained() {
//        BoardgameModelMongo updatedBoardgame = (BoardgameModelMongo) modelBean.getBean(Constants.SELECTED_BOARDGAME);
//        this.tagBoardgameLabel.setText(updatedPost.getTag());
//        this.postTitleTextArea.setText(updatedPost.getTitle());
//        this.postBodyTextArea.setText(updatedPost.getText());
//
//        // Potentially update a comment
//        CommentModelMongo updatedComment = (CommentModelMongo) modelBean.getBean(Constants.UPDATED_COMMENT);
//        if (updatedComment != null) {
//            modelBean.putBean(Constants.UPDATED_COMMENT, null);
//
//            reviews.replaceAll(comment -> comment.getId().equals(updatedComment.getId()) ? updatedComment : comment);
//            fillGridPane();
//        }
//    }

    // Called whenever the author user of a comment decides to delete that comment. This method updates the comments list and updates UI
//    public void updateUIAfterCommentDeletion(String deletedCommentId) {
//        reviews.removeIf(comment -> comment.getId().equals(deletedCommentId));
//        this.counterCommentsLabel.setText(String.valueOf(reviews.size()));
//        fillGridPane();
//    }

    public void onClickDeleteButton() {

    }

    public void onClickEditBoardgameButton() {
//        stageManager.showWindow(FxmlView.EDIT_POST);            // Do not close underlying page, just show the little post editing window
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
        reviews.addAll(getData(this.boardgame.getId()));
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
        reviews.addAll(getData(this.boardgame.getId()));
        //put all boardgames in the Pane
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    void resetPage() {
        //clear variables
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

        GridPane.setMargin(noContentsYet, new Insets(330, 100, 100, 265));
    }

    @FXML
    void fillGridPane() {
        // Setting up what method should be called upon comment deletion
        //deletedCommentCallback = this::updateUIAfterCommentDeletion;

        reviewsGridPane.getChildren().clear();

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
        Optional<BoardgameModelMongo> postFromMongo = boardgameDBMongo.findBoardgameById(this.boardgame.getId());
        postFromMongo.ifPresent(boardgameMongo -> boardgame = boardgameMongo);
        boardgame.getReviews().sort(Comparator.comparing(ReviewModelMongo::getDateOfReview).reversed());
        reviews.addAll(boardgame.getReviews());
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