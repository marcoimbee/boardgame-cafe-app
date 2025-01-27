package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.net.URL;
import java.util.*;

@Component
public class ControllerViewGuestPostsPage implements Initializable {

    @FXML
    private Button boardgamesCollectionButton;
    @FXML
    private Button boardgamePostsButton;
    @FXML
    private Button returnWPageButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button previousButton;
    @FXML
    private TextField textFieldSearch;
    @FXML
    private Button loginButton;
    @FXML
    private GridPane postGridPane;
    @FXML
    private ScrollPane scrollSet;

    @Autowired
    private PostDBMongo postDBMongo;
    @Autowired
    private ControllerObjectPost controllerObjectPost;

    private final StageManager stageManager;
    private List<PostModelMongo> posts = new ArrayList<>();
    private List<PostModelNeo4j> postsNeo4j = new ArrayList<>();
    private PostListener postListener;

    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;
    private final static int SKIP = 10;     // How many posts to skip per time
    private final static int LIMIT = 10;    // How many posts to show for each page

    @Autowired
    @Lazy
    public ControllerViewGuestPostsPage(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.boardgamePostsButton.setDisable(true);
        this.previousButton.setDisable(true);
        this.nextButton.setDisable(true);
        resetPage();

        posts.addAll(getData());
        if (posts.isEmpty()) {
            stageManager.showInfoMessage("INFO", "Database is empty!");
            try {
                Platform.exit();
                System.exit(0);
            } catch (Exception e) {
                System.err.println("[ERROR] initialize()@ControllerViewGuestsPostsPage.java raised an exception: " + e.getMessage());
            }
        }

        fillGridPane();
    }

    public void onClickBoardgamesCollection() {
        stageManager.showWindow(FxmlView.GUESTBOARDGAMES);
        stageManager.closeStageButton(this.boardgamesCollectionButton);
    }

    public void onClickSignUp() {
        stageManager.showWindow(FxmlView.SIGNUP);
    }

    public void onClickSearch() {
        String title = "Content access permissions";
        String message = "You need to be a registered user to search for a specific boardgame.";
        stageManager.showInfoMessage(title, message);
    }

    public void onClickClearField() {
        this.textFieldSearch.clear();
    }

    public void onClickReturnWelcomePage() {
        stageManager.closeStageButton(this.returnWPageButton);
        stageManager.showWindow(FxmlView.WELCOMEPAGE);
    }

    @FXML
    void onClickNext() {
        postGridPane.getChildren().clear();
        posts.clear();
        skipCounter += SKIP;
        posts.addAll(getData());
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    @FXML
    void onClickPrevious() {
        postGridPane.getChildren().clear();
        posts.clear();
        skipCounter -= SKIP;
        posts.addAll(getData());
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    void resetPage() {
        postGridPane.getChildren().clear();
        posts.clear();
        skipCounter = 0;
        this.nextButton.setDisable(true);
        this.previousButton.setDisable(true);
        scrollSet.setVvalue(0);
    }

    void prevNextButtonsCheck(List<PostModelMongo> posts) {
        if (posts.size() > 0){
            if (posts.size() < LIMIT) {
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

    private List<PostModelMongo> getData() {
        List<PostModelMongo> posts = postDBMongo.findRecentPosts(LIMIT, skipCounter);
        prevNextButtonsCheck(posts);
        return posts;
    }

    @FXML
    void fillGridPane() {
        // Needed to correctly position a single element in the grid pane
        if (posts.size() == 1) {
            columnGridPane = 0;
            rowGridPane = 0;
        } else {
            columnGridPane = 0;
            rowGridPane = 1;
        }

        postListener = (MouseEvent mouseEvent, PostModelMongo post) -> {
            String title = "Content access permissions";
            String message = "\t\t\tCurious to view the contents of this post?\n" +
                             "\t\t\nSign-up using the button on the left side panel to do this and much more.";
            stageManager.showInfoMessage(title, message);
        };

        try {
            for (PostModelMongo post : posts) {
                Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTPOST.getFxmlFile());

                AnchorPane anchorPane = new AnchorPane();
                anchorPane.getChildren().add(loadViewItem);

                controllerObjectPost.setData(post, postListener, null);
                anchorPane.setOnMouseClicked(event -> {
                    this.postListener.onClickPostListener(event, post);});

                if (columnGridPane == 1) {
                    columnGridPane = 0;
                    rowGridPane++;
                }

                postGridPane.add(anchorPane, columnGridPane++, rowGridPane);
                postGridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
                postGridPane.setPrefWidth(500);
                postGridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);
                postGridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
                postGridPane.setPrefHeight(400);
                postGridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
                GridPane.setMargin(anchorPane, new Insets(15,5,15,180));

            }
        } catch (Exception e) {
            System.err.println("[ERROR] fillGridPane()@ControllerViewGuestPostsPage.java raised an exception: " + e.getMessage());
            stageManager.showInfoMessage("INFO", "Something went wrong. Please try again in a while.");
        }
    }

    public void onClickLogin() {
        stageManager.showWindow(FxmlView.LOGIN);
        stageManager.closeStageButton(this.loginButton);
    }

    public void onClickRefreshButton() {
        cleanFetchAndFill();
    }

    private void cleanFetchAndFill() {
        resetPage();
        this.posts.addAll(getData());
        fillGridPane();
    }
}
