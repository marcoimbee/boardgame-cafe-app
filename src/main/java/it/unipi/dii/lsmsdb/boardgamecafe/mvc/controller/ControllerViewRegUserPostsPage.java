package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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
    private TextField textFieldSearch;
    @FXML
    private GridPane postGridPane;
    @FXML
    private ScrollPane scrollSet;
    @Autowired
    private PostDBMongo postDBMongo;
    @Autowired
    private ControllerObjectPost controllerObjectPost;
    private final StageManager stageManager;

    //Post Variables
    private List<PostModelMongo> posts = new ArrayList<>();

    private PostListener postListener;

    //Utils Variables
    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;
    private final static int SKIP = 10; //how many posts to skip per time
    private final static int LIMIT = 10; //how many posts to show for each page

    private final static Logger logger = LoggerFactory.getLogger(PostDBMongo.class);

    @Autowired
    @Lazy
    public ControllerViewRegUserPostsPage(StageManager stageManager) {
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
                logger.error("Exception occurred: " + e.getLocalizedMessage());
            }
        }

        fillGridPane();
    }

    public void onClickBoardgamesColletcion(ActionEvent actionEvent) {
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

    private List<PostModelMongo> getData(){

        List<PostModelMongo> posts =
                postDBMongo.findRecentPosts(LIMIT, skipCounter);

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
            stageManager.switchScene(FxmlView.USERPOFILEPAGE);
            stageManager.closeStageMouseEvent(mouseEvent);
        };

        //CREATE FOR EACH POST AN ITEM (ObjectPosts)
        try {
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
                GridPane.setMargin(anchorPane, new Insets(15,5,15,180));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickLogout(ActionEvent event) {
        stageManager.showWindow(FxmlView.WELCOMEPAGE);
        stageManager.closeStageButton(this.logoutButton);
    }

    public void onClickYourProfile(ActionEvent event) {
        stageManager.showWindow(FxmlView.USERPOFILEPAGE);
        stageManager.closeStageButton(this.logoutButton);
    }

    public void onClickAccountDetails(ActionEvent event) {
        stageManager.showWindow(FxmlView.SIGNUP);
    }

    public void onClickSearchUserButton(ActionEvent event) {
    }
}
