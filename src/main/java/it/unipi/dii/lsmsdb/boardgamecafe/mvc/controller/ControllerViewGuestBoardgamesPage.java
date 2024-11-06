package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.BoardgameListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.BoardgameDBMongo;
import javafx.application.Platform;
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
public class ControllerViewGuestBoardgamesPage implements Initializable {

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
    private Button searchButton;
    @FXML
    private Button clearFieldButton;
    @FXML
    private TextField textFieldSearch;
    @FXML
    private Button signUpButton;
    @FXML
    private Button loginButton;
    @FXML
    private GridPane boardgameGridPane;
    @FXML
    private ScrollPane scrollSet;

    @Autowired
    private BoardgameDBMongo boardgameDBMongo;
    @Autowired
    private ControllerObjectBoardgame controllerObjectBoardgame;
    private final StageManager stageManager;

    //Boardgame Variables
    private List<BoardgameModelMongo> boardgames = new ArrayList<>();
    private BoardgameListener boardgameListener;

    //Utils Variables
    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;
    private final static int SKIP = 12; //how many boardgame to skip per time
    private final static int LIMIT = 12; //how many boardgame to show for each page

    private final static Logger logger = LoggerFactory.getLogger(BoardgameDBMongo.class);

    @Autowired
    @Lazy
    public ControllerViewGuestBoardgamesPage(StageManager stageManager) {
        this.stageManager = stageManager;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.boardgamesCollectionButton.setDisable(true);
        this.previousButton.setDisable(true);
        this.nextButton.setDisable(true);
        resetPage();

        boardgames.addAll(getData());
        if (boardgames.isEmpty()) {
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

    public void onClickBoardgamePosts(ActionEvent actionEvent) {
        stageManager.showWindow(FxmlView.GUESTPOSTS);
        stageManager.closeStageButton(this.boardgamePostsButton);
    }

    public void onClickSignUp(ActionEvent actionEvent) {
        stageManager.showWindow(FxmlView.SIGNUP);
    }

    public void onClickSearch() {

        String text = this.textFieldSearch.getText();

        stageManager.showInfoMessage("Info Text", text);
    }

    public void onClickClearField() {
        this.textFieldSearch.clear();
    }

    public void onClickReturnWelcomePage(ActionEvent actionEvent)
    {
        stageManager.showWindow(FxmlView.WELCOMEPAGE);
        stageManager.closeStageButton(this.returnWPageButton);
    }

    /*public void onClickImageBoardgame(MouseEvent event) {
        int imageIndex = stageManager.getElemIndexGridPane(event);
        imageIndex--;
        BoardgameModelMongo boardgame = this.boardgames.get((18*counterPages)+imageIndex);
        BoardgamecafeApplication.getInstance().getModelBean().putBean(Symbols.SELECTED_BOARDGAME, boardgame);
        stageManager.showWindow(FxmlView.BOARDGAME_DETAILS);
    }*/


    @FXML
    void onClickNext() {
        //clear variables
        boardgameGridPane.getChildren().clear();
        boardgames.clear();

        //update the skipcounter
        skipCounter += SKIP;

        //retrieve boardgames
        boardgames.addAll(getData());
        //put all boardgames in the Pane
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    @FXML
    void onClickPrevious() {
        //clear variables
        boardgameGridPane.getChildren().clear();
        boardgames.clear();

        //update the skipcounter
        skipCounter -= SKIP;

        //retrieve boardgames
        boardgames.addAll(getData());
        //put all boardgames in the Pane
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    void prevNextButtonsCheck(List<BoardgameModelMongo> boardgames){
        if((boardgames.size() > 0)){
            if((boardgames.size() < LIMIT)){
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

    void resetPage() {
        //clear variables
        boardgameGridPane.getChildren().clear();
        boardgames.clear();
        skipCounter = 0;
    }
    private List<BoardgameModelMongo> getData(){

        List<BoardgameModelMongo> boardgames =
                boardgameDBMongo.findRecentBoardgames(LIMIT, skipCounter);

        prevNextButtonsCheck(boardgames);
        return boardgames;
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

        boardgameListener = (MouseEvent mouseEvent, BoardgameModelMongo boardgame) -> {
            String title = "Content Access Permissions";
            String message = "" +
                    "\t\t\tCurious To View The Content Of This Boardgame?\n" +
                    "\t\t\nSign-Up Via The Appropriate Button On The Left Side To Do This And More.";

            stageManager.showInfoMessage(title, message);
        };

        //CREATE FOR EACH BOARDGAME AN ITEM (ObjectBoardgame)
        try {
            for (BoardgameModelMongo boardgame : boardgames) { // iterando lista di boardgames

                Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTBOARDGAME.getFxmlFile());

                AnchorPane anchorPane = new AnchorPane();
                anchorPane.getChildren().add(loadViewItem);

                controllerObjectBoardgame.setData(boardgame, boardgameListener);

                //choice number of column
                if (columnGridPane == 4) {
                    columnGridPane = 0;
                    rowGridPane++;
                }

                boardgameGridPane.add(anchorPane, columnGridPane++, rowGridPane); //(child,column,row)
                //DISPLAY SETTINGS
                //set grid width
                boardgameGridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
                boardgameGridPane.setPrefWidth(430);
                boardgameGridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);
                //set grid height
                boardgameGridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
                boardgameGridPane.setPrefHeight(300);
                boardgameGridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
                GridPane.setMargin(anchorPane, new Insets(22));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickLogin(ActionEvent event) {
        stageManager.showWindow(FxmlView.LOGIN);
        stageManager.closeStageButton(this.loginButton);
    }
}
