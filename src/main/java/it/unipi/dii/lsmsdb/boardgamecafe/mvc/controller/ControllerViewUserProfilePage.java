package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
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
import javafx.application.Platform;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;

@Component
public class ControllerViewUserProfilePage implements Initializable{

    public enum ContentType {
        POSTS, REVIEWS;
    }
    //********* Buttons *********
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

    // ********** Llabels *********
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

    //********* Other Components *********
    @FXML
    private ImageView profileImage;
    @FXML
    private GridPane gridPane;
    @FXML
    private ScrollPane scrollSet;

    //********* Autowireds *********
    @Autowired
    private PostDBMongo postDBMongo;
    @Autowired
    private ReviewDBMongo reviewMongoOp;
    @Autowired
    private UserDBMongo userMongoOp;
    @Autowired
    private UserDBNeo4j userDBNeo;
    @Autowired
    private ControllerObjectPost controllerObjectPost;
    @Autowired
    private ControllerObjectReview controllerObjectReview;
    @Autowired
    private ModelBean modelBean;

    //Stage Manager
    private final StageManager stageManager;

    //Posts/Reviews Lists
    private List<PostModelMongo> postsUser = new ArrayList<>();
    private List<ReviewModelMongo> reviewsUser = new ArrayList<>();
    private UserModelMongo regUser;

    //Listeners
    private PostListener postListener;

    //Useful Variables
    private int totalFollowerUsers;
    private int totalFollowingUsers;
    private int totalPosts;
    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;
    private final static int SKIP = 10; //how many posts to skip per time
    private final static int LIMIT = 10; //how many posts to show for each page
    private ContentType selectedContentType; // variabile di stato per tipo di contenuto

    private final static Logger logger = LoggerFactory.getLogger(PostDBMongo.class);
    @Autowired
    @Lazy
    public ControllerViewUserProfilePage(StageManager stageManager) {
        this.stageManager = stageManager;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.followButton.setDisable(true);
        this.yourProfileButton.setDisable(true);
        this.yourPostsButton.setDisable(true);
        this.previousButton.setDisable(true);
        this.nextButton.setDisable(true);
        this.selectedContentType = ContentType.POSTS;
        this.resetPage();

        regUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);

        List<PostModelMongo> fullPosts = postDBMongo.findByUsername(regUser.getUsername());
        totalPosts = fullPosts.size();
        totalFollowerUsers = userDBNeo.getCountFollowers(regUser.getUsername());
        totalFollowingUsers = userDBNeo.getCountFollowing(regUser.getUsername());


        postsUser.addAll(getPosts(regUser.getUsername()));
        if (this.postsUser.isEmpty()) {
            stageManager.showInfoMessage("Info Posts", "No Posts Yet");
        }
        this.firstNameLabel.setText(regUser.getName());
        this.lastNameLabel.setText(regUser.getSurname());
        this.nationalityLabel.setText(regUser.getNationality());
        this.usernameLabel.setText(regUser.getUsername());
        this.followerLabel.setText(String.valueOf(totalFollowerUsers));
        this.followingLabel.setText(String.valueOf(totalFollowingUsers));
        this.counterPostsLabel.setText(String.valueOf(totalPosts));
        this.counterReviewsLabel.setText(String.valueOf(regUser.getReviews().size()));
        Image image = new Image(Objects.requireNonNull(getClass().
                                getResource("/user.png")).toExternalForm());
        this.profileImage.setImage(image);

        fillGridPane(postsUser);
    }

    public void onClickBoardgamesButton() {
        stageManager.showWindow(FxmlView.REGUSERBOARDGAMES);
        stageManager.closeStageButton(this.boardgamesCollectionButton);
    }

    public void onClickPostsFeedButton() {
        stageManager.showWindow(FxmlView.REGUSERPOSTS);
        stageManager.closeStageButton(this.postsFeedButton);
    }

    public void onClickSearchUserButton() {
        String title = "ToDo Message";
        String message = "" +
                "A breve vedrai la pagina in cui potrai ricercare e seguire nuovi utenti.\n";

        stageManager.showInfoMessage(title, message);
    }

    public void onClickFollowButton() {
        String title = "Work in Progress";
        String message = "" +
                "A breve avrai la possibilità di seguire questo utente.\n";
        stageManager.showInfoMessage(title, message);;
    }

    public void onClickPostsButton() {
        this.yourReviewsButton.setDisable(false);
        this.yourPostsButton.setDisable(true);
        this.selectedContentType = ContentType.POSTS;
        resetPage();
        loadContent();
        scrollSet.setVvalue(0);
    }

    public void onClickReviewsButton() {
        this.yourReviewsButton.setDisable(true);
        this.yourPostsButton.setDisable(false);
        this.selectedContentType = ContentType.REVIEWS;
        resetPage();
        loadContent();
        scrollSet.setVvalue(0);
    }

    // Metodo per caricare il contenuto in base a `selectedContentType`
    private void loadContent() {
        resetPage();
        if (selectedContentType.equals(ContentType.POSTS)) {
            List<?> items = getPosts(this.usernameLabel.getText());
            if (items.isEmpty()) {
                stageManager.showInfoMessage("Info Posts", "No Posts Yet");
            }
            fillGridPane(items);
        } else if (selectedContentType.equals(ContentType.REVIEWS)){
            List<?> items = getReviews(this.usernameLabel.getText());
            if (items.isEmpty()) {
                stageManager.showInfoMessage("Info Reviews", "No Reviews Yet");
            }
            fillGridPane(items);
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

    private void setGridPaneColumnAndRow(){
        columnGridPane = 0;
        rowGridPane = 1;
    }

    @FXML
    private void fillGridPane(List<?> items) {
        columnGridPane = 0;
        rowGridPane = 0;
        setGridPaneColumnAndRow();

        try {
            for (Object item : items) {
                Parent loadViewItem;
                AnchorPane anchorPane = new AnchorPane();

                if (item instanceof PostModelMongo) {
                    loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTPOST.getFxmlFile());
                    postListener = (MouseEvent mouseEvent, PostModelMongo post) -> {
                        String title = "Work in Progress";
                        String message = "" +
                                "A breve vedrai la pagina in cui potrai ci saranno i dettagli del post.\n";
                        stageManager.showInfoMessage(title, message);
                    };
                    controllerObjectPost.setData((PostModelMongo) item, postListener);

                } else if (item instanceof ReviewModelMongo) {
                    loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTREVIEW.getFxmlFile());
                    controllerObjectReview.setData((ReviewModelMongo) item);
                } else {
                    continue;
                }
                anchorPane.getChildren().add(loadViewItem);

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
                GridPane.setMargin(anchorPane, new Insets(12,5,12,130));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickLogout(ActionEvent event) {
        modelBean.putBean(Constants.CURRENT_USER, null);
        stageManager.showWindow(FxmlView.WELCOMEPAGE);
        stageManager.closeStageButton(this.logoutButton);
    }

    public void onClickYourProfileButton(ActionEvent event) {
        stageManager.showWindow(FxmlView.USERPROFILEPAGE);
        stageManager.closeStageButton(this.yourProfileButton);
    }

    public void onClickAccountInfoButton(ActionEvent event) {
        stageManager.showWindow(FxmlView.SIGNUP);
    }

}
