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
        BOARDGAME_GROUP_BY_CATEGORY,
        SEARCH_BOARDGAME
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
            this.textFieldSearch.clear();
            initPage();
        });

        ObservableList<Integer> yearsToShow = FXCollections.observableArrayList();
        for (int i = LocalDate.now().getYear(); i >= 2000 ; i--)
            yearsToShow.add(i);

        this.cboxYear.setItems(yearsToShow);
        this.cboxYear.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            this.currentlyShowing = BgameToFetch.TOP_RATED_BOARDGAMES_PER_YEAR;
            this.textFieldSearch.clear();
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

//        BoardgameModelMongo b = new BoardgameModelMongo("Monopoli", "thumbnail", "data:image/jpeg;base64,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"
//        ,"Gioco copia", 2000, 2, 8, 2, 4, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
//        boardgameService.insertBoardgame(b);

        currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);

        if (currentUser != null && currentUser.get_class().equals("admin")){
            this.newBoardgameButton.setVisible(true);
        } else {
            this.newBoardgameButton.setVisible(false);
        }

        boardgames.addAll(getBoardgamesByChoice());


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
        hideListViewBoardgames();
    }


    @FXML
    void onClickNext() {
        hideListViewBoardgames();
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
        hideListViewBoardgames();
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
        hideListViewBoardgames();
        boardgameGridPane.getChildren().clear();
        boardgames.clear();
        skipCounter = 0;
    }
    private List<BoardgameModelMongo> getBoardgamesByChoice()
    {
        List<BoardgameModelMongo> boardgames = null;
        switch (this.currentlyShowing)
        {
            case ALL_BOARDGAMES -> {
                boardgames = boardgameDBMongo.findRecentBoardgames(LIMIT, this.skipCounter);
                this.cboxYear.setVisible(false);
            }
            case TOP_RATED_BOARDGAMES_PER_YEAR ->
            {
                List<BoardgameModelMongo> finalBoardgames = new ArrayList<>();
                this.cboxYear.setVisible(true);
                this.topRatedBoardgamePairList.keySet().forEach(finalBoardgames::add);
                boardgames = finalBoardgames;
            }
            case BOARDGAME_COMMENTED_BY_FOLLOWERS -> {
                boardgames = boardgameService.suggestBoardgamesWithPostsByFollowedUsers(
                        ((UserModelMongo) modelBean.getBean(Constants.CURRENT_USER)).getUsername(), this.skipCounter);
            }
            case BOARDGAME_GROUP_BY_CATEGORY -> {
                // 16/11/2024 -> Da cambiare?
                for (Integer i : this.boardgameDBMongo.getListOfPublishedYear())
                    System.out.println("Anno : " + String.valueOf(i));
                boardgames = new ArrayList<>();
            }
            case SEARCH_BOARDGAME -> {
                String searchString = textFieldSearch.getText();
                if (searchString.isEmpty())
                    return null;
                boardgames = boardgameDBMongo.findBoardgamesStartingWith(searchString, LIMIT, this.skipCounter);
            }
        }
        if (boardgames == null)
            return null;

        prevNextButtonsCheck(boardgames);
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

    public void onClickVBox() { hideListViewBoardgames(); }
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

    private void hideListViewBoardgames() {listViewBoardgames.setVisible(false);}

    public void onMouseClickedListView()
    {
        hideListViewBoardgames();
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
            hideListViewBoardgames();
        } else {
            listViewBoardgames.setVisible(true);
        }
        ObservableList<String> tagsContainingSearchString = FXCollections.observableArrayList(
                ((List<String>)modelBean.getBean(Constants.BOARDGAME_LIST)).stream()
                        .filter(tag -> tag.toLowerCase().contains(searchString.toLowerCase())).toList());

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
        this.currentlyShowing = BgameToFetch.SEARCH_BOARDGAME;
        this.initPage();
    }

    public void onClickNewBoardgameButton(){

    }

}
