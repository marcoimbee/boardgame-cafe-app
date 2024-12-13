package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.BoardgameListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.BoardgameDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.services.BoardgameService;
import it.unipi.dii.lsmsdb.boardgamecafe.services.ReviewService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class ControllerViewRegUserBoardgamesPage implements Initializable {

    @FXML
    private Button applyFilterButton;
    @FXML
    private TextField limitFilter;
    @FXML
    private Label limitFilterLabel;
    @FXML
    private Label adminInfoLabel;
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
    private Button refreshButton;
    @FXML
    private Button statisticsButton;
    @FXML
    private Button logoutButton;
    @FXML
    private ChoiceBox<String> whatBgameToShowChoiceBox;
    @FXML
    private ComboBox cboxYear;
    @FXML
    private ComboBox cboxCategory;

    @Autowired
    private BoardgameDBMongo boardgameDBMongo;
    @Autowired
    private PostDBMongo postDBMongo;
    @Autowired
    private BoardgameService boardgameService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ControllerObjectBoardgame controllerObjectBoardgame;
    @Autowired
    private ModelBean modelBean;
    @Autowired
    private ControllerObjectCreateBoardgame controllerCreateBoardgame;
    private final StageManager stageManager;

    //Boardgame Variables
    private ObservableList<BoardgameModelMongo> boardgames = FXCollections.observableArrayList();
    private BoardgameListener boardgameListener;

    List<String> boardgameNames;
    private GenericUserModelMongo currentUser;

    //Utils Variables
    private int columnGridPane = 0;
    private int rowGridPane = 1; // era 0
    private int skipCounter = 0;
    private final static int SKIP = 12; //how many boardgame to skip per time
    private final static int LIMIT = 12; //how many boardgame to show for each page
    private ObservableList<String> whatBgameToShowList;

    private final List<String> availableUserQueries = Arrays.asList(
            "All boardgames",
            "Boardgames posted by followed users",
            "Top rated boardgames per year",
            "Boardgames by category"
    );

    private final List<String> availableAdminQueries = Arrays.asList(
            "All boardgames",
            "Top rated boardgames per year",
            "Boardgames by category",
            "ADMIN: most posted and commented boardgames"
    );

    private enum BgameToFetch {
        ALL_BOARDGAMES,
        BOARDGAME_POSTED_BY_FOLLOWERS,
        TOP_RATED_BOARDGAMES_PER_YEAR,
        BOARDGAME_GROUP_BY_CATEGORY,
        SEARCH_BOARDGAME,
        ADMIN_MOST_COMMENTED_AND_POSTED_BOARDGAME;
    };
    private BgameToFetch currentlyShowing;
    private static LinkedHashMap<BoardgameModelMongo, Double> topRatedBoardgamePairList; // Hash <gioco, Rating>

    static class TableData {
        private final SimpleStringProperty boardgameName;
        private final SimpleStringProperty postCount;
        private final SimpleStringProperty commentCount;
        public TableData(String boardgameName, int postCount, int commentCount) {
            this.boardgameName = new SimpleStringProperty(boardgameName);
            this.postCount = new SimpleStringProperty(String.valueOf(postCount));
            this.commentCount = new SimpleStringProperty(String.valueOf(commentCount));
        }
        public SimpleStringProperty boardgameNameProperty() { return boardgameName; };
        public SimpleStringProperty postCountProperty() { return postCount; };
        public SimpleStringProperty commentCountProperty() { return commentCount; };
    }

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

        currentUser = (GenericUserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        if (!currentUser.get_class().equals("admin")){
            currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
            this.newBoardgameButton.setVisible(false);
            this.statisticsButton.setVisible(false);

            // Setting up available regular user queries
            whatBgameToShowList = FXCollections.observableArrayList(availableUserQueries);
        } else {
            currentUser = (AdminModelMongo) modelBean.getBean(Constants.CURRENT_USER);
            this.newBoardgameButton.setVisible(true);
            this.yourProfileButton.setVisible(false);

            // Setting up available admin queries
            whatBgameToShowList = FXCollections.observableArrayList(availableAdminQueries);
        }

        whatBgameToShowChoiceBox.setItems(whatBgameToShowList);
        this.whatBgameToShowChoiceBox.setValue(this.whatBgameToShowList.get(0));

        this.whatBgameToShowChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateCurrentlyShowing(newValue);
            this.textFieldSearch.clear();
            initPage();
        });

        ObservableList<Integer> yearsToShow = FXCollections.observableArrayList();
        for (int i = LocalDate.now().getYear(); i >= 2000; i--)
            yearsToShow.add(i);

        this.cboxYear.setItems(yearsToShow);
        this.cboxYear.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            this.currentlyShowing = BgameToFetch.TOP_RATED_BOARDGAMES_PER_YEAR;
            this.textFieldSearch.clear();
            if (!(newValue instanceof Integer))
                return;
            int selectedYear = (int) newValue;
            topRatedBoardgamePairList = this.reviewService.getTopRatedBoardgamePerYear(5, 4, selectedYear);
            initPage();
        });

        this.cboxCategory.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            this.currentlyShowing = BgameToFetch.BOARDGAME_GROUP_BY_CATEGORY;
            this.textFieldSearch.clear();
            if (!(newValue instanceof String))
                return;
            initPage();
        });

        initPage();

        // Page focus listener - needed to potentially update UI when coming back from a post detail window
        boardgameGridPane.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.focusedProperty().addListener((observableFocus, wasFocused, isNowFocused) -> {
                    if (isNowFocused) {
                        // After gaining focus for a post details window closing,
                        // UI needs to be potentially updated
                        onRegainPageFocusAfterDetailsBoardgameWindowClosing();
                    }
                });
            }
        });
    }

    private void updateCurrentlyShowing(String choiceBoxValue) {
        if (currentUser.get_class().equals("admin")) {
            // Setting up admin queries correspondence
            if (choiceBoxValue.equals(whatBgameToShowList.get(0)))      currentlyShowing = BgameToFetch.ALL_BOARDGAMES;
            else if (choiceBoxValue.equals(whatBgameToShowList.get(1))) currentlyShowing = BgameToFetch.TOP_RATED_BOARDGAMES_PER_YEAR;
            else if (choiceBoxValue.equals(whatBgameToShowList.get(2))) currentlyShowing = BgameToFetch.BOARDGAME_GROUP_BY_CATEGORY;
            else if (choiceBoxValue.equals(whatBgameToShowList.get(3))) currentlyShowing = BgameToFetch.ADMIN_MOST_COMMENTED_AND_POSTED_BOARDGAME;
        } else {
            // Setting up regular user queries correspondence
            if (choiceBoxValue.equals(whatBgameToShowList.get(0)))      currentlyShowing = BgameToFetch.ALL_BOARDGAMES;
            else if (choiceBoxValue.equals(whatBgameToShowList.get(1))) currentlyShowing = BgameToFetch.BOARDGAME_POSTED_BY_FOLLOWERS;
            else if (choiceBoxValue.equals(whatBgameToShowList.get(2))) currentlyShowing = BgameToFetch.TOP_RATED_BOARDGAMES_PER_YEAR;
            else if (choiceBoxValue.equals(whatBgameToShowList.get(3))) currentlyShowing = BgameToFetch.BOARDGAME_GROUP_BY_CATEGORY;
        }
    }


    public void initPage() {
        resetPage();

        // User is admin and he comes from the analytics page OR he clicked on the admin query option
        ControllerViewStatisticsPage.statisticsToShow showMostPostedAndCommentedBGAnalytic =
                (ControllerViewStatisticsPage.statisticsToShow) modelBean.getBean(Constants.SELECTED_ANALYTICS);
        if (showMostPostedAndCommentedBGAnalytic != null || currentlyShowing == BgameToFetch.ADMIN_MOST_COMMENTED_AND_POSTED_BOARDGAME) {
            modelBean.putBean(Constants.SELECTED_ANALYTICS, null);
            whatBgameToShowChoiceBox.setValue(whatBgameToShowList.get(whatBgameToShowList.size() - 1));
            currentlyShowing = BgameToFetch.ADMIN_MOST_COMMENTED_AND_POSTED_BOARDGAME;

            Document fetchedResults = postDBMongo.findMostPostedAndCommentedTags(10);
            displayMostPostedBoardgames(fetchedResults);
            setAdminQueryFiltersVisibility(true);
        } else {
            boardgames.addAll((List<BoardgameModelMongo>) getBoardgamesByChoice(null));
            fillGridPane();
            setAdminQueryFiltersVisibility(false);
        }

        if (modelBean.getBean(Constants.BOARDGAME_LIST) == null ) {
            boardgameNames = boardgameDBMongo.getBoardgameTags();
            modelBean.putBean(Constants.BOARDGAME_LIST, boardgameNames);
        }
    }

    private void onRegainPageFocusAfterDetailsBoardgameWindowClosing() {
        // Update UI after potentially having deleted a Boardgame
        String deletedBoardgameName = (String) modelBean.getBean(Constants.DELETED_BOARDGAME);
        if (deletedBoardgameName != null) {
            boardgameNames.remove(deletedBoardgameName);
            modelBean.putBean(Constants.BOARDGAME_LIST, boardgameNames);
            modelBean.putBean(Constants.DELETED_BOARDGAME, null);  // Deleting bean for consistency
            boardgames.removeIf(boardgame -> boardgame.getBoardgameName().equals(deletedBoardgameName));
            currentlyShowing = BgameToFetch.ALL_BOARDGAMES;
            viewCurrentlyShowing();
        }

        // Update UI after potentially having updated a Boardgame
        BoardgameModelMongo updatedBoardgame = (BoardgameModelMongo) modelBean.
                                                getBean(Constants.UPDATED_BOARDGAME);
        if (updatedBoardgame != null) {
            boardgames.removeIf(boardgame -> boardgame.getBoardgameName().
                                             equals(updatedBoardgame.getBoardgameName()));
            boardgames.add(updatedBoardgame);
            currentlyShowing = BgameToFetch.ALL_BOARDGAMES;
            viewCurrentlyShowing();
        }
    }

    public void onClickStatisticsButton() {
        stageManager.switchScene(FxmlView.STATISTICS);
    }

    public void onClickBoardgamePosts() {
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
        boardgames.addAll((List<BoardgameModelMongo>) getBoardgamesByChoice(null));
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
        boardgames.addAll((List<BoardgameModelMongo>) getBoardgamesByChoice(null));
        //put all boardgames in the Pane
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    void prevNextButtonsCheck(List<BoardgameModelMongo> boardgames){
        if (boardgames.size() > 0) {
            if (boardgames.size() < LIMIT) {
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
            }  else {
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
        scrollSet.setVvalue(0);
    }

    private Object getBoardgamesByChoice(Integer adminLimitResults) {
        this.newBoardgameButton.setDisable(false);
        setDisablePreviousNextRefresh(false);
        List<BoardgameModelMongo> boardgames = null;

        switch (this.currentlyShowing) {
            case ALL_BOARDGAMES -> {
                boardgames = boardgameDBMongo.findRecentBoardgames(LIMIT, this.skipCounter);
                this.cboxYear.setVisible(false);
                this.cboxCategory.setVisible(false);
                this.clearFieldButton.setDisable(false);
                this.textFieldSearch.setDisable(false);
            }
            case TOP_RATED_BOARDGAMES_PER_YEAR -> {
                List<BoardgameModelMongo> finalBoardgames = new ArrayList<>();
                this.cboxYear.setVisible(true);
                this.cboxCategory.setVisible(false);
                this.clearFieldButton.setDisable(false);
                this.textFieldSearch.setDisable(false);
                this.topRatedBoardgamePairList.keySet().forEach(finalBoardgames::add);
                boardgames = finalBoardgames;
            }
            case BOARDGAME_POSTED_BY_FOLLOWERS -> {
                boardgames = boardgameService.suggestBoardgamesWithPostsByFollowedUsers(
                        ((UserModelMongo) modelBean.getBean(Constants.CURRENT_USER)).getUsername(), this.skipCounter);
                this.cboxCategory.setVisible(false);
                this.cboxYear.setVisible(false);
            }
            case BOARDGAME_GROUP_BY_CATEGORY -> {
                if (this.cboxCategory.getItems().isEmpty()) {
                    ObservableList<String> listOfCategories = FXCollections.observableArrayList(this.boardgameDBMongo.getBoardgamesCategoriest());
                    this.cboxCategory.setItems(listOfCategories);
                }
                this.cboxYear.setVisible(false);
                this.cboxCategory.setVisible(true);
                this.clearFieldButton.setDisable(false);
                this.textFieldSearch.setDisable(false);
                String selectedCategory = (String)this.cboxCategory.getValue();
                if (selectedCategory != null)
                    boardgames = this.boardgameDBMongo.findBoardgamesByCategory(selectedCategory, LIMIT, this.skipCounter);
            }
            case SEARCH_BOARDGAME -> {
                String searchString = textFieldSearch.getText();
                if (!searchString.isEmpty())
                    boardgames = boardgameDBMongo.findBoardgamesStartingWith(searchString, LIMIT, this.skipCounter);
            }
            case ADMIN_MOST_COMMENTED_AND_POSTED_BOARDGAME -> {
                int limitResults = adminLimitResults == null ? 10 : adminLimitResults;
                return postDBMongo.findMostPostedAndCommentedTags(limitResults);            // Returning right away, as we need an Object
            }
        }
        if (boardgames == null)
            return new ArrayList<>();

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

    private void setAdminQueryFiltersVisibility(boolean val) {
        this.applyFilterButton.setVisible(val);
        this.limitFilter.setVisible(val);
        this.limitFilterLabel.setVisible(val);
        this.adminInfoLabel.setVisible(val);
        if (val) {
            this.limitFilter.setText(String.valueOf(10));

            // Restrict limit filter to accept only integers
            TextFormatter<Integer> integerTextFormatter = new TextFormatter<>(change -> {
                if (change.getText().matches("\\d*")) {
                    return change;
                }
                return null;
            });
            this.limitFilter.setTextFormatter(integerTextFormatter);
        }
    }

    @FXML
    void fillGridPane() {
        columnGridPane = 0;
        rowGridPane = 1;

        boardgameListener = (MouseEvent mouseEvent, BoardgameModelMongo boardgame) -> {
            modelBean.putBean(Constants.SELECTED_BOARDGAME, boardgame);
            stageManager.showWindow(FxmlView.BOARDGAME_DETAILS);
        };

        if (boardgames.isEmpty()){
            loadViewMessageInfo();
        } else {
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
                    GridPane.setMargin(anchorPane, new Insets(20, 20, 0, 25));

                }
            } catch (Exception e) {
                System.err.println("[ERROR] fillGridPane@ControllerRegUserBoardgamePage.java raised an exception: " + e.getMessage());
            }
        }
    }

    private void displayMostPostedBoardgames(Document fetchedResults) {
        boardgameGridPane.getChildren().clear();         // Removing old content
        refreshButton.setDisable(true);
        nextButton.setDisable(true);
        previousButton.setDisable(true);
        newBoardgameButton.setDisable(true);
        cboxYear.setVisible(false);
        cboxCategory.setVisible(false);
        textFieldSearch.setDisable(true);
        clearFieldButton.setDisable(true);

        columnGridPane = 1;
        rowGridPane = 1;

        ObservableList<TableData> mostPostedBoardgames = FXCollections.observableArrayList();
        List<Document> results = (List<Document>) fetchedResults.get("results");
        for (Document doc : results) {
            String boardgameName = doc.getString("tag");
            int postCount = doc.getInteger("postCount");
            int commentCount = doc.getInteger("commentCount");

            mostPostedBoardgames.add(new TableData(boardgameName, postCount, commentCount));
        }

        TableView<TableData> mostPostedBoardgamesTableView = createMostPostedBoardgamesTableView(mostPostedBoardgames);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(mostPostedBoardgamesTableView);
        BorderPane.setMargin(mostPostedBoardgamesTableView, new Insets(0, 0, 0, 10));
        boardgameGridPane.add(borderPane, columnGridPane, rowGridPane);
    }

    private TableView<TableData> createMostPostedBoardgamesTableView(ObservableList<TableData> mostPostedBoardgames) {
        TableView<TableData> tableView = new TableView<>();

        int rowHeight = 35;
        int boardgameNameColumnWidth = 200;
        int postCountColumnWidth = 150;
        int commentCountColumnWidth = 200;

        TableColumn<TableData, String> boardgameNameColumn = new TableColumn<>("Boardgame");
        boardgameNameColumn.setCellValueFactory(cellData -> cellData.getValue().boardgameNameProperty());

        TableColumn<TableData, String> postCountColumn = new TableColumn<>("Post count");
        postCountColumn.setCellValueFactory(cellData -> cellData.getValue().postCountProperty());

        TableColumn<TableData, String> commentCountColumn = new TableColumn<>("Comment count");
        commentCountColumn.setCellValueFactory(cellData -> cellData.getValue().commentCountProperty());

        tableView.getColumns().addAll(boardgameNameColumn, postCountColumn, commentCountColumn);

        boardgameNameColumn.setPrefWidth(boardgameNameColumnWidth);
        boardgameNameColumn.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        postCountColumn.setPrefWidth(postCountColumnWidth);
        postCountColumn.setStyle("-fx-alignment: CENTER;");

        commentCountColumn.setPrefWidth(commentCountColumnWidth);
        commentCountColumn.setStyle("-fx-alignment: CENTER;");

        tableView.setLayoutX(10);           // Left padding
        tableView.setMinWidth(boardgameNameColumnWidth + postCountColumnWidth + commentCountColumnWidth + 10);

        if (!mostPostedBoardgames.isEmpty()) {
            tableView.setRowFactory(tv -> {
                TableRow<TableData> row = new TableRow<>();
                row.setPrefHeight(rowHeight);
                return row;
            });

            tableView.setItems(mostPostedBoardgames);
            tableView.setMinHeight((tableView.getItems().size() + 1) * rowHeight);
        } else {
            Label noResultsTablePlaceholder = new Label("No results to show with the specified filters.");
            tableView.setPlaceholder(noResultsTablePlaceholder);
        }

        return tableView;
    }

    public void onClickApplyFilterButton() {
        int limitResultsFilter = Integer.parseInt(this.limitFilter.getText());
        if (limitResultsFilter <= 0) {
            stageManager.showInfoMessage("Invalid filter values", "Choose a positive non-negative integer to limit results.");
            return;
        }

        Document fetchedResults = (Document) getBoardgamesByChoice(limitResultsFilter);
        displayMostPostedBoardgames(fetchedResults);
    }

    public static Double getBgameRating(BoardgameModelMongo bgame) {
        return topRatedBoardgamePairList.get(bgame);
    }

    public void onClickVBox() { hideListViewBoardgames(); }

    public void onClickLogout() {
        modelBean.putBean(Constants.CURRENT_USER, null);
        stageManager.switchScene(FxmlView.WELCOMEPAGE);
    }

    public void onClickLogout(ActionEvent event) {
        modelBean.putBean(Constants.CURRENT_USER, null);
        stageManager.switchScene(FxmlView.WELCOMEPAGE);
    }

    public void onClickYourProfile() {
        stageManager.showWindow(FxmlView.USERPROFILEPAGE);
        stageManager.closeStageButton(this.yourProfileButton);
    }

    public void onClickAccountInfoButton() {
        stageManager.showWindow(FxmlView.ACCOUNTINFOPAGE);
        stageManager.closeStageButton(this.accountInfoButton);
    }

    public void onClickSearchUserButton() {
        stageManager.showWindow(FxmlView.SEARCHUSER);
        stageManager.closeStageButton(this.searchUserButton);
    }

    private void hideListViewBoardgames() {listViewBoardgames.setVisible(false);}

    public void onMouseClickedListView() {
        hideListViewBoardgames();
        this.whatBgameToShowChoiceBox.setValue(this.whatBgameToShowList.get(0));
        this.cboxCategory.setVisible(false);
        this.cboxYear.setVisible(false);
        String selectedSearchTag = listViewBoardgames.getSelectionModel().getSelectedItem().toString();
        textFieldSearch.setText(selectedSearchTag);
        handleChoiceBoardgame(selectedSearchTag);
    }

    public void onClickRefreshButton() {
        initPage();
    }

    public void onKeyTypedSearchBar() {
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

    public void onClickNewBoardgameButton() {
        System.out.println("[INFO] Starting new boardgame creation procedure");
        try {
            this.whatBgameToShowChoiceBox.setDisable(true);
            this.newBoardgameButton.setDisable(true);
            this.cboxYear.setDisable(true);
            setDisablePreviousNextRefresh(true);
            // Load boardgame creation FXML
            Parent loadViewItem = stageManager.
                    loadViewNode(FxmlView.OBJECTCREATEBOARDGAME.getFxmlFile());

            // Recupera i controlli dall'interfaccia di creazione del boardgame
            TextField descriptionField = (TextField) loadViewItem.lookup("#descriptionTextField");
            TextField boardgameNameField = (TextField) loadViewItem.lookup("#boardgameNameTextField");
            TextField yearField = (TextField) loadViewItem.lookup("#yearOfPublicationTextField");
            TextField playingTimeField = (TextField) loadViewItem.lookup("#playingTimeTextField");
            TextField minPlayersField = (TextField) loadViewItem.lookup("#minPlayersTextField");
            TextField maxPlayersField = (TextField) loadViewItem.lookup("#maxPlayersTextField");
            TextField minAgeField = (TextField) loadViewItem.lookup("#minAgeTextField");
            TextField imageLinkField = (TextField) loadViewItem.lookup("#imageLinkTextField");
            TextField thumbnailLinkField = (TextField) loadViewItem.lookup("#thumbnailLinkTextField");

            Button uploadBoardgameButton = (Button) loadViewItem.lookup("#uploadButton");
            Button cancelBoardgameButton = (Button) loadViewItem.lookup("#cancelButton");

            // Imposta il comportamento del pulsante "Upload"
            uploadBoardgameButton.setOnAction(e -> {
                String description = descriptionField.getText();
                String name = boardgameNameField.getText();
                String imageLink = imageLinkField.getText();
                String thumbnailLink = thumbnailLinkField.getText();
                String year = yearField.getText();
                String playingTime = playingTimeField.getText();
                String minPlayers = minPlayersField.getText();
                String maxPlayers = maxPlayersField.getText();
                String minAge = minAgeField.getText();

                // Recupera le liste di categorie, designer e publisher
                List<String> categories = controllerCreateBoardgame.getCategories();
                List<String> designers = controllerCreateBoardgame.getDesigners();
                List<String> publishers = controllerCreateBoardgame.getPublishers();


                // Verifica che i campi numerici siano validi
                try {
                    int yearInt = Integer.parseInt(year);
                    int playingTimeInt = Integer.parseInt(playingTime);
                    int minPlayersInt = Integer.parseInt(minPlayers);
                    int maxPlayersInt = Integer.parseInt(maxPlayers);
                    int minAgeInt = Integer.parseInt(minAge);

                    // Se arriviamo qui, significa che tutti i campi numerici sono validi
                    addNewBoardgame(description, name, yearInt, playingTimeInt, minPlayersInt,
                                    maxPlayersInt, minAgeInt, imageLink, thumbnailLink,
                                    categories, designers, publishers);

                    removeBoardgameCreationPanel();
//                    fillGridPane();
//                    scrollSet.setVvalue(0);
//                    setDisablePreviousNextRefresh(false);
//                    prevNextButtonsCheck(boardgames);

                } catch (NumberFormatException ex) {
                    stageManager.showInfoMessage("ERROR",
                            "Please enter valid numbers for year, playing time, " +
                                    "min players, max players, and min age.");
                }
            });

             //Imposta il comportamento del pulsante "Cancel"
            cancelBoardgameButton.setOnAction(e -> {
                boolean discardBoardgame = stageManager.showDiscardBoardgameInfoMessage();
                if (discardBoardgame) {
                    this.cboxYear.setDisable(false);
                    this.newBoardgameButton.setDisable(false);
                    setDisablePreviousNextRefresh(false);
                    prevNextButtonsCheck(boardgames);
                    this.whatBgameToShowChoiceBox.setDisable(false);
                    removeBoardgameCreationPanel();
                    fillGridPane();
                    scrollSet.setVvalue(0);
                } else {
                    this.cboxYear.setDisable(true);
                    this.newBoardgameButton.setDisable(true);
                    setDisablePreviousNextRefresh(true);
                    this.whatBgameToShowChoiceBox.setDisable(true);
                }
            });

            // Aggiunge il pannello per creare un nuovo boardgame
            AnchorPane addBoardgameBox = new AnchorPane();
            addBoardgameBox.setId("newBoardgameBox");
            addBoardgameBox.getChildren().add(loadViewItem);

            if (!boardgames.isEmpty()){
                boardgameGridPane.getChildren().clear();
                boardgameGridPane.add(addBoardgameBox, 0, 1);
            } else {
                boardgameGridPane.add(addBoardgameBox, 0, rowGridPane);
            }
            GridPane.setMargin(addBoardgameBox, new Insets(5, 5, 15, 130));

        } catch (Exception e) {
            stageManager.showInfoMessage("INFO", "An error occurred while creating the boardgame. Try again in a while.");
            System.err.println("[ERROR] onClickNewBoardgameButton raised an exception: " + e.getMessage());
        }
    }

    private void removeBoardgameCreationPanel() {
        boardgameGridPane.getChildren().removeIf(elem -> {
            String elemId = elem.getId();
            if (elemId != null) {
                return elemId.equals("newBoardgameBox");
            }
            return false;
        });
    }

    private void addNewBoardgame(String description, String boardgameName, int yearPublished,
                                 int playingTime, int minPlayers, int maxPlayers, int minAge,
                                 String imageLink, String thumbnailLink, List<String> categories,
                                 List<String> designers, List<String> publishers) {

        if (boardgameName.isEmpty()) {
            stageManager.showInfoMessage("Error", "Boardgame Name Field Cannot Be Empty.");
            return;
        }
        if (description.isEmpty()) {
            stageManager.showInfoMessage("Error", "Description Field Cannot Be Empty.");
            return;
        }
        if (yearPublished == 0){
            stageManager.showInfoMessage("Error", "The Year Of Publication Cannot be 0 (Zero).");
            return;
        }
        if (playingTime == 0) {
            stageManager.showInfoMessage("Error", "The Playing Time Cannot be 0 (Zero).");
            return;
        }
        if (minPlayers == 0){
            stageManager.showInfoMessage("Error", "The Minimum # Players Cannot be 0 (Zero).");
            return;
        }
        if (maxPlayers == 0) {
            stageManager.showInfoMessage("Error", "The Maximum # Players Cannot be 0 (Zero).");
            return;
        }
        if (minAge == 0){
            stageManager.showInfoMessage("Error", "The Minimum Age Cannot be 0 (Zero).");
            return;
        }
        if (imageLink.isEmpty()) {
            stageManager.showInfoMessage("Error", "Image Link Field Cannot Be Empty.");
            return;
        }
        if (categories.isEmpty()){
            stageManager.showInfoMessage("Error", "Categories List Cannot Be Empty. At Least 1 Element");
            return;
        }

        BoardgameModelMongo newBoardgame = new BoardgameModelMongo(
                                                boardgameName, thumbnailLink,
                                                imageLink, description,
                                                yearPublished, minPlayers,
                                                maxPlayers, playingTime, minAge,
                                                categories, designers, publishers);

        boolean savedBoardgame = boardgameService.insertBoardgame(newBoardgame);   // MongoDB + Neo4J insertion

        if (savedBoardgame) {
            System.out.println("[INFO] New Boardgame added");
            handleSuccessfulBoardgameAddition(newBoardgame);
            this.newBoardgameButton.setDisable(false);
        } else {
            System.out.println("[INFO] An error occurred while adding a new boardgame");
            stageManager.showInfoMessage("INFO", "Failed to add boardgame. Please try again in a while.");
            fillGridPane();             // Restoring GridPane if anything went wrong
            scrollSet.setVvalue(0);
        }

    }

    private void handleSuccessfulBoardgameAddition(BoardgameModelMongo newlyInsertedBoardgame) {
        if (currentlyShowing == BgameToFetch.ALL_BOARDGAMES) {
            stageManager.showInfoMessage("Success", "Your Boardgame has been added successfully!");
            boardgames.remove(boardgames.size() - 1); //removes the last item by temporarily resizing the boardgames list
            boardgames.add(0, newlyInsertedBoardgame);  //adds boardgame at top and the size of the list is restored
            fillGridPane();
            prevNextButtonsCheck(boardgames);
            scrollSet.setVvalue(0);
        } else {
            stageManager.showInfoMessage("Success", "Your Boardgame has been added successfully! You're being redirected to the 'All Boardgames' page.");
            currentlyShowing = BgameToFetch.ALL_BOARDGAMES;          // get back to ALL_POSTS page, show the new post first
            whatBgameToShowChoiceBox.setValue(whatBgameToShowList.get(0));  // Setting string inside choice box
            viewCurrentlyShowing();
        }
        boardgameNames.add(newlyInsertedBoardgame.getBoardgameName());
        modelBean.putBean(Constants.BOARDGAME_LIST, boardgameNames);
        setDisablePreviousNextRefresh(false);
        whatBgameToShowChoiceBox.setDisable(false);
    }

    public void viewCurrentlyShowing() {
        resetPage();
        List<BoardgameModelMongo> retrievedBoardgames = (List<BoardgameModelMongo>) getBoardgamesByChoice(null);
        boardgames.addAll(retrievedBoardgames);            // Add new LIMIT posts (at most)
        fillGridPane();
        prevNextButtonsCheck(retrievedBoardgames);            // Initialize buttons
    }

    public void setDisablePreviousNextRefresh(boolean isVisible){
        this.previousButton.setDisable(isVisible);
        this.nextButton.setDisable(isVisible);
        this.refreshButton.setDisable(isVisible);
    }
}
