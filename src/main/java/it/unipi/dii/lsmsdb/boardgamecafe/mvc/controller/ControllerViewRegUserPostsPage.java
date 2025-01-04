package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.CommentModel;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.BoardgameDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.PostService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;


@Component
public class ControllerViewRegUserPostsPage implements Initializable {
    @FXML
    private ListView searchResultsList;
    @FXML
    private Button boardgamesCollectionButton;
    @FXML
    private Button postsFeedButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button previousButton;
    @FXML
    private Button newPostButton;
    @FXML
    private Button yourProfileButton;
    @FXML
    private Button clearFieldButton;
    @FXML
    private Button searchUserButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button statisticsButton;
    @FXML
    private Button accountInfoButton;
    @FXML
    private Button refreshButton;
    @FXML
    private ChoiceBox<String> whatPostsToShowChoiceBox;
    @FXML
    private Tooltip tooltipAdminHint;

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
    private BoardgameDBMongo boardgameDBMongo;
    @Autowired
    private ControllerObjectPost controllerObjectPost;
    @Autowired
    private ModelBean modelBean;
    private final StageManager stageManager;
    PostListener postListener;
    private boolean shiftDownSingleObjectGridPane;

    // Choice box variables
    private ObservableList<String> whatPostsToShowList;

    private final List<String> availableUserQueries = Arrays.asList(
            "Posts by followed users",
            "Posts liked by followed users",
            "Posts commented by followed users",
            "All posts"
    );

    private final List<String> availableAdminQueries = Arrays.asList(
            "All posts",
            "ADMIN: top commented tagged posts"
    );

    //Post Variables
    private List<PostModelMongo> posts = new ArrayList<>();

    //Utils Variables
    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;            // How many times the user clicked on the 'Next' button
    private final static int SKIP = 10;     // How many posts to skip each time
    private final static int LIMIT = 10;    // How many posts to show in each page
    private final List<String> buttonLikeMessages = new ArrayList<>(Arrays.asList("Like", "Dislike"));

    public enum PostsToFetch {
        POSTS_BY_FOLLOWED_USERS,
        POSTS_LIKED_BY_FOLLOWED_USERS,
        POSTS_COMMENTED_BY_FOLLOWED_USERS,
        SEARCH_RESULTS,
        ALL_POSTS,
        ADMIN_TOP_COMMENTED_POST
    };
    public static PostsToFetch currentlyShowing;       // Global indicator of what type of post is being shown on the page

    private static int currentPage;
    private static List<Integer> visitedPages;
    private static boolean visualizedLastPost;      // Keeps track of whether the user has reached the las reachable page or not;

    // Search functionalities
    private List<String> boardgameTags;
    private static String selectedSearchTag;

    private static GenericUserModelMongo currentUser;

    private Consumer<String> deletedPostCallback;       // Used when a post author decides to delete a post via the delete button without opening its details page

    @Autowired
    @Lazy
    public ControllerViewRegUserPostsPage(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        visitedPages = new ArrayList<>();
        this.shiftDownSingleObjectGridPane = false;
        this.postsFeedButton.setDisable(true);
        this.previousButton.setDisable(true);
        this.nextButton.setDisable(true);
        resetPageVars();

        currentUser = (GenericUserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        if (!currentUser.get_class().equals("admin")) {
            currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
            this.newPostButton.setDisable(false);
            this.statisticsButton.setVisible(false);

            whatPostsToShowList = FXCollections.observableArrayList(availableUserQueries);
            whatPostsToShowChoiceBox.setValue(whatPostsToShowList.get(0));

            currentlyShowing = PostsToFetch.POSTS_BY_FOLLOWED_USERS;
        } else {
            currentUser = (AdminModelMongo) modelBean.getBean(Constants.CURRENT_USER);
            this.newPostButton.setDisable(true);
            this.yourProfileButton.setVisible(false);

            whatPostsToShowList = FXCollections.observableArrayList(availableAdminQueries);

            ControllerViewStatisticsPage.statisticsToShow showMostCommentedTaggedPost =     // Checking if the admin is visiting this page coming from the analytics panel
                    (ControllerViewStatisticsPage.statisticsToShow)modelBean.getBean(Constants.SELECTED_ANALYTICS);
            if (showMostCommentedTaggedPost != null) {                  // User is admin and he comes from the analytics panel
                modelBean.putBean(Constants.SELECTED_ANALYTICS, null);
                currentlyShowing = PostsToFetch.ADMIN_TOP_COMMENTED_POST;
                whatPostsToShowChoiceBox.setValue(whatPostsToShowList.get(whatPostsToShowList.size() - 1));
            } else {
                currentlyShowing = PostsToFetch.ALL_POSTS;
                whatPostsToShowChoiceBox.setValue(whatPostsToShowList.get(0));
            }
        }

        // Choice box init
        whatPostsToShowChoiceBox.setItems(whatPostsToShowList);

        // Adding listeners to option selection: change indicator of what is displayed on the screen and retrieve results
        whatPostsToShowChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateCurrentlyShowing(newValue);
            onSelectChoiceBoxOption();
        });
        onSelectChoiceBoxOption();        // Show posts by followed users by default

        // Prefetch boardgame tags for the search function and init search functionalities variables
        searchResultsList.setVisible(false);
        if (modelBean.getBean(Constants.BOARDGAME_LIST) == null) {
            boardgameTags = boardgameDBMongo.getBoardgameTags();            // Fetching boardgame names as soon as the page opens
            modelBean.putBean(Constants.BOARDGAME_LIST, boardgameTags);     // Saving them in the Bean, so they'll be always available from now on in the whole app
        } else
            boardgameTags = (List<String>) modelBean.getBean(Constants.BOARDGAME_LIST);     // Obtaining tags from the Bean, as thy had been put there before

        selectedSearchTag = null;

        // Post details listener - used to display post details once a post is clicked on
        postListener = (MouseEvent mouseEvent, PostModelMongo post) -> {
            searchResultsList.setVisible(false);
            modelBean.putBean(Constants.SELECTED_POST, post);
            Stage detailsStage = stageManager.showWindow(FxmlView.DETAILS_POST);
            detailsStage.setOnHidden(windowEvent -> {
                String lastPostId = ((PostModelMongo) modelBean.getBean(Constants.SELECTED_POST)).getId();
                AnchorPane eventAnchorPanePost = (AnchorPane) (mouseEvent.getSource());
                ((Label) eventAnchorPanePost.lookup("#counterLikesLabel")).setText(
                        String.valueOf(postDBNeo4j.findTotalLikesByPostID(lastPostId)));
                Button workingButton = ((Button) eventAnchorPanePost.lookup("#likeButton"));
                FontAwesomeIconView workingIcon = (FontAwesomeIconView) workingButton.getGraphic();
                boolean likeIsPresent = this.postService.hasLikedPost(currentUser.getUsername(), lastPostId);
                workingIcon.setIcon((likeIsPresent) ? FontAwesomeIcon.THUMBS_DOWN : FontAwesomeIcon.THUMBS_UP);
                workingButton.setText((this.buttonLikeMessages.get((likeIsPresent) ? 1 : 0)));
            });
        };

        // Page focus listener - needed to potentially update UI when coming back from a post detail window
        postGridPane.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.focusedProperty().addListener((observableFocus, wasFocused, isNowFocused) -> {
                    if (isNowFocused) {
                        // After gaining focus for a post details window closing, UI needs to be potentially updated
                        onRegainPageFocusAfterPostDetailsWindowClosing();
                    }
                });
            }
        });
    }

    private void onRegainPageFocusAfterPostDetailsWindowClosing() {
        // Update UI after potentially having deleted a post
        String deletedPostId = (String) modelBean.getBean(Constants.DELETED_POST);
        if (deletedPostId != null) {
            modelBean.putBean(Constants.DELETED_POST, null);            // Deleting bean for consistency
            posts.removeIf(post -> post.getId().equals(deletedPostId));
            onSelectChoiceBoxOption();
        }

        // Update UI after potentially having added a comment to a post
        CommentModel addedComment = (CommentModel) modelBean.getBean(Constants.ADDED_COMMENT);
        if (addedComment != null) {
            modelBean.putBean(Constants.ADDED_COMMENT, null);
            fillGridPane();
        }

        // Update UI after potentially having deleted a comment form a post
        CommentModel deletedComment = (CommentModel) modelBean.getBean(Constants.DELETED_COMMENT);
        if (deletedComment != null) {
            modelBean.putBean(Constants.DELETED_COMMENT, null);
            for (PostModelMongo post : posts) {
                if (post.getId().equals(deletedComment.getPost())) {
                    post.deleteCommentInPost(deletedComment.getId());
                    break;
                }
            }
            onSelectChoiceBoxOption();
        }


        // Update UI after potentially having updated a post
        PostModelMongo updatedPost = (PostModelMongo) modelBean.getBean(Constants.UPDATED_POST);
        if (updatedPost != null) {
            modelBean.putBean(Constants.UPDATED_POST, null);
            posts.replaceAll(post -> post.getId().equals(updatedPost.getId()) ? updatedPost : post);
            fillGridPane();
        }
    }

    private void updateCurrentlyShowing(String choiceBoxValue) {
        if (!currentUser.get_class().equals("admin")) {
            // Setting up regular user queries correspondence
            if (choiceBoxValue.equals(whatPostsToShowList.get(0)))      currentlyShowing = PostsToFetch.POSTS_BY_FOLLOWED_USERS;
            else if (choiceBoxValue.equals(whatPostsToShowList.get(1))) currentlyShowing = PostsToFetch.POSTS_LIKED_BY_FOLLOWED_USERS;
            else if (choiceBoxValue.equals(whatPostsToShowList.get(2))) currentlyShowing = PostsToFetch.POSTS_COMMENTED_BY_FOLLOWED_USERS;
            else if (choiceBoxValue.equals(whatPostsToShowList.get(3))) currentlyShowing = PostsToFetch.ALL_POSTS;
        } else {
            // Setting up admin queries correspondence
            if (choiceBoxValue.equals(whatPostsToShowList.get(0)))      currentlyShowing = PostsToFetch.ALL_POSTS;
            else if (choiceBoxValue.equals(whatPostsToShowList.get(1))) currentlyShowing = PostsToFetch.ADMIN_TOP_COMMENTED_POST;
        }
    }

    public void onSelectChoiceBoxOption() {
        resetPageVars();
        selectedSearchTag = null;
        List<PostModelMongo> retrievedPosts = fetchPosts(null);
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
        scrollSet.setVvalue(0);
        this.shiftDownSingleObjectGridPane = false;
    }

    public void onClickStatisticsButton() {
        stageManager.switchScene(FxmlView.STATISTICS);
    }

    public void onClickBoardgamesCollection() {
        stageManager.showWindow(FxmlView.REGUSERBOARDGAMES);
        stageManager.closeStageButton(this.boardgamesCollectionButton);
    }

    public void startSearch() {
        searchResultsList.setVisible(false);
        resetPageVars();
        List<PostModelMongo> retrievedPosts;
        if (currentlyShowing != PostsToFetch.ADMIN_TOP_COMMENTED_POST) {
            currentlyShowing = PostsToFetch.SEARCH_RESULTS;
            retrievedPosts = fetchPosts(selectedSearchTag);
        }
        else
        {
            retrievedPosts = this.postDBMongo.findTopCommentedTaggedPosts(this.textFieldSearch.getText(), LIMIT, skipCounter);
            retrievedPosts.forEach(postModelMongo -> System.out.println("SIze: " + postModelMongo.getComments().size()));
        }
        posts.addAll(retrievedPosts);            // Add new LIMIT posts (at most)
        fillGridPane();
        prevNextButtonsCheck(retrievedPosts.size());            // Initialize buttons
    }

    public void onClickClearField() {
        searchResultsList.setVisible(false);
        this.textFieldSearch.clear();           // When clearing the search box, we reset the view to make it show the default shown posts
        currentlyShowing = PostsToFetch.POSTS_BY_FOLLOWED_USERS;
        whatPostsToShowChoiceBox.setValue(whatPostsToShowList.get(0));
        onSelectChoiceBoxOption();
    }

    @FXML
    void onClickNext() {
        postGridPane.getChildren().clear();

        List<PostModelMongo> retrievedPosts = new ArrayList<>();
        currentPage++;
        if (!visitedPages.contains(currentPage)) {
            // New posts need to be retrieved from the DB when visiting a page further from the furthest visited page
            skipCounter += SKIP;
            if (currentlyShowing != PostsToFetch.ADMIN_TOP_COMMENTED_POST)
                retrievedPosts = fetchPosts(selectedSearchTag);        // Fetching new posts
            else
                retrievedPosts = this.postDBMongo.findTopCommentedTaggedPosts(this.textFieldSearch.getText(), LIMIT, skipCounter);
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

        if (posts.isEmpty()) {
            nextButton.setDisable(true);
        } else {
            boolean onFurthestPage = visitedPages.get(visitedPages.size() - 1) == currentPage;     // User is in the furthest page he visited

            if (onFurthestPage && retrievedPostsSize == 0 && !visualizedLastPost) {
                nextButton.setDisable(false);   // Keep enabled if we are on the furthest visited page up to now, we re-visited it, and we didn't reach the end
            } else
            {
                    boolean morePostsAvailable = (retrievedPostsSize == SKIP);          // If we retrieved SKIP posts, likely there will be more available in the DB
                    nextButton.setDisable(onFurthestPage && !morePostsAvailable);       // Disable if on last page and if retrieved less than SKIP posts
            }
        }
    }

    private List<PostModelMongo> fetchPosts(String tag){
        if (currentUser.get_class().equals("admin"))
            this.newPostButton.setDisable(true);
        else
            this.newPostButton.setDisable(false);
        System.out.println("[INFO] New data has been fetched");
        List<PostModelMongo> postListToReturn = new ArrayList<>();
        if (currentlyShowing != PostsToFetch.ADMIN_TOP_COMMENTED_POST)
        {
            this.tooltipAdminHint.hide();
            this.textFieldSearch.setTooltip(null);
        }
        switch (currentlyShowing) {        // Decide what type of posts need to be fetched
            case POSTS_BY_FOLLOWED_USERS ->
                    postListToReturn = postService.findPostsByFollowedUsers(currentUser.getUsername(), LIMIT, skipCounter);
            case POSTS_LIKED_BY_FOLLOWED_USERS ->
                    postListToReturn = postService.suggestPostLikedByFollowedUsers(currentUser.getUsername(), LIMIT, skipCounter);
            case POSTS_COMMENTED_BY_FOLLOWED_USERS ->
                    postListToReturn = postService.suggestPostCommentedByFollowedUsers(currentUser.getUsername(), LIMIT, skipCounter);
            case SEARCH_RESULTS ->
                    postListToReturn = postService.findPostsByTag(tag, LIMIT, skipCounter);
            case ALL_POSTS ->
                    postListToReturn = postDBMongo.findRecentPosts(LIMIT, skipCounter);
            case ADMIN_TOP_COMMENTED_POST -> {
                this.textFieldSearch.setTooltip(this.tooltipAdminHint);
                this.textFieldSearch.requestFocus();
                Scene thisScene = this.textFieldSearch.getScene();
                if (thisScene == null)
                {
                    Platform.runLater(() -> {
                        this.tooltipAdminHint.show(this.textFieldSearch.getScene().getWindow(),
                                this.textFieldSearch.localToScreen(this.textFieldSearch.getBoundsInLocal()).getMinX(),
                                this.textFieldSearch.localToScreen(this.textFieldSearch.getBoundsInLocal()).getMinY() - 30);
                    });
                }
                else
                {
                    this.tooltipAdminHint.show(this.textFieldSearch.getScene().getWindow(),
                            this.textFieldSearch.localToScreen(this.textFieldSearch.getBoundsInLocal()).getMinX(),
                            this.textFieldSearch.localToScreen(this.textFieldSearch.getBoundsInLocal()).getMinY() - 30);
                }
            }
        };
        return postListToReturn;
    }


    private void loadViewMessageInfo() {
        Parent loadViewItem = stageManager.loadViewNode(FxmlView.INFOMSGPOSTS.getFxmlFile());
        AnchorPane noContentsYet = new AnchorPane();
        noContentsYet.getChildren().add(loadViewItem);

        postGridPane.add(noContentsYet, 0, 1);
        GridPane.setMargin(noContentsYet, new Insets(123, 200, 200, 387));
    }

    private void updateUIAfterPostDeletion(String postId) {
        posts.removeIf(post -> post.getId().equals(postId));
        if (posts.isEmpty()) {
            loadViewMessageInfo();
        } else {
            onSelectChoiceBoxOption();          // Reloading page
        }
    }

    @FXML
    void fillGridPane() {
        searchResultsList.setVisible(false);
        columnGridPane = 0;       // Needed to correctly position a single element in the gridpane
        if (posts.size() == 1) {
            if (shiftDownSingleObjectGridPane)
                rowGridPane = 2;
            else
                rowGridPane = 0;
        } else {
            rowGridPane++;
        }

        postGridPane.getChildren().clear();         // Removing old posts

        try {
            if (posts.isEmpty()) {
                loadViewMessageInfo();
            } else {
                // Creating an item for each post: displaying posts in [skipCounter, skipCounter + LIMIT - 1]
                int startPost = skipCounter;
                int endPost = skipCounter + LIMIT - 1;
                if (endPost >= posts.size()) {
                    endPost = posts.size() - 1;
                    visualizedLastPost = true;
                }

                //System.out.println("[DEBUG] [post.size(), startPost, endPost]: [" + posts.size() + ", " + startPost + ", " + endPost + "]");

                for (int i = startPost; i <= endPost; i++) {
                    PostModelMongo post = posts.get(i);
                    AnchorPane postNode = createPostViewNode(post);
                    addPostToGridPane(postNode);
                }
            }
        } catch (Exception ex) {
            stageManager.showInfoMessage("INFO", "An error occurred while retrieving posts. Please try again in a while.");
            System.err.println("[ERROR] fillGridPane@ControllerViewRegUserPostsPage.java raised an exception: " + ex.getMessage());
        }
    }

    private void addPostToGridPane(AnchorPane postNode) {
        if (columnGridPane == 1) {
            columnGridPane = 0;
            rowGridPane++;
        }

        postGridPane.add(postNode, columnGridPane++, rowGridPane); //(child,column,row)

        postGridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
        postGridPane.setPrefWidth(500);
        postGridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);

        postGridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
        postGridPane.setPrefHeight(400);
        postGridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);

        GridPane.setMargin(postNode, new Insets(15, 5, 15, 190));
    }

    private AnchorPane createPostViewNode(PostModelMongo post) {
        Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTPOST.getFxmlFile());
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().add(loadViewItem);

        // Setting up what should be called upon post deletion using the delete post button, without opening the post's details
        deletedPostCallback = this::updateUIAfterPostDeletion;

        controllerObjectPost.setData(post, postListener, deletedPostCallback);

        anchorPane.setOnMouseClicked(event -> {
            this.postListener.onClickPostListener(event, post);});

        return anchorPane;
    }

    public void onClickLogout() {
        modelBean.putBean(Constants.CURRENT_USER, null);
        stageManager.switchScene(FxmlView.WELCOMEPAGE);
    }

    public void onClickYourProfileButton() {
        stageManager.showWindow(FxmlView.USERPROFILEPAGE);
        stageManager.closeStageButton(this.yourProfileButton);
    }

    public void onClickAccountInfoButton() {
        stageManager.showWindow(FxmlView.ACCOUNTINFOPAGE);
        stageManager.closeStageButton(this.accountInfoButton);
    }

    public void onClickSearchUserButton() {
        stageManager.showWindow(FxmlView.SEARCHUSER);
        stageManager.closeStageButton(this.searchUserButton);
    }

    public void onClickNewPostButton() {
        System.out.println("[INFO] Starting new post creation procedure");
        scrollSet.setVvalue(0);
        try {
            this.newPostButton.setDisable(true);
            Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTCREATEPOST.getFxmlFile());       // Load modifiable post's FXML

            TextField tagBoardgameTextArea = (TextField) loadViewItem.lookup("#tagBoardgameText");  // Get control over FXML input fields
            TextField titleTextArea = (TextField) loadViewItem.lookup("#titleTextLabel");
            TextField postTextArea = (TextField) loadViewItem.lookup("#bodyTextLabel");
            Button submitPostButton = (Button) loadViewItem.lookup("#submitButton");
            Button cancelPostButton = (Button) loadViewItem.lookup("#cancelButton");

            tagBoardgameTextArea.setPromptText("What boardgame will the post be about? (Optional)");
            titleTextArea.setPromptText("Write The Post Title Here...");
            postTextArea.setPromptText("Write Your Post Here...");

            // AddButton behavior
            submitPostButton.setOnAction(e -> {
                String tag = tagBoardgameTextArea.getText();           // Getting post data
                String title = titleTextArea.getText();
                String body = postTextArea.getText();

                addNewPost(tag, title, body);                   // Adding the post
            });

            // CancelButton behavior
            cancelPostButton.setOnAction(e -> {
                String latestTag = tagBoardgameTextArea.getText();
                String latestTitle = titleTextArea.getText();
                String latestBody = postTextArea.getText();
                if (!latestTitle.isEmpty() || !latestBody.isEmpty()) {
                    boolean discardPost = stageManager.showDiscardPostInfoMessage();          // Show info message
                    if (discardPost) {              // User chose to discard post, remove post creation panel element
                        removePostInsertionPanel();
                    }
                } else {
                    removePostInsertionPanel();   // The post was empty, can remove the panel without warning
                }
                newPostButton.setDisable(false);
                whatPostsToShowChoiceBox.setDisable(false);
                fillGridPane();
            });

            // Displaying new post insertion box
            AnchorPane addPostBox = new AnchorPane();
            addPostBox.setId("newPostBox");
            addPostBox.getChildren().add(loadViewItem);

            if (!posts.isEmpty()){
                if (posts.size() == 1)
                    shiftDownSingleObjectGridPane = true;
                else
                    shiftDownSingleObjectGridPane = false;
                fillGridPane();
                postGridPane.add(addPostBox, 0, 1);
            } else {
                postGridPane.getChildren().clear();
                postGridPane.add(addPostBox, 0, 1);
            }

            GridPane.setMargin(addPostBox, new Insets(15, 5, 15, 190));

        } catch (Exception e) {
            stageManager.showInfoMessage("INFO", "An error occurred while creating the post. Try again in a while.");
            System.err.println("[ERROR] onClickNewPostButton@ControllerViewRegUserPostsPage.java raised an exception: " + e.getMessage());
        }
    }

    private void removePostInsertionPanel() {
        postGridPane.getChildren().removeIf(elem -> {
            String elemId = elem.getId();
            if (elemId != null) {
                return elemId.equals("newPostBox");
            }
            return false;
        });
    }

    private void addNewPost(String tag, String title, String body) {
        if (body.isEmpty()) {
            stageManager.showInfoMessage("Error", "Post Cannot Be Empty.");
            return;
        }
        if (title.isEmpty()){
            stageManager.showInfoMessage("Error", "Title Cannot Be Empty.");
            return;
        }

        if (!tag.isEmpty() && !boardgameTags.contains(tag)) {     // Checking boardgame validity
            stageManager.showInfoMessage("Error", "'" + tag + "' is not a valid boardgame name.");
            return;
        }

        PostModelMongo newPost = new PostModelMongo(    // Create a new PostModelMongo and save it in the DB
                currentUser.getUsername(),
                title,
                body,
                tag,
                new Date()
        );

        PostModelMongo savedPost = postService.insertPost(newPost);     // MongoDB + Neo4J insertion

        if (savedPost != null) {
            System.out.println("[INFO] New post added");
            handleSuccessfulPostAddition(savedPost);
            this.newPostButton.setDisable(false);
        } else {
            System.out.println("[INFO] An error occurred while adding a new post");
            stageManager.showInfoMessage("INFO", "Failed to add post. Please try again in a while.");
            fillGridPane();             // Restoring GridPane if anything went wrong
        }
    }

    private void handleSuccessfulPostAddition(PostModelMongo newlyInsertedPost) {
        if (currentlyShowing == PostsToFetch.ALL_POSTS) {
            posts.remove(posts.size() - 1);         // Alter posts collection but keep it compliant to posts displaying rules
            posts.add(0, newlyInsertedPost);
            fillGridPane();
            prevNextButtonsCheck(posts.size() <= LIMIT ? posts.size() : LIMIT);
        } else {
            stageManager.showInfoMessage("Success", "Your post has been added successfully! You're being redirected to the 'All posts' page.");
            currentlyShowing = PostsToFetch.ALL_POSTS;          // get back to ALL_POSTS page, show the new post first
            whatPostsToShowChoiceBox.setValue(whatPostsToShowList.get(whatPostsToShowList.size() - 1));       // Setting string inside choice box
            onSelectChoiceBoxOption();      // What needs to be done is the same as what's done here
        }

        refreshButton.setDisable(false);        // Re-enabling the button
        whatPostsToShowChoiceBox.setDisable(false);
    }

    public void onClickRefreshButton(){
        onSelectChoiceBoxOption();  // The same actions that are performed when clicking a choice box option have to be performed
    }

    public void onKeyTypedSearchBar() {
        String searchString = textFieldSearch.getText();

        if (searchString.isEmpty()) {
            searchResultsList.setVisible(false);
        } else {
            searchResultsList.setVisible(true);
        }
        ObservableList<String> tagsContainingSearchString = FXCollections.observableArrayList(
                ((List<String>)modelBean.getBean(Constants.BOARDGAME_LIST)).stream()
                .filter(tag -> tag.toLowerCase().contains(searchString.toLowerCase())).toList());
        //System.out.println("[DEBUG] filtered tag list size: " + tagsContainingSearchString.size());

        searchResultsList.setItems(tagsContainingSearchString);
        int LIST_ROW_HEIGHT = 24;
        if (tagsContainingSearchString.size() > 10) {
            searchResultsList.setPrefHeight(10 * LIST_ROW_HEIGHT + 2);
        } else if (tagsContainingSearchString.isEmpty()){
            searchResultsList.setVisible(false);
        } else {
            searchResultsList.setPrefHeight(tagsContainingSearchString.size() * LIST_ROW_HEIGHT + 2);
        }

        // Highlight matching search substring in result strings
        searchResultsList.setCellFactory(boardgameResult -> new ListCell<String>() {
            @Override
            protected void updateItem(String result, boolean empty) {
                super.updateItem(result, empty);

                if (empty || result == null) {
                    setGraphic(null);
                    return;
                    }

                TextFlow textFlow = new TextFlow();
                int startIdx = result.toLowerCase().indexOf(searchString.toLowerCase());

                if (startIdx >= 0 && !searchString.isEmpty()) {
                    Text beforeMatch = new Text(result.substring(0, startIdx));
                    beforeMatch.setFill(Color.BLACK);

                    Text matchedPart = new Text(result.substring(startIdx, startIdx + searchString.length()));
                    matchedPart.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 14));

                    Text afterMatch = new Text(result.substring(startIdx + searchString.length()));
                    afterMatch.setFill(Color.BLACK);

                    textFlow.getChildren().addAll(beforeMatch, matchedPart, afterMatch);
                }

                setGraphic(textFlow);
            }
        });
    }

    @FXML
    public void onMouseClickedListView() {
        searchResultsList.setVisible(false);
        selectedSearchTag = searchResultsList.getSelectionModel().getSelectedItem().toString();
        textFieldSearch.setText(selectedSearchTag);
        startSearch();
    }

    public void onClickAnchorPane()
    {
        this.searchResultsList.setVisible(false);
    }

}
