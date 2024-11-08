package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.PostService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


@Component
public class ControllerViewRegUserPostsPage implements Initializable {
    @FXML
    private Button boardgamesCollectionButton;
    @FXML
    private Button boardgamePostsButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button previousButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button clearFieldButton;
    @FXML
    private Button searchUserButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button profileButton;
    @FXML
    private Button accountDetailsButton;
    @FXML
    private ChoiceBox<String> whatPostsToShowChoiceBox;
    @FXML
    private Button testButton;
    @FXML
    private TextField textFieldSearch;
    @FXML
    private GridPane postGridPane;
    @FXML
    private ScrollPane scrollSet;
    @Autowired
    private PostDBMongo postDBMongo;
    @Autowired
    private PostDBNeo4j postDBNeo4j;
    @Autowired
    private PostService postService;
    @Autowired
    private ControllerObjectPost controllerObjectPost;
    private final StageManager stageManager;

    // Choice box variables
    ObservableList<String> whatPostsToShowList = FXCollections.observableArrayList(
            "Posts by followed users",
            "Posts liked by followed users",
            "Posts commented by followed users"
    );

    //Post Variables
    private List<PostModelMongo> posts = new ArrayList<>();

    private PostListener postListener;

    //Utils Variables
    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;            // Ho many times the user clicked on the 'Next' button
    private final static int SKIP = 10;     // How many posts to skip each time
    private final static int LIMIT = 10;    // How many posts to show in each page

    private final static Logger logger = LoggerFactory.getLogger(PostDBMongo.class);

    private enum PostsToFetch {
        POSTS_BY_FOLLOWED_USERS,
        POSTS_LIKED_BY_FOLLOWED_USERS,
        POSTS_COMMENTED_BY_FOLLOWED_USERS
    };
    private static PostsToFetch currentlyShowing;       // Global indicator of what type of post is being shown on the page

    @Autowired
    @Lazy
    public ControllerViewRegUserPostsPage(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    @FXML
    public void initialize(URL location, ResourceBundle resources) {

        this.boardgamePostsButton.setDisable(true);
        this.previousButton.setDisable(true);
        this.nextButton.setDisable(true);
        resetPage();

        currentlyShowing = PostsToFetch.POSTS_BY_FOLLOWED_USERS;            // Static var init

        // Choice box init
        whatPostsToShowChoiceBox.setValue(whatPostsToShowList.getFirst());      // Default choice box string
        whatPostsToShowChoiceBox.setItems(whatPostsToShowList);                 // Setting the other options in choice box

        // Adding listeners to option selection: change indicator of what is displayed on the screen and retrieve results
        whatPostsToShowChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateCurrentlyShowing(newValue);
            onSelectChoiceBoxOption();
        });

        onSelectChoiceBoxOption();        // Show posts by followed users by default
    }

    private void updateCurrentlyShowing(String choiceBoxValue) {
        if (choiceBoxValue.equals(whatPostsToShowList.getFirst())) {
            currentlyShowing = PostsToFetch.POSTS_BY_FOLLOWED_USERS;
        }

        if (choiceBoxValue.equals(whatPostsToShowList.get(1))) {
            currentlyShowing = PostsToFetch.POSTS_LIKED_BY_FOLLOWED_USERS;
        }

        if (choiceBoxValue.equals(whatPostsToShowList.get(2))) {
            currentlyShowing = PostsToFetch.POSTS_COMMENTED_BY_FOLLOWED_USERS;
        }
    }

    public void onSelectChoiceBoxOption() {
        posts.clear();                      // Remove old posts
        posts.addAll(getData());            // Add new posts
        fillGridPane();
    }

    public void onClickBoardgamesCollection(ActionEvent actionEvent) {
        stageManager.showWindow(FxmlView.REGUSERBOARDGAMES);
        stageManager.closeStageButton(this.boardgamesCollectionButton);
    }

    public void onClickSearch() {
        String text = this.textFieldSearch.getText();

        stageManager.showInfoMessage("Info Text", text);
    }

    public void onClickClearField() {
        this.textFieldSearch.clear();
    }

    @FXML
    void onClickNext() {
        //clear variables
        postGridPane.getChildren().clear();
        posts.clear();

        //update the skipcounter
        skipCounter += SKIP;

        //retrieve boardgames
        posts.addAll(getData());
        //put all boardgames in the Pane
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    @FXML
    void onClickPrevious() {
        //clear variables
        postGridPane.getChildren().clear();
        posts.clear();

        //update the skipcounter
        skipCounter -= SKIP;

        //retrieve boardgames
        posts.addAll(getData());
        //put all boardgames in the Pane
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    void resetPage() {
        //clear variables
        postGridPane.getChildren().clear();
        posts.clear();
        skipCounter = 0;
        currentlyShowing = PostsToFetch.POSTS_BY_FOLLOWED_USERS;
    }

    private List<PostModelMongo> getData(){
        List<PostModelMongo> posts = switch (currentlyShowing) {        // Decide what type of posts need to be fetched
            case POSTS_BY_FOLLOWED_USERS ->
                    postService.findPostsByFollowedUsers("blackpanda723", LIMIT, skipCounter);        // TODO: change this. implement new in post service
            case POSTS_LIKED_BY_FOLLOWED_USERS ->
                    postService.suggestPostLikedByFollowedUsers("blackpanda723", LIMIT, skipCounter);
            case POSTS_COMMENTED_BY_FOLLOWED_USERS ->
                    postService.suggestPostCommentedByFollowedUsers("blackpanda723", LIMIT, skipCounter);
        };

        prevNextButtonsCheck(posts.size());

        return posts;
    }

    void prevNextButtonsCheck(int fetchedPostsCount) {
        if (fetchedPostsCount > 0) {                    // Fetched some posts
            if (fetchedPostsCount < LIMIT) {            // Fetched posts number is less than those displayable on page
                if (skipCounter == 0) {                 // We are in the first page - disable both buttons
                    previousButton.setDisable(true);
                    nextButton.setDisable(true);
                } else {                                // We are not in the first page - enable going to previous page but disable next page
                    previousButton.setDisable(false);
                    nextButton.setDisable(true);
                }
            } else {                // Fetched a number of posts greater than those displayable on screen
                if (skipCounter == 0){                  // We are in the first page - enable going to next page but disable going back
                    previousButton.setDisable(true);
                    nextButton.setDisable(false);
                } else {                                // We are not in the first page - enable everything
                    previousButton.setDisable(false);
                    nextButton.setDisable(false);
                }
            }
        } else {
            if(skipCounter == 0) {   // No results fetched and in first page - disable both buttons
                previousButton.setDisable(true);
                nextButton.setDisable(true);
            } else {         // No results fetched and not in first page - disable next button but can to previous page
                previousButton.setDisable(false);
                nextButton.setDisable(true);
            }
        }
    }

    void setGridPaneColumnAndRow(){
        columnGridPane = 0;
        rowGridPane = 1;
    }

    @FXML
    void fillGridPane() {
        columnGridPane = 0;
        rowGridPane = 0;
        setGridPaneColumnAndRow();

        postListener = (MouseEvent mouseEvent, PostModelMongo post) -> {
            // Logica per mostrare i dettagli del post usando StageManager
            stageManager.switchScene(FxmlView.USERPROFILEPAGE);
            stageManager.closeStageMouseEvent(mouseEvent);
        };

        postGridPane.getChildren().clear();         // Removing old posts

        try {
            if (posts.isEmpty()) {
                stageManager.showInfoMessage("INFO", "No posts to show!");
            } else {            //CREATE FOR EACH POST AN ITEM (ObjectPosts)
                for (PostModelMongo post : posts) { // iterando lista di posts

                    Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTPOST.getFxmlFile());

                    AnchorPane anchorPane = new AnchorPane();
                    anchorPane.getChildren().add(loadViewItem);

                    controllerObjectPost.setData(post, postListener);

                    //choice number of column
                    if (columnGridPane == 1) {
                        columnGridPane = 0;
                        rowGridPane++;
                    }

                    postGridPane.add(anchorPane, columnGridPane++, rowGridPane); //(child,column,row)
                    //DISPLAY SETTINGS
                    //set grid width
                    postGridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
                    postGridPane.setPrefWidth(500);
                    postGridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);
                    //set grid height
                    postGridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
                    postGridPane.setPrefHeight(400);
                    postGridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
                    //GridPane.setMargin(anchorPane, new Insets(25));
                    GridPane.setMargin(anchorPane, new Insets(15, 5, 15, 180));
                }
            }
        } catch (Exception ex) {
            stageManager.showInfoMessage("INFO", "An error occurred while retrieving posts. Try again in a while.");
            System.err.println("[ERROR] fillGridPane@ControllerViewRegUserPostsPage.java raised an exception: " + ex.getMessage());
        }
    }

    public void onClickLogout(ActionEvent event) {
        stageManager.showWindow(FxmlView.WELCOMEPAGE);
        stageManager.closeStageButton(this.logoutButton);
    }

    public void onClickYourProfile(ActionEvent event) {
        stageManager.showWindow(FxmlView.USERPROFILEPAGE);
        stageManager.closeStageButton(this.logoutButton);
    }

    public void onClickAccountDetails(ActionEvent event) {
        stageManager.showWindow(FxmlView.SIGNUP);
    }

    public void onClickSearchUserButton(ActionEvent event) {
    }
}
