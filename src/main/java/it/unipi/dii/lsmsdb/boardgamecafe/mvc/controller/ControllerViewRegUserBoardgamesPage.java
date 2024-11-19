package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.BoardgameListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.BoardgameDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.BoardgameDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.BoardgameService;
import it.unipi.dii.lsmsdb.boardgamecafe.services.ReviewService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ControllerViewRegUserBoardgamesPage implements Initializable {

    @FXML
    private Button boardgamesCollectionButton;
    @FXML
    private Button boardgamePostsButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button previousButton;
    @FXML
    private Button searchUserButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button yourProfileButton;
    @FXML
    private Button accountInfoButton;
    @FXML
    private Button clearFieldButton;
    @FXML
    private Button newBoardgameButton;
    @FXML
    private TextField textFieldSearch;
    @FXML
    private GridPane boardgameGridPane;
    @FXML
    private ScrollPane scrollSet;
    @FXML
    private ListView listViewBoardgames;
    @FXML
    private Button onClickRefreshButton;
    @FXML
    private ChoiceBox<String> whatBgameToShowChoiceBox;
    @FXML
    private ComboBox cboxYear;

    @Autowired
    private BoardgameDBMongo boardgameDBMongo;
    @Autowired
    private BoardgameService boardgameService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ControllerObjectBoardgame controllerObjectBoardgame;
    @Autowired
    private ModelBean modelBean;
    private final StageManager stageManager;

    //Boardgame Variables
    private ObservableList<BoardgameModelMongo> boardgames = FXCollections.observableArrayList();
    private BoardgameListener boardgameListener;

    private UserModelMongo currentUser;

    //Utils Variables
    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;
    private final static int SKIP = 12; //how many boardgame to skip per time
    private final static int LIMIT = 12; //how many boardgame to show for each page
    private final static Logger logger = LoggerFactory.getLogger(BoardgameDBMongo.class);

    //• Show the boardgame that has the highest average score in its reviews. -----> Top rated boardgames
    //• Show Boardgames with the highest average score in its reviews per year. --> Top rated boardgames per Year
    //• Suggerisci Boardgame su cui hanno fatto post utenti che segui ---> Boargames commentati da utenti che segui

    private ObservableList<String> whatBgameToShowList = FXCollections.observableArrayList(
            "All bordgames",
            "Boardgames commented by followed users",
            "Top rated Boardgames per year",
            "Boardgames group by category"
    );

    private enum BgameToFetch {
        ALL_BOARDGAMES,
        BOARDGAME_COMMENTED_BY_FOLLOWERS,
        TOP_RATED_BOARDGAMES_PER_YEAR,
        BOARDGAME_GROUP_BY_CATEGORY
    };
    private BgameToFetch currentlyShowing;

    private static LinkedHashMap<BoardgameModelMongo, Double> topRatedBoardgamePairList; // Hash <gioco, Rating>

    @Autowired
    @Lazy
    public ControllerViewRegUserBoardgamesPage(StageManager stageManager) {
        this.stageManager = stageManager;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        topRatedBoardgamePairList = new LinkedHashMap<>(); // Con la linkedHM, viene preservato l'ordine di inserimento che è fondamentale!
        this.boardgamesCollectionButton.setDisable(true);
        this.previousButton.setDisable(true);
        this.nextButton.setDisable(true);

        this.currentlyShowing = BgameToFetch.ALL_BOARDGAMES;
        this.whatBgameToShowChoiceBox.setValue(this.whatBgameToShowList.get(0));
        this.whatBgameToShowChoiceBox.setItems(this.whatBgameToShowList);

        this.whatBgameToShowChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            this.currentlyShowing = BgameToFetch.values()[this.whatBgameToShowList.indexOf(newValue)];
            // Solamente se hai selezionato TOP_RATED... mostra la listViewYears
            initPage();
        });

        ObservableList<Integer> yearsToShow = FXCollections.observableArrayList();
        for (int i = LocalDate.now().getYear(); i >= 2000 ; i--)
            yearsToShow.add(i);

        this.cboxYear.setItems(yearsToShow);
        this.cboxYear.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            if (!(newValue instanceof Integer))
                return;
            int selectedYear = (int)newValue;
            topRatedBoardgamePairList = this.reviewService.getTopRatedBoardgamePerYear(5, 4, selectedYear);
            //topRatedBoardgamePairList.keySet().forEach(key -> System.out.println("Chiave --> " + key.getBoardgameName()));
            initPage();
        });

        initPage();

        //pause.setOnFinished(event -> performSearch());
    }

    public void initPage()
    {
        resetPage();

        currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);

        if (currentUser != null && currentUser.get_class().equals("admin")){
            this.newBoardgameButton.setVisible(true);
        } else {
            this.newBoardgameButton.setVisible(false);
        }

        boardgames.addAll(getBoardgamesByChoice());
        // Come mostro nessun boardgame ??


        if (modelBean.getBean(Constants.BOARDGAME_LIST) == null )
        {
            List<String> boardgameNames = boardgameDBMongo.getBoardgameTags();
            modelBean.putBean(Constants.BOARDGAME_LIST, boardgameNames);
        }

        fillGridPane();
    }

    public void onClickBoardgamePosts(ActionEvent actionEvent) {
        stageManager.showWindow(FxmlView.REGUSERPOSTS);
        stageManager.closeStageButton(this.boardgamePostsButton);

    }

    public void onClickClearField() {
        this.textFieldSearch.clear();
    }


    @FXML
    void onClickNext() {
        //clear variables
        boardgameGridPane.getChildren().clear();
        boardgames.clear();

        //update the skipcounter
        skipCounter += SKIP;

        //retrieve boardgames
        boardgames.addAll(getBoardgamesByChoice());
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
        boardgames.addAll(getBoardgamesByChoice());
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
    private List<BoardgameModelMongo> getBoardgamesByChoice()
    {
        List<BoardgameModelMongo> boardgames = null;
        this.cboxYear.setVisible(false);
        switch (this.currentlyShowing)
        {
            case ALL_BOARDGAMES ->
                    boardgames = boardgameDBMongo.findRecentBoardgames(LIMIT, this.skipCounter);
            case TOP_RATED_BOARDGAMES_PER_YEAR ->
            {
                List<BoardgameModelMongo> finalBoardgames = new ArrayList<>();
                this.cboxYear.setVisible(true);
                this.topRatedBoardgamePairList.keySet().forEach(finalBoardgames::add);
                boardgames = finalBoardgames;
            }
            case BOARDGAME_COMMENTED_BY_FOLLOWERS ->
                    boardgames = boardgameService.suggestBoardgamesWithPostsByFollowedUsers(
                            ((UserModelMongo)modelBean.getBean(Constants.CURRENT_USER)).getUsername(), this.skipCounter);
            case BOARDGAME_GROUP_BY_CATEGORY -> {
                // 16/11/2024 -> Da cambiare?
                for (Integer i : this.boardgameDBMongo.getListOfPublishedYear())
                    System.out.println("Anno : " + String.valueOf(i));
                boardgames = new ArrayList<>();
            }
            default ->
                boardgames = null;
        }
        if (boardgames == null)
            return null;

        prevNextButtonsCheck(boardgames);
        for(BoardgameModelMongo b : boardgames)
            System.out.println("gioco -> " + b.getBoardgameName());
        return boardgames;
    }

    private void loadViewMessageInfo(){
        Parent loadViewItem = stageManager.loadViewNode(FxmlView.INFOMSGBOARDGAMES.getFxmlFile());
        AnchorPane noBoardgamesFount = new AnchorPane();
        noBoardgamesFount.getChildren().add(loadViewItem);

        if (boardgames.isEmpty()){
            boardgameGridPane.getChildren().clear();
            boardgameGridPane.add(noBoardgamesFount, 0, 1);
        }
        GridPane.setMargin(noBoardgamesFount, new Insets(100, 200, 200, 350));
    }

    @FXML
    void fillGridPane() {
        columnGridPane = 0;
        rowGridPane = 1;
        //setGridPaneColumnAndRow();

        boardgameListener = (MouseEvent mouseEvent, BoardgameModelMongo boardgame) -> {
            // Logica per mostrare i dettagli del post usando StageManager
            modelBean.putBean(Constants.SELECTED_BOARDGAME, boardgame);
            stageManager.showWindow(FxmlView.BOARDGAME_DETAILS);
        };

        if (boardgames.isEmpty()){
            loadViewMessageInfo();
            return;
        }
        try {
            for (BoardgameModelMongo boardgame : boardgames) {

                Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTBOARDGAME.getFxmlFile());

                AnchorPane anchorPane = new AnchorPane();
                anchorPane.setId(boardgame.getId()); // the ancorPane-id is the boardgame _id.

                anchorPane.getChildren().add(loadViewItem);
                Double ratingForThisGame = null;
                if (this.currentlyShowing == BgameToFetch.TOP_RATED_BOARDGAMES_PER_YEAR)
                    ratingForThisGame = this.topRatedBoardgamePairList.get(boardgame);
                controllerObjectBoardgame.setData(boardgame, boardgameListener, anchorPane, ratingForThisGame);
                controllerObjectBoardgame.anchorPane.setId(boardgame.getId()); // the ancorPane-id is the boardgame _id.
                anchorPane.setOnMouseClicked(event -> {
                    this.boardgameListener.onClickBoardgameListener(event, boardgame);
                });

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

    public static Double getBgameRating(BoardgameModelMongo bgame)
    {
        return topRatedBoardgamePairList.get(bgame);
    }

    public void onClickLogout(ActionEvent event) {
        stageManager.showWindow(FxmlView.WELCOMEPAGE);
        stageManager.closeStageButton(this.logoutButton);
    }

    public void onClickYourProfile(ActionEvent event) {
        stageManager.showWindow(FxmlView.USERPROFILEPAGE);
        stageManager.closeStageButton(this.yourProfileButton);
    }

    public void onClickAccountInfoButton(ActionEvent event) {
        stageManager.showWindow(FxmlView.ACCOUNTINFOPAGE);
        stageManager.closeStageButton(this.accountInfoButton);
    }

    public void onClickSearchUserButton(ActionEvent event) {
        stageManager.showWindow(FxmlView.SEARCHUSER);
        stageManager.closeStageButton(this.searchUserButton);
    }


    public void onMouseClickedListView()
    {
        listViewBoardgames.setVisible(false);
        String selectedSearchTag = listViewBoardgames.getSelectionModel().getSelectedItem().toString();
        textFieldSearch.setText(selectedSearchTag);
        handleChoiceBoardgame(selectedSearchTag);
    }

    public void onClickRefreshButton()
    {
        initPage();
    }

    public void onKeyTypedSearchBar()
    {
        String searchString = textFieldSearch.getText();

        if (searchString.isEmpty()) {
            listViewBoardgames.setVisible(false);
        } else {
            listViewBoardgames.setVisible(true);
        }
        ObservableList<String> tagsContainingSearchString = FXCollections.observableArrayList(
                ((List<String>)modelBean.getBean(Constants.BOARDGAME_LIST)).stream()
                        .filter(tag -> tag.toLowerCase().contains(searchString.toLowerCase())).toList());
        System.out.println("[DEBUG] filtered tag list size: " + tagsContainingSearchString.size());

        listViewBoardgames.setItems(tagsContainingSearchString);
        int LIST_ROW_HEIGHT = 24;
        if (tagsContainingSearchString.size() > 10) {
            listViewBoardgames.setPrefHeight(10 * LIST_ROW_HEIGHT + 2);
        } else if (tagsContainingSearchString.isEmpty()){
            listViewBoardgames.setVisible(false);
        } else {
            listViewBoardgames.setPrefHeight(tagsContainingSearchString.size() * LIST_ROW_HEIGHT + 2);
        }

        // Highlight matching search substring in result strings
        listViewBoardgames.setCellFactory(boardgameResult -> new ListCell<String>() {
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

    public void handleChoiceBoardgame(String boardgameName)
    {
        System.out.println("Scelto -> " + boardgameName);
    }

    public void onClickNewBoardgameButton(){

    }

}
