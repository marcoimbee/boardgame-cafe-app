package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.PostService;
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
    private Button postsFeedButton;
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
    private Button accountInfoButton;
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

    private final static int CACHED_POSTS_LIMIT = LIMIT * 10;

    private static int currentPage;
    private static List<Integer> visitedPages;
    private static boolean visualizedLastPost;      // Keeps track of whether the user has reached the las reachable page or not;

    @Autowired
    @Lazy
    public ControllerViewRegUserPostsPage(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        visitedPages = new ArrayList<>();

        this.postsFeedButton.setDisable(true);
        this.previousButton.setDisable(true);
        this.nextButton.setDisable(true);
        resetPageVars();

        currentlyShowing = PostsToFetch.POSTS_BY_FOLLOWED_USERS;            // Static var init

        // Choice box init
        whatPostsToShowChoiceBox.setValue(whatPostsToShowList.get(0));      // Default choice box string
        whatPostsToShowChoiceBox.setItems(whatPostsToShowList);                 // Setting the other options in choice box

        // Adding listeners to option selection: change indicator of what is displayed on the screen and retrieve results
        whatPostsToShowChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateCurrentlyShowing(newValue);
            onSelectChoiceBoxOption();
        });

        onSelectChoiceBoxOption();        // Show posts by followed users by default
    }

    private void updateCurrentlyShowing(String choiceBoxValue) {
        if (choiceBoxValue.equals(whatPostsToShowList.get(0))) {
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
        resetPageVars();
        List<PostModelMongo> retrievedPosts = getData();
        posts.addAll(retrievedPosts);            // Add new LIMIT posts (at most)
        fillGridPane();
        prevNextButtonsCheck(retrievedPosts.size());            // Initialize buttons
    }

    private void resetPageVars() {
        skipCounter = 0;
        posts.clear();
        currentPage = 0;
        visitedPages.clear();
        visitedPages.add(0);
        visualizedLastPost = false;
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
        postGridPane.getChildren().clear();

        List<PostModelMongo> retrievedPosts = new ArrayList<>();
        currentPage++;
        if (!visitedPages.contains(currentPage)) {
            // New posts need to be retrieved from the DB when visiting a page further from the furthest visited page
            skipCounter += SKIP;
            retrievedPosts = getData();        // Fetching new posts
            posts.addAll(retrievedPosts);            // Adding fetched posts to the post list
            visitedPages.add(currentPage);
        } else {
            skipCounter += SKIP;
        }

        prevNextButtonsCheck(retrievedPosts.size());

        fillGridPane();
        scrollSet.setVvalue(0);
    }

    @FXML
    void onClickPrevious() {
        postGridPane.getChildren().clear();

        if (currentPage > 0) {
            currentPage--;
            skipCounter -= SKIP;
        }

        prevNextButtonsCheck(0);
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    void prevNextButtonsCheck(int retrievedPostsSize) {
        previousButton.setDisable(currentPage == 0);

        boolean onFurthestPage = visitedPages.get(visitedPages.size() - 1) == currentPage;     // User is in the furthest page he visited

        if (onFurthestPage && retrievedPostsSize == 0 && !visualizedLastPost) {
            nextButton.setDisable(false);   // Keep enabled if we are on the furthest visited page up to now, we re-visited it, and we didn't reach the end
        } else {
            boolean morePostsAvailable = (retrievedPostsSize == SKIP);          // If we retrieved SKIP posts, likely there will be more available in the DB
            nextButton.setDisable(onFurthestPage && !morePostsAvailable);       // Disable if on last page and if retrieved less than SKIP posts
        }
    }

    private List<PostModelMongo> getData(){
        System.out.println("[INFO] New data has been fetched");
        return switch (currentlyShowing) {        // Decide what type of posts need to be fetched
            case POSTS_BY_FOLLOWED_USERS ->
                    postService.findPostsByFollowedUsers("g.sferr", LIMIT, skipCounter);
            case POSTS_LIKED_BY_FOLLOWED_USERS ->
                    postService.suggestPostLikedByFollowedUsers("g.sferr", LIMIT, skipCounter);
            case POSTS_COMMENTED_BY_FOLLOWED_USERS ->
                    postService.suggestPostCommentedByFollowedUsers("g.sferr", LIMIT, skipCounter);
        };
    }

    @FXML
    void fillGridPane() {
        columnGridPane = 0;
        rowGridPane = 1;

        // Logica per mostrare i dettagli del post usando StageManager
        PostListener postListener = (MouseEvent mouseEvent, PostModelMongo post) -> {
            // Logica per mostrare i dettagli del post usando StageManager
            stageManager.switchScene(FxmlView.USERPROFILEPAGE);
            stageManager.closeStageMouseEvent(mouseEvent);
        };

        postGridPane.getChildren().clear();         // Removing old posts

        try {
            if (posts.isEmpty()) {
                stageManager.showInfoMessage("INFO", "No posts to show!");
            } else {
                // Creating an item for each post: displaying posts in [skipCounter, skipCounter + LIMIT - 1]
                int startPost = skipCounter;
                int endPost = skipCounter + LIMIT - 1;
                if (endPost > posts.size()) {
                    endPost = posts.size() - 1;
                    visualizedLastPost = true;
                }

                System.out.println("[DEBUG] [startPost, endPost]: [" + startPost + ", " + endPost + "]");

                for (int i = startPost; i <= endPost; i++) {
                    PostModelMongo post = posts.get(i);

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

    public void onClickYourProfileButton(ActionEvent event) {
        stageManager.showWindow(FxmlView.USERPROFILEPAGE);
        stageManager.closeStageButton(this.logoutButton);
    }

    public void onClickAccountInfoButton(ActionEvent event) {
        stageManager.showWindow(FxmlView.SIGNUP);
    }

    public void onClickSearchUserButton(ActionEvent event) {
    }
}
