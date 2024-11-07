package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;

@Component
public class ControllerViewUserProfilePage implements Initializable{

    @FXML
    private Button yourProfileButton;
    @FXML
    private Button postsFeedButton;
    @FXML
    private Button editProfileButton;
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
    @FXML
    private ImageView profileImage;

    @FXML
    private GridPane gridPane;
    @FXML
    private ScrollPane scrollSet;
    @Autowired
    private PostDBMongo postDBMongo;
    @Autowired
    private UserDBMongo userMongoOp;
    @Autowired
    private UserDBNeo4j userDBNeo;
    @Autowired
    private ControllerObjectPost controllerObjectPost;
    @Autowired
    private ControllerObjectReview controllerObjectReview;
    private final StageManager stageManager;

    //Post Variables
    private List<PostModelMongo> postsUser = new ArrayList<>();
    private List<ReviewModelMongo> reviews = new ArrayList<>();
    private PostListener postListener;

    //Utils Variables
    private int totalFollowerUsers;
    private int totalFollowingUsers;
    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;
    private final static int SKIP = 10; //how many posts to skip per time
    private final static int LIMIT = 10; //how many posts to show for each page

    private final static Logger logger = LoggerFactory.getLogger(PostDBMongo.class);

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
        resetPage();

        Optional<GenericUserModelMongo> user = userMongoOp.findByUsername("blackfish414");
        if (user.isPresent()){
            UserModelMongo regUser = (UserModelMongo) user.get();
            totalFollowerUsers = userDBNeo.getCountFollowers(regUser.getUsername());
            totalFollowingUsers = userDBNeo.getCountFollowing(regUser.getUsername());

            this.postsUser.addAll(getData(regUser.getUsername()));
            if (this.postsUser.isEmpty()) {
                stageManager.showInfoMessage("INFO", "Database is empty!");
                try {
                    Platform.exit();
                    System.exit(0);
                } catch (Exception e) {
                    logger.error("Exception occurred: " + e.getLocalizedMessage());
                }
            }

            this.firstNameLabel.setText(regUser.getName());
            this.lastNameLabel.setText(regUser.getSurname());
            this.nationalityLabel.setText(regUser.getNationality());
            this.usernameLabel.setText(regUser.getUsername());
            this.followerLabel.setText(String.valueOf(totalFollowerUsers));
            this.followingLabel.setText(String.valueOf(totalFollowingUsers));
            this.counterPostsLabel.setText(String.valueOf(postsUser.size()));
            this.counterReviewsLabel.setText(String.valueOf(regUser.getReviews().size()));
            Image image = new Image(Objects.requireNonNull(getClass().getResource("/user.png")).toExternalForm());
            this.profileImage.setImage(image);

            fillGridPane();

        }else{
            System.out.println("\n Error with user info loading: User Not Found");
        }
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
                "A breve verrai reindirizzato alla pagina in cui potrai ricercare e seguire nuovi utenti.\n";

        stageManager.showInfoMessage(title, message);
    }

    public void onClickEditProfileButton() {
        stageManager.showWindow(FxmlView.SIGNUP);
    }

    public void onClickPostsButton() {

        this.yourReviewsButton.setDisable((false));
        this.yourPostsButton.setDisable((true));

        String title = "ToDo Message";
        String message = "" +
                "A breve tramite quest'azione potrai visualizzare i tuoi posts.\n";

        stageManager.showInfoMessage(title, message);
    }
    public void onClickReviewsButton() {

        this.yourReviewsButton.setDisable((true));
        this.yourPostsButton.setDisable((false));

        String title = "ToDo Message";
        String message = "" +
                "A breve tramite quest'azione potrai visualizzare le tue reviews.\n";

        stageManager.showInfoMessage(title, message);

    }


    @FXML
    void onClickNext() {
        //clear variables
        gridPane.getChildren().clear();
        postsUser.clear();

        //update the skipcounter
        skipCounter += SKIP;

        //retrieve boardgames
        postsUser.addAll(getData(usernameLabel.toString()));
        //put all boardgames in the Pane
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    @FXML
    void onClickPrevious() {
        //clear variables
        gridPane.getChildren().clear();
        postsUser.clear();

        //update the skipcounter
        skipCounter -= SKIP;

        //retrieve boardgames
        postsUser.addAll(getData(usernameLabel.toString()));
        //put all boardgames in the Pane
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    void resetPage() {
        //clear variables
        gridPane.getChildren().clear();
        postsUser.clear();
        skipCounter = 0;
    }

    void prevNextButtonsCheck(List<PostModelMongo> posts){
        if((posts.size() > 0)){
            if((posts.size() < LIMIT)){
                if(skipCounter <= 0 ){
                    previousButton.setDisable(true);
                    nextButton.setDisable(true);
                }
                else{
                    previousButton.setDisable(false);
                    nextButton.setDisable(true);
                }
            }
            else{
                if(skipCounter <= 0 ){
                    previousButton.setDisable(true);
                    nextButton.setDisable(false);
                }
                else{
                    previousButton.setDisable(false);
                    nextButton.setDisable(false);
                }
            }
        }
        else{
            if(skipCounter <= 0 ){
                previousButton.setDisable(true);
                nextButton.setDisable(true);
            }
            else {
                previousButton.setDisable(false);
                nextButton.setDisable(true);
            }
        }
    }

    private List<PostModelMongo> getData(String username){

        List<PostModelMongo> posts =
                postDBMongo.findByUsername(username);

        prevNextButtonsCheck(posts);
        return posts;
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
            String title = "ToDo Message";
            String message = "" +
                    "A breve verrai reindirizzato alla pagina in cui puoi vedere i dettagli del post.\n";

            stageManager.showInfoMessage(title, message);
        };

        //CREATE FOR EACH POST AN ITEM (ObjectPosts)
        try {
            for (PostModelMongo post : postsUser) { // iterando lista di posts

                Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTPOST.getFxmlFile());

                AnchorPane anchorPane = new AnchorPane();
                anchorPane.getChildren().add(loadViewItem);

                controllerObjectPost.setData(post, postListener);

                //choice number of column
                if (columnGridPane == 1) {
                    columnGridPane = 0;
                    rowGridPane++;
                }

                gridPane.add(anchorPane, columnGridPane++, rowGridPane); //(child,column,row)
                //DISPLAY SETTINGS
                //set grid width
                gridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
                gridPane.setPrefWidth(500);
                gridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);
                //set grid height
                gridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
                gridPane.setPrefHeight(400);
                gridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
                //GridPane.setMargin(anchorPane, new Insets(25));
                GridPane.setMargin(anchorPane, new Insets(12,5,12,130));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickLogout(ActionEvent event) {
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
