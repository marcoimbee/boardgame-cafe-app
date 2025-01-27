package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.BoardgameListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.BoardgameDBNeo4j;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
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
    private TextField textFieldSearch;
    @FXML
    private Button loginButton;
    @FXML
    private GridPane boardgameGridPane;
    @FXML
    private ScrollPane scrollSet;

    @Autowired
    private BoardgameDBNeo4j boardgameDBNeo4j;
    @Autowired
    private ControllerObjectBoardgame controllerObjectBoardgame;

    private final StageManager stageManager;
    private List<BoardgameModelNeo4j> boardgames = new ArrayList<>();
    private BoardgameListener boardgameListener;

    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;
    private final static int SKIP = 12;     // How many boardgame to skip per time
    private final static int LIMIT = 12;    // How many boardgame to show for each page

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
            stageManager.showInfoMessage("INFO", "The database is empty!");
            try {
                Platform.exit();
                System.exit(0);
            } catch (Exception e) {
                System.err.println("[ERROR] initialize()@ControllerViewGuestBoardgamesPage.java raised an exception: " + e.getMessage());
            }
        }
        fillGridPane();
    }

    public void onClickBoardgamePosts() {
        stageManager.showWindow(FxmlView.GUESTPOSTS);
        stageManager.closeStageButton(this.boardgamePostsButton);
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
        stageManager.showWindow(FxmlView.WELCOMEPAGE);
        stageManager.closeStageButton(this.returnWPageButton);
    }

    @FXML
    void onClickNext() {
        boardgameGridPane.getChildren().clear();
        boardgames.clear();
        skipCounter += SKIP;
        boardgames.addAll(getData());
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    @FXML
    void onClickPrevious() {
        boardgameGridPane.getChildren().clear();
        boardgames.clear();
        skipCounter -= SKIP;
        boardgames.addAll(getData());
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    void prevNextButtonsCheck(int boardgamesNumber) {
        if (boardgamesNumber > 0) {
            if (boardgamesNumber < LIMIT) {
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

    void resetPage() {
        boardgameGridPane.getChildren().clear();
        boardgames.clear();
        skipCounter = 0;
    }

    private List<BoardgameModelNeo4j> getData() {
        List<BoardgameModelNeo4j> boardgames =
                boardgameDBNeo4j.findRecentBoardgames(LIMIT, skipCounter);

        prevNextButtonsCheck(boardgames.size());
        return boardgames;
    }

    @FXML
    void fillGridPane() {
        columnGridPane = 0;
        rowGridPane = 1;

        boardgameListener = (MouseEvent mouseEvent, String boardgameId) -> {
            String title = "Content access permissions";
            String message = "\t\t\tCurious to view the details of this boardgame?\n" +
                             "\t\t\nSign-up using the button on the left side panel to do this and much more.";
            stageManager.showInfoMessage(title, message);
        };

        try {
            for (BoardgameModelNeo4j boardgame : boardgames) {
                Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTBOARDGAME.getFxmlFile());

                AnchorPane anchorPane = new AnchorPane();
                anchorPane.getChildren().add(loadViewItem);

                controllerObjectBoardgame.setData(boardgame, boardgameListener, anchorPane, null);

                anchorPane.setOnMouseClicked(event ->{
                    this.boardgameListener.onClickBoardgameListener(event,boardgame.getId());} );

                if (columnGridPane == 4) {
                    columnGridPane = 0;
                    rowGridPane++;
                }

                boardgameGridPane.add(anchorPane, columnGridPane++, rowGridPane);
                boardgameGridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
                boardgameGridPane.setPrefWidth(430);
                boardgameGridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);
                boardgameGridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
                boardgameGridPane.setPrefHeight(300);
                boardgameGridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
                GridPane.setMargin(anchorPane, new Insets(22));
            }
        } catch (Exception e) {
            System.err.println("[ERROR] fillGridPane()@ControllerViewGuestBoardgamesPage.java raised an exception: " + e.getMessage());
            stageManager.showInfoMessage("INFO", "Something went wrong. Please try again in a while.");
        }
    }

    public void onClickLogin() {
        stageManager.showWindow(FxmlView.LOGIN);
        stageManager.closeStageButton(this.loginButton);
    }
}
