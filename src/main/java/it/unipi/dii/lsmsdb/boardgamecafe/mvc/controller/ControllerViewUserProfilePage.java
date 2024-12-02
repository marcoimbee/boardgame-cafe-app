package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.ReviewDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;


@Component
public class ControllerViewUserProfilePage implements Initializable{

    public enum ContentType {
        POSTS, REVIEWS;
    }

    @FXML
    private Button yourProfileButton;
    @FXML
    private Button followButton;
    @FXML
    private Button postsFeedButton;
    @FXML
    private Button accountInfoButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button previousButton;
    @FXML
    private Button searchUserButton;
    @FXML
    private Button boardgamesCollectionButton;
    @FXML
    private Button yourPostsButton;
    @FXML
    private Button yourReviewsButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button statisticsButton;

    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label nationalityLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label followerLabel;
    @FXML
    private Label followingLabel;
    @FXML
    private Label counterPostsLabel;
    @FXML
    private Label counterReviewsLabel;

    @FXML
    private ImageView profileImage;
    @FXML
    private GridPane gridPane;
    @FXML
    private ScrollPane scrollSet;

    @Autowired
    private PostDBMongo postDBMongo;
    @Autowired
    private ReviewDBMongo reviewMongoOp;
    @Autowired
    private UserDBNeo4j userDBNeo;
    @Autowired
    private ControllerObjectPost controllerObjectPost;
    @Autowired
    private ControllerObjectReview controllerObjectReview;
    @Autowired
    private ModelBean modelBean;

    private final StageManager stageManager;

    private List<PostModelMongo> postsUser = new ArrayList<>();
    private List<ReviewModelMongo> reviewsUser = new ArrayList<>();
    private UserModelMongo openUserProfile;     // The user whose profile is open right now

    private PostListener postListener;

    private int totalFollowerUsers;
    private int totalFollowingUsers;
    private int totalPosts;
    private int columnGridPane = 0;
    private int rowGridPane = 1;
    private int skipCounter = 0;
    private final static int SKIP = 10; //how many posts to skip per time
    private final static int LIMIT = 10; //how many posts to show for each page
    private ContentType selectedContentType; // variabile di stato per tipo di contenuto
    private static List<String> currentUserFollowedList;
    private static UserModelMongo currentUser;
    private static UserModelMongo selectedUser;
    private Consumer<String> deletedPostCallback;

    @Autowired
    @Lazy
    public ControllerViewUserProfilePage(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[INFO] Loaded ControllerViewUserProfilePage");
        this.yourProfileButton.setDisable(true);
        this.yourPostsButton.setDisable(true);
        this.previousButton.setDisable(true);
        this.nextButton.setDisable(true);
        this.selectedContentType = ContentType.POSTS;
        this.resetPage();

        selectedUser = (UserModelMongo) modelBean.getBean(Constants.SELECTED_USER);
        currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);

        if (selectedUser != null){          // User is looking at another user's profile
            checkSelectedUser();
        } else {
            openUserProfile = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);           // User is seeing his profile page
            this.followButton.setDisable(true);
        }

        List<PostModelMongo> fullPosts = postDBMongo.findByUsername(openUserProfile.getUsername());
        totalPosts = fullPosts.size();
        totalFollowerUsers = userDBNeo.getCountFollowers(openUserProfile.getUsername());
        totalFollowingUsers = userDBNeo.getCountFollowing(openUserProfile.getUsername());

        postsUser.addAll(getPosts(openUserProfile.getUsername()));
        if (this.postsUser.isEmpty()) {
            loadViewMessageInfo();
        }
        this.firstNameLabel.setText(openUserProfile.getName());
        this.lastNameLabel.setText(openUserProfile.getSurname());
        this.nationalityLabel.setText(openUserProfile.getNationality());
        this.usernameLabel.setText(openUserProfile.getUsername());
        this.followerLabel.setText(String.valueOf(totalFollowerUsers));
        this.followingLabel.setText(String.valueOf(totalFollowingUsers));
        this.counterPostsLabel.setText(String.valueOf(totalPosts));
        this.counterReviewsLabel.setText(String.valueOf(openUserProfile.getReviews().size()));
        Image image = new Image(Objects.requireNonNull(getClass().
                                getResource("/user.png")).toExternalForm());
        this.profileImage.setImage(image);

        fillGridPane(postsUser);

        // If we enter here we are at the app start - it's the first time we see this page, so the constant is certainly empty
        if (modelBean.getBean(Constants.CURRENT_USER_FOLLOWED_LIST) == null) {
            currentUserFollowedList = userDBNeo.getFollowedUsernames(currentUser.getUsername());
            modelBean.putBean(Constants.CURRENT_USER_FOLLOWED_LIST, currentUserFollowedList);
            System.out.println("[INFO] Fetched " + currentUserFollowedList.size() + " followed users' usernames");
        } else {            // If we enter here for sure we're re-visiting the current user profile page, or we're visiting another user's profile page
            currentUserFollowedList = (List<String>) modelBean.getBean(Constants.CURRENT_USER_FOLLOWED_LIST);

            // Setting follow button text depending on if the user already follows or not the user whose profile is being visited
            if (selectedUser != null && currentUserFollowedList.contains(selectedUser.getUsername())) {
                this.followButton.setText(" Unfollow");
            } else {
                this.followButton.setText(" Follow");
            }
        }

        // Post details listener - used to display post details once a post is clicked on
        postListener = (MouseEvent mouseEvent, PostModelMongo post) -> {
            modelBean.putBean(Constants.SELECTED_POST, post);
            stageManager.showWindow(FxmlView.DETAILS_POST);
        };

        // Page focus listener - needed to potentially update UI when coming back from a post detail window
        gridPane.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.focusedProperty().addListener((observableFocus, wasFocused, isNowFocused) -> {
                    if (isNowFocused) {
                        // After gaining focus for a post details window closing or a review update window closing,
                        // UI needs to be potentially updated
                        if (selectedContentType == ContentType.POSTS) {
                            onRegainPageFocusAfterPostDetailsWindowClosing();
                        } else {
                            onRegainPageFocusAfterReviewWindowClosing();
                        }
                    }
                });
            }
        });
    }

    private void onRegainPageFocusAfterReviewWindowClosing() {
        // Potentially update UI after review deletion

        // Potentially update UI after review editing
    }

    private void onRegainPageFocusAfterPostDetailsWindowClosing() {
        // Potentially update UI after post deletion
        String deletedPostId = (String) modelBean.getBean(Constants.DELETED_POST);
        if (deletedPostId != null) {
            modelBean.putBean(Constants.DELETED_POST, null);            // Deleting bean for consistency
            resetPage();
            List<PostModelMongo> retrievedPosts = getPosts(currentUser.getUsername());
            this.counterPostsLabel.setText(String.valueOf(retrievedPosts.size()));
            if (retrievedPosts.isEmpty()) {
                loadViewMessageInfo();
            } else {
                postsUser.addAll(retrievedPosts);
                fillGridPane(postsUser);
                prevNextButtonsCheck(postsUser);
            }
        }

        // Potentially update UI after post editing
        PostModelMongo updatedPost = (PostModelMongo) modelBean.getBean(Constants.UPDATED_POST);
        if (updatedPost != null) {
            postsUser.replaceAll(post -> post.getId().equals(updatedPost.getId()) ? updatedPost : post);
            fillGridPane(postsUser);
        }

        // Potentially update UI after comment deletion under a post
        CommentModelMongo deletedComment = (CommentModelMongo) modelBean.getBean(Constants.DELETED_COMMENT);
        if (deletedComment != null) {
            modelBean.putBean(Constants.DELETED_COMMENT, null);
            resetPage();
            List<PostModelMongo> retrievedPosts = getPosts(currentUser.getUsername());
            this.counterPostsLabel.setText(String.valueOf(retrievedPosts.size()));
            if (retrievedPosts.isEmpty()) {
                loadViewMessageInfo();
            } else {
                postsUser.addAll(retrievedPosts);
                fillGridPane(postsUser);
                prevNextButtonsCheck(postsUser);
            }
        }
    }


    public void onClickBoardgamesButton() {
        stageManager.showWindow(FxmlView.REGUSERBOARDGAMES);
        stageManager.closeStageButton(this.boardgamesCollectionButton);
    }

    public void onClickPostsFeedButton() {
        stageManager.showWindow(FxmlView.REGUSERPOSTS);
        stageManager.closeStageButton(this.boardgamesCollectionButton);
    }

    public void onClickSearchUserButton() {
        stageManager.showWindow(FxmlView.SEARCHUSER);
        stageManager.closeStageButton(this.searchUserButton);
    }

    public void onClickFollowButton() {
        try {
            boolean following = currentUserFollowedList.contains(selectedUser.getUsername());  // Tells if the current use is following or not the user he's looking at
            if (!following) {
                // TODO: turn this into a UserService method

                // Add new Neo4J relationship
                userDBNeo.followUser(currentUser.getUsername(), selectedUser.getUsername());

                // Adding username to collection and updating model bean
                currentUserFollowedList.add(selectedUser.getUsername());
                modelBean.putBean(Constants.CURRENT_USER_FOLLOWED_LIST, currentUserFollowedList);

                // Increment user followers counter in graphics
                totalFollowerUsers++;
                this.followerLabel.setText(String.valueOf(totalFollowerUsers));         // No need to read again from DB

                // Update follow/unfollow button graphics
                this.followButton.setText(" Unfollow");

                System.out.println("[INFO] " + currentUser.getUsername() + " followed " + selectedUser.getUsername());
            } else {
                // TODO: turn this into a UserService method

                // Remove Neo4J relationship
                userDBNeo.unfollowUser(currentUser.getUsername(), selectedUser.getUsername());

                // Remove username from collection and updating model bean
                currentUserFollowedList.remove(selectedUser.getUsername());
                modelBean.putBean(Constants.CURRENT_USER_FOLLOWED_LIST, currentUserFollowedList);

                // Decrement user followers counter in graphics
                totalFollowerUsers--;
                this.followerLabel.setText(String.valueOf(totalFollowerUsers));

                // Update follow/unfollow button graphics
                this.followButton.setText(" Follow");

                System.out.println("[INFO] " + currentUser.getUsername() + " stopped following " + selectedUser.getUsername());
            }
        } catch (Exception e) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Try again in a while.");
            System.err.println("[ERROR] onClickFollowButton@ControllerViewUserProfilePage raised an exception: " + e.getMessage());
        }
    }

    public void onClickPostsButton() {
        this.yourReviewsButton.setDisable(false);
        this.yourPostsButton.setDisable(true);
        this.selectedContentType = ContentType.POSTS;
        loadContent();
        scrollSet.setVvalue(0);
    }

    public void onClickReviewsButton() {
        this.yourReviewsButton.setDisable(true);
        this.yourPostsButton.setDisable(false);
        this.selectedContentType = ContentType.REVIEWS;
        loadContent();
        scrollSet.setVvalue(0);
    }

    private void loadContent() {
        // Loading content depending on selectedContentType
        if (selectedContentType.equals(ContentType.POSTS)) {
            List<?> items = getPosts(this.usernameLabel.getText());
            if (items.isEmpty()) {
                loadViewMessageInfo();
            } else {
                gridPane.getChildren().clear();
                fillGridPane(items);
            }
        } else if (selectedContentType.equals(ContentType.REVIEWS)){
            List<?> items = getReviews(this.usernameLabel.getText());
            if (items.isEmpty()) {
                loadViewMessageInfo();
            } else {
                gridPane.getChildren().clear();
                fillGridPane(items);
            }
        }
    }

    @FXML
    public void onClickNext() {
        if (selectedContentType.equals(ContentType.POSTS)) {
            gridPane.getChildren().clear();
            postsUser.clear();
            skipCounter += SKIP;
            List<?> items = getPosts(this.usernameLabel.getText());
            fillGridPane(items);
        } else if (selectedContentType.equals(ContentType.REVIEWS)) {
            gridPane.getChildren().clear();
            reviewsUser.clear();
            skipCounter += SKIP;
            List<?> items = getReviews(this.usernameLabel.getText());
            fillGridPane(items);
        }
        scrollSet.setVvalue(0);
    }

    @FXML
    public void onClickPrevious() {
        if (selectedContentType.equals(ContentType.POSTS)) {
            gridPane.getChildren().clear();
            postsUser.clear();
            skipCounter -= SKIP;
            List<?> items = getPosts(this.usernameLabel.getText());
            fillGridPane(items);
        } else if (selectedContentType.equals(ContentType.REVIEWS)) {
            gridPane.getChildren().clear();
            reviewsUser.clear();
            skipCounter -= SKIP;
            List<?> items = getReviews(this.usernameLabel.getText());
            fillGridPane(items);
        }
        scrollSet.setVvalue(0);
    }

    private void resetPage() {
        gridPane.getChildren().clear();
        if (selectedContentType.equals(ContentType.POSTS)) {
            postsUser.clear();
        } else if (selectedContentType.equals(ContentType.REVIEWS)) {
            reviewsUser.clear();
        }
        skipCounter = 0;
        previousButton.setDisable(true);
        nextButton.setDisable(true);
        scrollSet.setVvalue(0);
    }

    private void prevNextButtonsCheck(List<?> contentList) {
        if (contentList.size() > 0) {
            if (contentList.size() < LIMIT) {
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

    private List<PostModelMongo> getPosts(String username) {
        List<PostModelMongo> posts = postDBMongo.
                findRecentPostsByUsername(username, LIMIT, skipCounter);
        prevNextButtonsCheck(posts);
        return posts;
    }

    private List<ReviewModelMongo> getReviews(String username) {
        List<ReviewModelMongo> reviews = reviewMongoOp.
                findRecentReviewsByUsername(username, LIMIT, skipCounter);
        prevNextButtonsCheck(reviews);
        return reviews;
    }

    private void loadViewMessageInfo(){
        Parent whatToLoad;
        if (selectedContentType == ContentType.POSTS) {
            whatToLoad = stageManager.loadViewNode(FxmlView.INFOMSGPOSTS.getFxmlFile());
        } else {
            whatToLoad = stageManager.loadViewNode(FxmlView.INFOMSGREVIEWS.getFxmlFile());
        }

        AnchorPane noContentsYet = new AnchorPane();
        noContentsYet.getChildren().add(whatToLoad);

        if (postsUser.isEmpty()){
            gridPane.getChildren().clear();
            gridPane.add(noContentsYet, 0, 1);
        } else if (reviewsUser.isEmpty()) {
            gridPane.getChildren().clear();
            gridPane.add(noContentsYet, 0, 1);
        }

        GridPane.setMargin(noContentsYet, new Insets(100, 200, 200, 331));
    }

    private void updateUIAfterPostDeletion(String postId) {
        resetPage();
        List<PostModelMongo> retrievedPosts = getPosts(currentUser.getUsername());
        this.counterPostsLabel.setText(String.valueOf(retrievedPosts.size()));
        if (retrievedPosts.isEmpty()) {
            loadViewMessageInfo();
        } else {
            postsUser.addAll(retrievedPosts);
            fillGridPane(postsUser);
            prevNextButtonsCheck(postsUser);
        }
    }

    @FXML
    private void fillGridPane(List<?> items) {
        // Setting up what should be called upon post deletion
        deletedPostCallback = this::updateUIAfterPostDeletion;

        columnGridPane = 0; rowGridPane = 0;
        if (postsUser.size() > 1 || reviewsUser.size() > 1){
            rowGridPane++;
        }

        try {
            for (Object item : items) {
                Parent loadViewItem = null;
                AnchorPane anchorPane = new AnchorPane();
                if (item instanceof PostModelMongo) {
                    loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTPOST.getFxmlFile());
                    controllerObjectPost.setData((PostModelMongo) item, postListener, deletedPostCallback);
                } else if (item instanceof ReviewModelMongo) {
                    loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTREVIEW.getFxmlFile());
                    controllerObjectReview.setData((ReviewModelMongo) item);
                }

                anchorPane.getChildren().add(loadViewItem);
                if (item instanceof PostModelMongo){
                    anchorPane.setOnMouseClicked(event -> {
                        this.postListener.onClickPostListener(event, (PostModelMongo) item);});
                }

                if (columnGridPane == 1) {
                    columnGridPane = 0;
                    rowGridPane++;
                }
                gridPane.add(anchorPane, columnGridPane++, rowGridPane);
                gridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
                gridPane.setPrefWidth(500);
                gridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);
                gridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
                gridPane.setPrefHeight(400);
                gridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
                GridPane.setMargin(anchorPane, new Insets(23,5,5,130));
            }
        } catch (Exception ex) {
            stageManager.showInfoMessage("INFO", "An error occurred while retrieving content. Please try again in a while.");
            System.err.println("[ERROR] fillGridPane@ControllerViewUserProfile.java raised an exception: " + ex.getMessage());
        }
    }

    public void onClickLogout(ActionEvent event) {
        modelBean.putBean(Constants.CURRENT_USER, null);
        stageManager.showWindow(FxmlView.WELCOMEPAGE);
        stageManager.closeStageButton(this.logoutButton);
    }

    public void checkSelectedUser() {
        UserModelMongo selectedUser = (UserModelMongo) modelBean.getBean(Constants.SELECTED_USER);
        if (selectedUser == openUserProfile) {              // User found his profile and clicked on it while searching for a user
            modelBean.putBean(Constants.SELECTED_USER, null);
            resetToCurrent();
        } else {
            this.followButton.setDisable(false);        // User decided to open another user's profile
            this.yourProfileButton.setDisable(false);
            this.yourPostsButton.setDisable(true);
            this.yourReviewsButton.setDisable(false);
            openUserProfile = selectedUser;
        }
    }

    public void onClickYourProfileButton() {
        checkSelectedUser();
    }

    private void resetToCurrent(){
        UserModelMongo currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        if (currentUser != null  ) {
            openUserProfile = currentUser;

            // Aggiorna le etichette con le informazioni dell'utente corrente
            this.firstNameLabel.setText(openUserProfile.getName());
            this.lastNameLabel.setText(openUserProfile.getSurname());
            this.nationalityLabel.setText(openUserProfile.getNationality());
            this.usernameLabel.setText(openUserProfile.getUsername());
            this.followerLabel.setText(String.valueOf(userDBNeo.getCountFollowers(openUserProfile.getUsername())));
            this.followingLabel.setText(String.valueOf(userDBNeo.getCountFollowing(openUserProfile.getUsername())));
            this.counterPostsLabel.setText(String.valueOf(postDBMongo.findByUsername(openUserProfile.getUsername()).size()));
            this.counterReviewsLabel.setText(String.valueOf(openUserProfile.getReviews().size()));

            // Imposta l'immagine del profilo
            Image image = new Image(Objects.requireNonNull(getClass().getResource("/user.png")).toExternalForm());
            this.profileImage.setImage(image);

            // Ripristina i post o le recensioni a seconda del tipo di contenuto selezionato
            selectedContentType = ContentType.POSTS;
            resetPage(); //fondamentale per il corretto caricamento dopo onClickYourProfileButton()
            loadContent();
            scrollSet.setVvalue(0);

            // Disabilita il pulsante "Your Profile" se l'utente è già sul proprio profilo
            this.yourPostsButton.setDisable(true);
            this.yourReviewsButton.setDisable(false);
            this.yourProfileButton.setDisable(true);
            this.followButton.setDisable(true);
        }
    }

    public void onClickAccountInfoButton(ActionEvent event) {
        stageManager.switchScene(FxmlView.ACCOUNTINFOPAGE);
    }

    public void onClickStatisticsButton() {
        stageManager.switchScene(FxmlView.STATISTICS);
    }

}
