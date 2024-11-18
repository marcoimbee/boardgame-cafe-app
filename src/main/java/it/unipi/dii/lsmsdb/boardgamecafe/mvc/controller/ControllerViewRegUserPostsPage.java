package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.BoardgameDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.PostService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


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
    private Button searchButton;
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
    private Button accountInfoButton;
    @FXML
    private Button refreshButton;
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
    private BoardgameDBMongo boardgameDBMongo;
    @Autowired
    private ControllerObjectPost controllerObjectPost;
    @Autowired
    private ModelBean modelBean;
    private final StageManager stageManager;
    PostListener postListener;

    // Choice box variables
    ObservableList<String> whatPostsToShowList = FXCollections.observableArrayList(
            "Posts by followed users",
            "Posts liked by followed users",
            "Posts commented by followed users",
            "All posts"
    );

    //Post Variables
    private List<PostModelMongo> posts = new ArrayList<>();

    //Utils Variables
    private int columnGridPane = 0;
    private int rowGridPane = 1;
    private int skipCounter = 0;            // Ho many times the user clicked on the 'Next' button
    private final static int SKIP = 10;     // How many posts to skip each time
    private final static int LIMIT = 10;    // How many posts to show in each page

    private enum PostsToFetch {
        POSTS_BY_FOLLOWED_USERS,
        POSTS_LIKED_BY_FOLLOWED_USERS,
        POSTS_COMMENTED_BY_FOLLOWED_USERS,
        SEARCH_RESULTS,
        ALL_POSTS
    };
    private static PostsToFetch currentlyShowing;       // Global indicator of what type of post is being shown on the page

    private static int currentPage;
    private static List<Integer> visitedPages;
    private static boolean visualizedLastPost;      // Keeps track of whether the user has reached the las reachable page or not;

    // Search functionalities
    private List<String> boardgameTags;
    private static String selectedSearchTag;

    private static String currentUser;

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

        this.postsFeedButton.setDisable(true);
        this.previousButton.setDisable(true);
        this.nextButton.setDisable(true);
        this.newPostButton.setDisable(false);
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

        // Prefetch boardgame tags for the search function and init search functionalities variables
        searchResultsList.setVisible(false);
        long startTime = System.currentTimeMillis();
        if (modelBean.getBean(Constants.BOARDGAME_LIST) == null) {
            boardgameTags = boardgameDBMongo.getBoardgameTags();            // Fetching boardgame names as soon as the page opens
            modelBean.putBean(Constants.BOARDGAME_LIST, boardgameTags);     // Saving them in the Bean, so they'll be always available from now on in the whole app
        } else {
            boardgameTags = (List<String>) modelBean.getBean(Constants.BOARDGAME_LIST);     // Obtaining tags from the Bean, as thy had been put there before
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("[INFO] Fetched " + boardgameTags.size() + " boardgame tags in " + elapsedTime + " ms");
        selectedSearchTag = null;

        currentUser = ((UserModelMongo) modelBean.getBean(Constants.CURRENT_USER)).getUsername();

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
        String deletedPostId = (String) modelBean.getBean(Constants.DELETED_POST);          // TODO: fix this
        if (deletedPostId != null) {
            modelBean.putBean(Constants.DELETED_POST, null);            // Deleting bean for consistency
            posts.removeIf(post -> post.getId().equals(deletedPostId));
            fillGridPane();
        }

        // Update UI after potentially having added a comment to a post
        CommentModelMongo addedComment = (CommentModelMongo) modelBean.getBean(Constants.ADDED_COMMENT);
        if (addedComment != null) {
            modelBean.putBean(Constants.ADDED_COMMENT, null);
            fillGridPane();
        }

        // Update UI after potentially having deleted a comment form a post
        CommentModelMongo deletedComment = (CommentModelMongo) modelBean.getBean(Constants.DELETED_COMMENT);
        if (deletedComment != null) {
            modelBean.putBean(Constants.DELETED_COMMENT, null);
            for (PostModelMongo post : posts) {
                if (post.getId().equals(deletedComment.getPost())) {
                    post.deleteCommentInPost(deletedComment.getId());
                    break;
                }
            }
            fillGridPane();
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
        if (choiceBoxValue.equals(whatPostsToShowList.get(0))) {
            currentlyShowing = PostsToFetch.POSTS_BY_FOLLOWED_USERS;
        }

        if (choiceBoxValue.equals(whatPostsToShowList.get(1))) {
            currentlyShowing = PostsToFetch.POSTS_LIKED_BY_FOLLOWED_USERS;
        }

        if (choiceBoxValue.equals(whatPostsToShowList.get(2))) {
            currentlyShowing = PostsToFetch.POSTS_COMMENTED_BY_FOLLOWED_USERS;
        }

        if (choiceBoxValue.equals(whatPostsToShowList.get(3))) {
            currentlyShowing = PostsToFetch.ALL_POSTS;
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
    }

    public void onClickBoardgamesCollection() {
        stageManager.showWindow(FxmlView.REGUSERBOARDGAMES);
        stageManager.closeStageButton(this.boardgamesCollectionButton);
    }

    public void onClickSearch() {
        currentlyShowing = PostsToFetch.SEARCH_RESULTS;
        resetPageVars();
        List<PostModelMongo> retrievedPosts = fetchPosts(selectedSearchTag);
        posts.addAll(retrievedPosts);            // Add new LIMIT posts (at most)
        fillGridPane();
        prevNextButtonsCheck(retrievedPosts.size());            // Initialize buttons
    }

    public void onClickClearField() {
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
            retrievedPosts = fetchPosts(selectedSearchTag);        // Fetching new posts
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
            } else {
                boolean morePostsAvailable = (retrievedPostsSize == SKIP);          // If we retrieved SKIP posts, likely there will be more available in the DB
                nextButton.setDisable(onFurthestPage && !morePostsAvailable);       // Disable if on last page and if retrieved less than SKIP posts
            }
        }
    }

    private List<PostModelMongo> fetchPosts(String tag){
        this.newPostButton.setDisable(false);
        System.out.println("[INFO] New data has been fetched");
        return switch (currentlyShowing) {        // Decide what type of posts need to be fetched
            case POSTS_BY_FOLLOWED_USERS ->
                    postService.findPostsByFollowedUsers(currentUser, LIMIT, skipCounter);
            case POSTS_LIKED_BY_FOLLOWED_USERS ->
                    postService.suggestPostLikedByFollowedUsers(currentUser, LIMIT, skipCounter);
            case POSTS_COMMENTED_BY_FOLLOWED_USERS ->
                    postService.suggestPostCommentedByFollowedUsers(currentUser, LIMIT, skipCounter);
            case SEARCH_RESULTS ->
                    postService.findPostsByTag(tag, LIMIT, skipCounter);
            case ALL_POSTS ->
                    postDBMongo.findRecentPosts(LIMIT, skipCounter);
        };
    }

    private void loadViewMessageInfo() {
        Parent loadViewItem = stageManager.loadViewNode(FxmlView.INFOMSGPOSTS.getFxmlFile());
        AnchorPane noContentsYet = new AnchorPane();
        noContentsYet.getChildren().add(loadViewItem);

        postGridPane.getChildren().clear();
        resetPageVars();
        postGridPane.add(noContentsYet, 0, 1);
        GridPane.setMargin(noContentsYet, new Insets(123, 200, 200, 387));
    }

    private void updateUIAfterPostDeletion(String postId) {         // TODO: fix this
        posts.removeIf(post -> post.getId().equals(postId));
        if (posts.isEmpty()) {
            loadViewMessageInfo();
        } else {
            fillGridPane();
        }
    }

    @FXML
    void fillGridPane() {
        if (posts.size() == 1 || posts.isEmpty()) {        // Needed to correctly position a single element in the GridPane
            columnGridPane = 0;
            rowGridPane = 0;
        } else {
            columnGridPane = 0;
            rowGridPane++;
        }

        // Logica per mostrare i dettagli del post usando StageManager
        postListener = (MouseEvent mouseEvent, PostModelMongo post) -> {
            modelBean.putBean(Constants.SELECTED_POST, post);
            stageManager.showWindow(FxmlView.DETAILS_POST);
        };
        postGridPane.getChildren().clear();         // Removing old posts

        try {
            if (posts.isEmpty()) {
                loadViewMessageInfo();
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
        stageManager.showWindow(FxmlView.WELCOMEPAGE);
        stageManager.closeStageButton(this.logoutButton);
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
        stageManager.closeStageButton(this.searchButton);
    }

    public void onClickNewPostButton() {
        System.out.println("[INFO] Starting new post creation procedure");
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
            });

            // Displaying new post insertion box
            AnchorPane addPostBox = new AnchorPane();
            addPostBox.setId("newPostBox");
            addPostBox.getChildren().add(loadViewItem);

            if (!posts.isEmpty()){
                rowGridPane++;
                fillGridPane();
                postGridPane.add(addPostBox, 0, 1);
            } else {
                postGridPane.add(addPostBox, 0, rowGridPane+1);
            }

            GridPane.setMargin(addPostBox, new Insets(15, 5, 15, 180));

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
                currentUser,
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
        System.out.println("[DEBUG] filtered tag list size: " + tagsContainingSearchString.size());

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
    }
}
