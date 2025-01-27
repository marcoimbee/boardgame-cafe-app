package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.ReviewDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.PostService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
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
    private Label firstName;
    @FXML
    private Label lastName;
    @FXML
    private Label nationality;
    @FXML
    private Separator separator;
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
    private PostService postService;
    @Autowired
    private PostDBNeo4j postDBNeo4j;
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
    private ControllerObjectReviewBlankBody controllerObjectReviewBlankBody;
    @Autowired
    private ModelBean modelBean;

    public enum ContentType {
        POSTS, REVIEWS;
    }

    private List<PostModelMongo> postsUser = new ArrayList<>();
    private List<ReviewModelMongo> reviewsUser = new ArrayList<>();
    private GenericUserModelMongo openUserProfile;     // The user whose profile is open right now

    private PostListener postListener;

    private int totalFollowerUsers;
    private int totalReviews;
    private int columnGridPane = 0;
    private int rowGridPane = 1;
    private int skipCounter = 0;
    private final static int SKIP = 10;
    private final static int LIMIT = 10;
    private ContentType selectedContentType;
    private static List<String> currentUserFollowedList;
    private static GenericUserModelMongo currentUser;
    private static UserModelMongo selectedUser;
    private Consumer<String> deletedContentCallback;
    private final List<String> buttonLikeMessages = new ArrayList<>(Arrays.asList("Like", "Dislike"));

    private final StageManager stageManager;

    @Autowired
    @Lazy
    public ControllerViewUserProfilePage(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.yourProfileButton.setDisable(true);
        this.yourPostsButton.setDisable(true);
        this.previousButton.setDisable(true);
        this.nextButton.setDisable(true);
        this.selectedContentType = ContentType.POSTS;
        this.resetPage();

        selectedUser = (UserModelMongo) modelBean.getBean(Constants.SELECTED_USER);
        currentUser = (GenericUserModelMongo) modelBean.getBean(Constants.CURRENT_USER);

        if (!currentUser.get_class().equals("user")) {
            currentUser = (AdminModelMongo) modelBean.getBean(Constants.CURRENT_USER);          // The logged user is an admin
            openUserProfile = null;
            if (selectedUser != null) {           // User is looking at another user's profile
                checkSelectedUser();
            }
            this.yourProfileButton.setVisible(false);
        } else {
            currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);           // The logged user is a regular user
            openUserProfile = currentUser;
            if (selectedUser != null) {           // User is looking at another user's profile
                checkSelectedUser();
            } else {
                this.followButton.setDisable(true);
            }
            this.statisticsButton.setVisible(false);
        }

        // Setting profile data
        if (openUserProfile instanceof UserModelMongo userModel) {
            initPage(userModel);
        }
    }

    private void initPage(UserModelMongo user){
        int postsCount = postDBMongo.findByUsername(user.getUsername()).size();
        int totalFollowers = userDBNeo.getCountFollowers(user.getUsername());
        int totalFollowing = userDBNeo.getCountFollowing(user.getUsername());
        int reviewsCount = reviewMongoOp.findReviewByUsername(user.getUsername()).size();
        totalReviews = reviewsCount;

        // Retrieving user posts - these are shown by default as the profile page opens
        postsUser.addAll(getPosts(user.getUsername()));
        if (this.postsUser.isEmpty()) {
            loadViewMessageInfo();
        }
        totalFollowerUsers = totalFollowers;
        this.firstNameLabel.setText(user.getName());
        this.lastNameLabel.setText(user.getSurname());
        this.nationalityLabel.setText(user.getNationality());
        this.usernameLabel.setText(user.getUsername());
        this.followerLabel.setText(String.valueOf(totalFollowers));
        this.followingLabel.setText(String.valueOf(totalFollowing));
        this.counterPostsLabel.setText(String.valueOf(postsCount));
        this.counterReviewsLabel.setText(String.valueOf(reviewsCount));
        String profileImageFilename;
        Image image;

        // Disabling follow/unfollow button based on if the selected user is banned or not - only admins can visit the profile of a banned user
        if (selectedUser != null && selectedUser.isBanned()) {
            this.followButton.setDisable(true);
            profileImageFilename = "/images/bannedUser.png";
        } else {
            profileImageFilename = "/images/user.png";
        }
        image = new Image(Objects.requireNonNull(getClass().getResource(profileImageFilename)).toExternalForm());
        this.profileImage.setImage(image);

         fillGridPane(postsUser);

        // If we enter here we are at the app start - it's the first time we see this page, so the constant is certainly empty
        if (modelBean.getBean(Constants.CURRENT_USER_FOLLOWED_LIST) == null) {
            currentUserFollowedList = userDBNeo.getFollowedUsernames(currentUser.getUsername());
            modelBean.putBean(Constants.CURRENT_USER_FOLLOWED_LIST, currentUserFollowedList);
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
            Stage detailsStage = stageManager.showWindow(FxmlView.DETAILS_POST);
            detailsStage.setOnHidden(windowEvent -> {
                String lastPostId = ((PostModelMongo)modelBean.getBean(Constants.SELECTED_POST)).getId();
                AnchorPane eventAnchorPanePost = (AnchorPane) (mouseEvent.getSource());
                ((Label)eventAnchorPanePost.lookup("#counterLikesLabel")).setText(
                        String.valueOf(post.getLikeCount()));
                Button workingButton = ((Button)eventAnchorPanePost.lookup("#likeButton"));
                FontAwesomeIconView workingIcon = (FontAwesomeIconView) workingButton.getGraphic();
                boolean likeIsPresent = this.postService.hasLikedPost(currentUser.getUsername(), lastPostId);
                workingIcon.setIcon((likeIsPresent) ? FontAwesomeIcon.THUMBS_DOWN : FontAwesomeIcon.THUMBS_UP);
                workingButton.setText((this.buttonLikeMessages.get((likeIsPresent) ? 1 : 0)));
            });
        };

        // Setting up what should be called upon post or review deletion
        deletedContentCallback = this::updateUIAfterPostOrReviewDeletion;

        // Page focus listener - needed to potentially update UI when coming back from a post detail window
        gridPane.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.focusedProperty().addListener((observableFocus, wasFocused, isNowFocused) -> {
                    if (isNowFocused) {
                        // After gaining focus for a post details window closing or a review update window closing,
                        // UI needs to be potentially updated
                        if (selectedContentType == ContentType.POSTS) {
                            if (modelBean.getBean(Constants.OPENED_POST) != null) {
                                onRegainPageFocusAfterPostDetailsWindowClosing();
                                modelBean.putBean(Constants.OPENED_POST, null);
                            }
                        } else {
                            onRegainPageFocusAfterEditReviewWindowClosing();
                        }
                    }
                });
            }
        });
    }

    private void onRegainPageFocusAfterEditReviewWindowClosing() {
        // Potentially update UI after review editing
        ReviewModelMongo updatedReview = (ReviewModelMongo) modelBean.getBean(Constants.UPDATED_REVIEW);
        if (updatedReview != null) {
            modelBean.putBean(Constants.UPDATED_REVIEW, null);
            reviewsUser.replaceAll(review -> review.getId().equals(updatedReview.getId()) ? updatedReview : review);
            gridPane.getChildren().clear();
            fillGridPane(reviewsUser);
            prevNextButtonsCheck(reviewsUser);
        }

        /*
            No need to handle review deletion here,
            as that operation is only viable by using the
            'Delete' button on the review card
         */
    }

    private void refreshPage() {
        postsUser.clear();
        resetPage();
//        checkSelectedUser();
        List<PostModelMongo> retrievedPosts = new ArrayList<>();
        if (openUserProfile.get_class().equals("user")) {
            retrievedPosts = getPosts(openUserProfile.getUsername());
        }
        postsUser.addAll(retrievedPosts);
        fillGridPane(postsUser);
        prevNextButtonsCheck(retrievedPosts);
        if (this.postsUser.isEmpty()) {
            loadViewMessageInfo();
        }
    }

    private void onRegainPageFocusAfterPostDetailsWindowClosing() {
        refreshPage();
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
            } else {
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
            }
        } catch (Exception e) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Please try again in a while.");
            System.err.println("[ERROR] onClickFollowButton()@ControllerViewUserProfilePage raised an exception: " + e.getMessage());
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
                postsUser.addAll((Collection<? extends PostModelMongo>) items);
                gridPane.getChildren().clear();
                fillGridPane(items);
            }
        } else if (selectedContentType.equals(ContentType.REVIEWS)){
            List<?> items = getReviews(this.usernameLabel.getText());
            if (items.isEmpty()) {
                loadViewMessageInfo();
            } else {
                reviewsUser.addAll((Collection<? extends ReviewModelMongo>) items);
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
            if (contentList.size() <= LIMIT) {
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
        List<PostModelMongo> posts = postDBMongo.findRecentPostsByUsername(username, LIMIT, skipCounter);
        prevNextButtonsCheck(posts);
        return posts;
    }

    private List<ReviewModelMongo> getReviews(String username) {
        List<ReviewModelMongo> reviews = reviewMongoOp.findRecentReviewsByUsername(username, LIMIT, skipCounter);
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

    /*
        This method gets called whenever the author decides to delete a comment or a
        review by using the 'Delete' button in the comment/review card
     */
    private void updateUIAfterPostOrReviewDeletion(String contentId) {
        resetPage();
        switch (selectedContentType) {
            case POSTS:
                List<PostModelMongo> retrievedPosts = getPosts(currentUser.getUsername());
                this.counterPostsLabel.setText(String.valueOf(retrievedPosts.size()));
                if (retrievedPosts.isEmpty()) {
                    loadViewMessageInfo();
                } else {
                    postsUser.addAll(retrievedPosts);
                    fillGridPane(postsUser);
                    prevNextButtonsCheck(postsUser);
                }
                break;
            case REVIEWS:
                List<ReviewModelMongo> retrievedReviews = getReviews(currentUser.getUsername());
                this.counterReviewsLabel.setText(String.valueOf(retrievedReviews.size()));
                if (retrievedReviews.isEmpty()) {
                    loadViewMessageInfo();
                } else {
                    reviewsUser.addAll(retrievedReviews);
                    fillGridPane(reviewsUser);
                    prevNextButtonsCheck(reviewsUser);
                }
                break;
        }
    }

    @FXML
    private void fillGridPane(List<?> items) {
        if (selectedContentType == ContentType.POSTS) {
            columnGridPane = 0;
            if (postsUser.size() == 1) {
                rowGridPane = 0;
            } else {
                rowGridPane = 1;
            }
        } else {
            columnGridPane = 0;
            if (reviewsUser.size() == 1) {
                rowGridPane = 0;
            } else {
                rowGridPane = 1;
            }
        }

        try {
            for (Object item : items) {
                AnchorPane itemNode = createItemViewNode(item);
                addItemToGridPane(itemNode);
            }
        } catch (Exception ex) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Please try again in a while.");
            System.err.println("[ERROR] fillGridPane()@ControllerViewUserProfile.java raised an exception: " + ex.getMessage());
        }
    }

    private AnchorPane createItemViewNode(Object item) {
        Parent loadViewItem = null;
        AnchorPane anchorPane = new AnchorPane();
        deletedContentCallback = this::updateUIAfterPostOrReviewDeletion;
        if (item instanceof PostModelMongo) {
            loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTPOST.getFxmlFile());
            controllerObjectPost.setData((PostModelMongo) item, postListener, deletedContentCallback);
        } else if (item instanceof ReviewModelMongo) {
            if (((ReviewModelMongo) item).getBody().isEmpty()) {
                loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTREVIEWBLANKBODY.getFxmlFile());
                controllerObjectReviewBlankBody.setData((ReviewModelMongo) item, deletedContentCallback);
            } else {
                loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTREVIEW.getFxmlFile());
                controllerObjectReview.setData((ReviewModelMongo) item, deletedContentCallback);
            }
        }

        anchorPane.getChildren().add(loadViewItem);
        if (item instanceof PostModelMongo) {
            anchorPane.setOnMouseClicked(event -> {
                this.postListener.onClickPostListener(event, (PostModelMongo) item);
            });
        }

        return anchorPane;
    }

    private void addItemToGridPane(AnchorPane itemNode) {
        if (columnGridPane == 1) {
            columnGridPane = 0;
            rowGridPane++;
        }

        gridPane.add(itemNode, columnGridPane++, rowGridPane);

        gridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
        gridPane.setPrefWidth(500);
        gridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);

        gridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
        gridPane.setPrefHeight(400);
        gridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);

        GridPane.setMargin(itemNode, new Insets(23,5,5,130));
    }

    public void onClickLogout() {
        modelBean.putBean(Constants.CURRENT_USER, null);
        stageManager.switchScene(FxmlView.WELCOMEPAGE);
    }

    public void checkSelectedUser() {
        UserModelMongo selectedUser = (UserModelMongo) modelBean.getBean(Constants.SELECTED_USER);
        // Selected User Management Check, in case Selected variable has been initialized with object different from first current user
        if (selectedUser == openUserProfile && openUserProfile.get_class().equals("user")) {   // User found his profile and clicked on it while searching for a user
            modelBean.putBean(Constants.SELECTED_USER, null);
            resetToCurrent();
        } else {
            // Selected User Management
            if (currentUser.get_class().equals("admin")) {
                openUserProfile = selectedUser;
                this.yourProfileButton.setVisible(false);
                this.statisticsButton.setVisible(true);
                this.followButton.setDisable(true);
                modelBean.putBean(Constants.SELECTED_USER, null);
            } else {
                openUserProfile = selectedUser;
                if(currentUser.getUsername().equals(openUserProfile.getUsername()) ){
                    this.yourProfileButton.setDisable(true);
                    this.followButton.setDisable(true);
                } else {
                    this.yourProfileButton.setDisable(false);
                    this.followButton.setDisable(false);
                }
                this.yourPostsButton.setDisable(true);
                this.yourReviewsButton.setDisable(false);
                modelBean.putBean(Constants.SELECTED_USER, null);
            }
        }
    }

    public void onClickYourProfileButton() {
        if (openUserProfile != currentUser) {
            resetToCurrent();
        } else {
            checkSelectedUser();
        }
    }

    private void resetToCurrent(){
        UserModelMongo currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        if (currentUser != null) {
            resetPage();        // Mandatory to correctly load the  content
            openUserProfile = currentUser;
            totalReviews = reviewMongoOp.findReviewByUsername(openUserProfile.getUsername()).size();

            // Updating labels with information about the current user
            if (openUserProfile instanceof UserModelMongo userModel) {
                this.firstNameLabel.setText(userModel.getName());
                this.lastNameLabel.setText(userModel.getSurname());
                this.nationalityLabel.setText(userModel.getNationality());
                this.counterReviewsLabel.setText(String.valueOf(totalReviews));
            }
            this.usernameLabel.setText(openUserProfile.getUsername());
            this.followerLabel.setText(String.valueOf(userDBNeo.getCountFollowers(openUserProfile.getUsername())));
            this.followingLabel.setText(String.valueOf(userDBNeo.getCountFollowing(openUserProfile.getUsername())));
            this.counterPostsLabel.setText(String.valueOf(postDBMongo.findByUsername(openUserProfile.getUsername()).size()));

            Image image = new Image(Objects.requireNonNull(getClass().getResource("/images/user.png")).toExternalForm());
            this.profileImage.setImage(image);

            selectedContentType = ContentType.POSTS;
            loadContent();
            scrollSet.setVvalue(0);

            this.yourPostsButton.setDisable(true);
            this.yourReviewsButton.setDisable(false);
            this.yourProfileButton.setDisable(true);
            this.followButton.setDisable(true);
        }
    }

    public void onClickAccountInfoButton() {
        stageManager.switchScene(FxmlView.ACCOUNTINFOPAGE);
    }

    public void onClickStatisticsButton() {
        stageManager.switchScene(FxmlView.STATISTICS);
    }
}
