package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.UserListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.AdminModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.services.UserService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.bson.Document;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Component
public class ControllerViewSearchUserPage implements Initializable {
    @FXML
    private Label adminInfoLabel;
    @FXML
    private Button applyFilterButton;
    @FXML
    private Label startDateFilterLabel;
    @FXML
    private Label endDateFilterLabel;
    @FXML
    private Label limitFilterLabel;
    @FXML
    private DatePicker endDateFilter;
    @FXML
    private DatePicker startDateFilter;
    @FXML
    private TextField limitFilter;


    @FXML
    private ListView searchResultsList;
    @FXML
    private Button boardgamesButton;
    @FXML
    private Button postsFeedButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button previousButton;
    @FXML
    private Button clearFieldButton;
    @FXML
    private Button searchUserButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button yourProfileButton;
    @FXML
    private Button accountInfoButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button statisticsButton;
    @FXML
    private ChoiceBox<String> whatUsersToShowChoiceBox;
    @FXML
    private TextField textFieldSearch;
    @FXML
    private GridPane usersGridPane;
    @FXML
    private ScrollPane scrollSet;
    @Autowired
    private UserDBMongo userDBMongo;
    @Autowired
    private UserService userService;
    @Autowired
    private ControllerObjectUser controllerObjectUser;
    @Autowired
    private ModelBean modelBean;
    private final StageManager stageManager;
    UserListener userListener;

    // Choice box variables
    ObservableList<String> whatUsersToShowList = FXCollections.observableArrayList(
            "All users",
            "Users which posted about boardgames you posted about too",  // suggestUsersByCommonBoardgamePosted@UserService
            "Users that enjoy the same posts as you do",  // suggestUsersByCommonLikedPosts@UserService
            "Influencers in the boardgame community"   // suggestInfluencerUsers@UserService
    );

    private List<UserModelMongo> users = new ArrayList<>();

    private List<GenericUserModelMongo> bannedUsers = new ArrayList<>();

    //Utils Variables
    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;            // Ho many times the user clicked on the 'Next' button
    private final static int SKIP = 10;     // How many users to skip each time
    private final static int LIMIT = 10;    // How many users to show in each page

    private enum UsersToFetch {
        ALL_USERS,
        USERS_WITH_COMMON_BOARDGAMES_POSTED,
        USERS_WITH_COMMON_LIKED_POSTS,
        INFLUENCER_USERS,
        SEARCH_RESULTS,
        ADMIN_MOST_ACTIVE_USERS;
    };
    private static UsersToFetch currentlyShowing;       // Global indicator of what type of user is being shown on the page
    private static int currentPage;

    private static List<Integer> visitedPages;
    private static boolean visualizedLastUser;      // Keeps track of whether the user has reached the last reachable page or not;
    // Search functionalities

    private List<String> userUsernames;
    private static String selectedSearchUser;
    private static GenericUserModelMongo currentUser;

    static class TableData {

        private final SimpleStringProperty username;
        private final SimpleStringProperty reviewCount;
        private final SimpleStringProperty avgDateDiff;
        public TableData(String username, int reviewCount, double avgDateDiff) {
            this.username = new SimpleStringProperty(username);
            this.reviewCount = new SimpleStringProperty(String.valueOf(reviewCount));
            this.avgDateDiff = new SimpleStringProperty(String.valueOf(Math.floor(avgDateDiff)));
        }
        public SimpleStringProperty usernameProperty() { return username; };
        public SimpleStringProperty reviewCountProperty() { return reviewCount; };
        public SimpleStringProperty avgDateDiffProperty() { return avgDateDiff; };
    }

    @Autowired
    @Lazy
    public ControllerViewSearchUserPage(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = (GenericUserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        if (currentUser == null)
            throw new RuntimeException("No logged");

        currentUser = (GenericUserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        if (!currentUser.get_class().equals("admin")) {
            currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
            this.statisticsButton.setVisible(false);
        } else {
            currentUser = (AdminModelMongo) modelBean.getBean(Constants.CURRENT_USER);
            whatUsersToShowList.add("ADMIN: most active users");
            this.yourProfileButton.setVisible(false);

        }

        visitedPages = new ArrayList<>();

        this.searchUserButton.setDisable(true);
        this.previousButton.setDisable(true);
        this.nextButton.setDisable(true);
        resetPageVars();

        currentlyShowing = UsersToFetch.ALL_USERS;      // Static var init

        // Choice box init
        whatUsersToShowChoiceBox.setValue(whatUsersToShowList.get(0));
        whatUsersToShowChoiceBox.setItems(whatUsersToShowList);

        // Adding listeners to option selection: change indicator of what is displayed on the screen and retrieve results
        whatUsersToShowChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateCurrentlyShowing(newValue);
            onSelectChoiceBoxOption();
        });

        onSelectChoiceBoxOption();

        // Prefetch usernames for the search function and init search functionalities variables
        searchResultsList.setVisible(false);

        long startTime = System.currentTimeMillis();
        if (modelBean.getBean(Constants.USERS_USERNAMES) == null) {
            userUsernames = userDBMongo.getUserUsernames();       // Fetching usernames as soon as the page opens
            modelBean.putBean(Constants.USERS_USERNAMES, userUsernames);       // Saving them in the Bean, so they'll be always available from now on in the whole app
        } else {
            userUsernames = (List<String>) modelBean.getBean(Constants.USERS_USERNAMES);    // Obtaining usernames from the Bean, as thy had been put there before
        }
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("[INFO] Fetched " + userUsernames.size() + " user usernames in " + elapsedTime + " ms");
        selectedSearchUser = null;

        // Setting up admin tools - list of banned users, needed to set up ban/unban buttons
        if (currentUser.get_class().equals("admin")) {
            if (modelBean.getBean(Constants.BANNED_USERS_LIST) == null) {
                bannedUsers = userDBMongo.getBannedUsers();
                modelBean.putBean(Constants.BANNED_USERS_LIST, bannedUsers);
            } else {
                bannedUsers = (List<GenericUserModelMongo>) modelBean.getBean(Constants.BANNED_USERS_LIST);
            }
        }

        // Page focus listener - needed to potentially update UI when coming back from a user ban or delete operation by the admin
        usersGridPane.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.focusedProperty().addListener((observableFocus, wasFocused, isNowFocused) -> {
                    if (isNowFocused) {
                        // After gaining focus for user ban or delete window closing (admin-only operations),
                        // UI needs to be potentially updated
                        onRegainPageFocusAfterUserBanOrDeletion();
                    }
                });
            }
        });
    }

    private void onRegainPageFocusAfterUserBanOrDeletion() {
        // Potentially update UI after user deletion
        UserModelMongo deletedUser = (UserModelMongo) modelBean.getBean(Constants.DELETED_USER);
        if (deletedUser != null) {
            modelBean.putBean(Constants.DELETED_USER, null);
            onClickRefreshButton();
        }

        // Potentially update UI after user ban
        UserModelMongo bannedUser = (UserModelMongo) modelBean.getBean(Constants.BANNED_USER);
        if (bannedUser != null) {
            modelBean.putBean(Constants.BANNED_USER, null);
            onClickRefreshButton();
        }

        // Potentially update UI after user unban
        UserModelMongo unbannedUser = (UserModelMongo) modelBean.getBean(Constants.UNBANNED_USER);
        if (unbannedUser != null) {
            modelBean.putBean(Constants.UNBANNED_USER, null);
            onClickRefreshButton();
        }
    }

    private void updateCurrentlyShowing(String choiceBoxValue) {
        if (choiceBoxValue.equals(whatUsersToShowList.get(0))) {
            currentlyShowing = UsersToFetch.ALL_USERS;
        }

        if (choiceBoxValue.equals(whatUsersToShowList.get(1))) {
            currentlyShowing = UsersToFetch.USERS_WITH_COMMON_BOARDGAMES_POSTED;
        }

        if (choiceBoxValue.equals(whatUsersToShowList.get(2))) {
            currentlyShowing = UsersToFetch.USERS_WITH_COMMON_LIKED_POSTS;
        }

        if (choiceBoxValue.equals(whatUsersToShowList.get(3))) {
            currentlyShowing = UsersToFetch.INFLUENCER_USERS;
        }

        if (choiceBoxValue.equals(whatUsersToShowList.get(4))) {
            currentlyShowing = UsersToFetch.ADMIN_MOST_ACTIVE_USERS;
        }
    }

    public void onSelectChoiceBoxOption() {
        resetPageVars();
        selectedSearchUser = null;

        ControllerViewStatisticsPage.statisticsToShow showMostActiveUsersAnalytic =     // Checking if the admin is visiting this page coming from the analytics panel
                (ControllerViewStatisticsPage.statisticsToShow)modelBean.getBean(Constants.SELECTED_ANALYTICS);
        if (showMostActiveUsersAnalytic != null) {                  // User is admin and he comes from the analytics panel
            modelBean.putBean(Constants.SELECTED_ANALYTICS, null);
            currentlyShowing = UsersToFetch.ADMIN_MOST_ACTIVE_USERS;
            whatUsersToShowChoiceBox.setValue(whatUsersToShowList.get(whatUsersToShowList.size() - 1));
            Object fetchedResults = fetchUsers(null, null, null, null);
            if (fetchedResults instanceof Document) {
                System.out.println("[DEBUG] Displaying fetched results...");
                displayMostActiveUsers((Document) fetchedResults);
            }
            setAdminQueryFiltersVisibility(true);
        } else {                    // Normal page setup
            Object fetchedResults = fetchUsers(null, null, null, null);
            if (fetchedResults instanceof Document) {
                displayMostActiveUsers((Document) fetchedResults);
                setAdminQueryFiltersVisibility(true);
            } else {
                List<UserModelMongo> fetchedUsers = (List<UserModelMongo>) fetchedResults;
                users.addAll(fetchedUsers);            // Add new LIMIT users (at most)
                fillGridPane();
                prevNextButtonsCheck(fetchedUsers.size());            // Initialize buttons
            }
        }
    }

    public void onClickApplyFilterButton() {
        LocalDate startDateFilterLocaldate = this.startDateFilter.getValue();
        Date startDateFilter = Date.from(startDateFilterLocaldate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (startDateFilter.compareTo(new Date()) > 0) {
            stageManager.showInfoMessage("Invalid filter values", "Start date cannot be greater than the current date.");
            return;
        }

        LocalDate endDateFilterLocaldate = this.endDateFilter.getValue();
        Date endDateFilter = Date.from(endDateFilterLocaldate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (startDateFilter.compareTo(endDateFilter) > 0) {
            stageManager.showInfoMessage("Invalid filter values", "Start date cannot be greater than end date.");
            return;
        }

        int limitResultsFilter = Integer.parseInt(this.limitFilter.getText());
        if (limitResultsFilter <= 0) {
            stageManager.showInfoMessage("Invalid filter values", "Choose a positive non-negative integer to limit results.");
            return;
        }

        Document fetchedResults = (Document) fetchUsers(null, startDateFilter, endDateFilter, limitResultsFilter);
        displayMostActiveUsers(fetchedResults);
    }

    private void resetPageVars() {
        skipCounter = 0;
        users.clear();
        currentPage = 0;
        visitedPages.clear();
        visitedPages.add(0);
        visualizedLastUser = false;
        scrollSet.setVvalue(0);
        textFieldSearch.setText(null);
        setAdminQueryFiltersVisibility(false);
    }

    private void setAdminQueryFiltersVisibility(boolean val) {
        this.applyFilterButton.setVisible(val);
        this.startDateFilter.setVisible(val);
        this.startDateFilterLabel.setVisible(val);
        this.endDateFilter.setVisible(val);
        this.endDateFilterLabel.setVisible(val);
        this.limitFilter.setVisible(val);
        this.limitFilterLabel.setVisible(val);
        this.adminInfoLabel.setVisible(val);
        if (val) {
            this.startDateFilter.setValue(LocalDate.parse("01-01-2000", DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            this.endDateFilter.setValue((new Date()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
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

    public void onClickStatisticsButton() {
        stageManager.switchScene(FxmlView.STATISTICS);
    }

    public void onClickBoardgamesButton() {
        stageManager.showWindow(FxmlView.REGUSERBOARDGAMES);
        stageManager.closeStageButton(this.boardgamesButton);
    }

    public void onClickPostsFeedButton() {
        stageManager.showWindow(FxmlView.REGUSERPOSTS);
        stageManager.closeStageButton(this.postsFeedButton);
    }

    public void onClickClearField() {
        this.textFieldSearch.clear();           // When clearing the search box, we reset the view to make it show the default shown users
        currentlyShowing = UsersToFetch.ALL_USERS;
        onSelectChoiceBoxOption();
    }

    @FXML
    void onClickNext() {
        usersGridPane.getChildren().clear();

        List<UserModelMongo> retrievedUsers = new ArrayList<>();
        currentPage++;
        if (!visitedPages.contains(currentPage)) {
            // New users need to be retrieved from the DB when visiting a page further from the furthest visited page
            skipCounter += SKIP;
            retrievedUsers = (List<UserModelMongo>)
                    fetchUsers(
                            selectedSearchUser,
                            null,
                            null,
                            null
                    );        // Fetching new users
            users.addAll(retrievedUsers);            // Adding fetched users to the users list
            visitedPages.add(currentPage);
        } else {
            skipCounter += SKIP;
        }

        prevNextButtonsCheck(retrievedUsers.size());

        fillGridPane();
        scrollSet.setVvalue(0);
    }

    @FXML
    void onClickPrevious() {
        usersGridPane.getChildren().clear();

        if (currentPage > 0) {
            currentPage--;
            skipCounter -= SKIP;
        }

        prevNextButtonsCheck(0);
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    void prevNextButtonsCheck(int retrievedUsersSize) {
        previousButton.setDisable(currentPage == 0);

        boolean onFurthestPage = visitedPages.get(visitedPages.size() - 1) == currentPage;     // User is in the furthest page he visited

        if (onFurthestPage && retrievedUsersSize == 0 && !visualizedLastUser) {
            nextButton.setDisable(false);   // Keep enabled if we are on the furthest visited page up to now, we re-visited it, and we didn't reach the end
        } else {
            boolean moreUsersAvailable = (retrievedUsersSize == SKIP);          // If we retrieved SKIP users, likely there will be more available in the DB
            nextButton.setDisable(onFurthestPage && !moreUsersAvailable);       // Disable if on last page and if retrieved less than SKIP users
        }
    }

    private Object fetchUsers(String username, Date adminQueryStartDate, Date adminQueryEndDate, Integer adminQueryResultLimit){
        System.out.println("[INFO] New data has been fetched");
        switch (currentlyShowing) {             // Decide what type of users need to be fetched
            case ALL_USERS:
                this.refreshButton.setDisable(false);
                return userDBMongo.findAllUsersWithLimit(LIMIT, skipCounter);
            case USERS_WITH_COMMON_BOARDGAMES_POSTED:
                this.refreshButton.setDisable(false);
                return userService.suggestUsersByCommonBoardgamePosted(currentUser.getUsername(), LIMIT, skipCounter);
            case USERS_WITH_COMMON_LIKED_POSTS:
                this.refreshButton.setDisable(false);
                return userService.suggestUsersByCommonLikedPosts(currentUser.getUsername(), LIMIT, skipCounter);
            case INFLUENCER_USERS:
                this.refreshButton.setDisable(false);
                return userService.suggestInfluencerUsers(10, 10, 10, 10);
            case SEARCH_RESULTS:
                GenericUserModelMongo searchResult = userDBMongo.findByUsername(username, false).get();
                System.out.println("[DEBUG] searchResult: " + searchResult);
                return List.of((UserModelMongo) searchResult);
            case ADMIN_MOST_ACTIVE_USERS:
                String startDateString = "01-01-2000";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Date startDate;
                try {
                    startDate = adminQueryStartDate == null ? (simpleDateFormat.parse(startDateString)) : adminQueryStartDate;    // Get specified start date or 01-01-2000
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                Date endDate = adminQueryEndDate == null ? (new Date()) : adminQueryEndDate;                // Get specified end date or today
                int limitResults = adminQueryResultLimit == null ? 10 : adminQueryResultLimit;          // Get specified limit or top 10
                return userDBMongo.findActiveUsersByReviews(startDate, endDate, limitResults);
        }

        return new ArrayList<>();
    }

    private void loadViewMessageInfo(){
        Parent loadViewItem = stageManager.loadViewNode(FxmlView.INFOMSGUSERS.getFxmlFile());
        AnchorPane noContentsYet = new AnchorPane();
        noContentsYet.getChildren().add(loadViewItem);

        if (!users.isEmpty()){
            resetPageVars();
            usersGridPane.add(noContentsYet, 0, rowGridPane);
        } else {
            resetPageVars();
            usersGridPane.add(noContentsYet, 0, 1);
        }
        GridPane.setMargin(noContentsYet, new Insets(123, 200, 200, 387));
    }

    @FXML
    void fillGridPane() {
        if (users.size() == 1) {        // Needed to correctly position a single user in the gridpane
            columnGridPane = 0;
            rowGridPane = 0;
        } else {
            columnGridPane = 0;
            rowGridPane = 1;
        }

        userListener = (MouseEvent mouseEvent, UserModelMongo user) -> {        // Show user details using StageManager
            modelBean.putBean(Constants.SELECTED_USER, user);
            stageManager.switchScene(FxmlView.USERPROFILEPAGE);
        };
        usersGridPane.getChildren().clear();         // Removing old users

        try {
            if (users.isEmpty()) {
                loadViewMessageInfo();
            } else {
                // Creating an item for each user: displaying users in [skipCounter, skipCounter + LIMIT - 1]
                int startUser = skipCounter;
                int endUser = skipCounter + LIMIT - 1;
                if (endUser > users.size()) {
                    endUser = users.size() - 1;
                    visualizedLastUser = true;
                }

                System.out.println("[DEBUG] [startUser, endUser]: [" + startUser + ", " + endUser + "]");

                for (int i = startUser; i <= endUser; i++) {
                    UserModelMongo user = users.get(i);
                    if (currentUser.get_class().equals("user")) {      // Only show users which are not banned
                        if (!user.isBanned()) {
                            AnchorPane userNode = createUserViewNode(user);
                            addUserToGridPane(userNode);
                        }
                    } else {            // Show all users (also banned ones)
                        AnchorPane userNode = createUserViewNode(user);
                        addUserToGridPane(userNode);
                    }
                }
            }
        } catch (Exception ex) {
            stageManager.showInfoMessage("INFO", "An error occurred while retrieving users. Try again in a while.");
            System.err.println("[ERROR] fillGridPane@ControllerViewSearchUserPage.java raised an exception: " + ex.getMessage());
        }
    }

    private void displayMostActiveUsers(Document fetchedResults) {
        usersGridPane.getChildren().clear();         // Removing old content
        refreshButton.setDisable(true);
        nextButton.setDisable(true);

        columnGridPane = 1;
        rowGridPane = 1;

        ObservableList<TableData> mostActiveUsers = FXCollections.observableArrayList();
        List<Document> results = (List<Document>) fetchedResults.get("results");
        for (Document doc : results) {
            String username = doc.getString("_id");
            int reviewCount = doc.getInteger("reviewCount");
            double avgDateDiff = doc.getDouble("averageDateDifference");

            mostActiveUsers.add(new TableData(username, reviewCount, avgDateDiff));
        }

        TableView<TableData> mostAvtiveUsersTableView = createActiveUsersTableView(mostActiveUsers);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(mostAvtiveUsersTableView);
        BorderPane.setMargin(mostAvtiveUsersTableView, new Insets(0, 0, 0, 10));
        usersGridPane.add(borderPane, columnGridPane, rowGridPane);
    }

    private TableView<TableData> createActiveUsersTableView(ObservableList<TableData> mostActiveUsers) {
        TableView<TableData> tableView = new TableView<>();

        int rowHeight = 35;
        int usernameColumnWidth = 200;
        int reviewCountColumnWidth = 150;
        int avgDateDiffColumnWidth = 200;

        TableColumn<TableData, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());

        TableColumn<TableData, String> reviewCountColumn = new TableColumn<>("Review count");
        reviewCountColumn.setCellValueFactory(cellData -> cellData.getValue().reviewCountProperty());

        TableColumn<TableData, String> avgDateDiffColumn = new TableColumn<>("Average days distance");
        avgDateDiffColumn.setCellValueFactory(cellData -> cellData.getValue().avgDateDiffProperty());

        tableView.getColumns().addAll(usernameColumn, reviewCountColumn, avgDateDiffColumn);

        usernameColumn.setPrefWidth(usernameColumnWidth);
        usernameColumn.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        reviewCountColumn.setPrefWidth(reviewCountColumnWidth);
        reviewCountColumn.setStyle("-fx-alignment: CENTER;");

        avgDateDiffColumn.setPrefWidth(avgDateDiffColumnWidth);
        avgDateDiffColumn.setStyle("-fx-alignment: CENTER;");

        tableView.setLayoutX(10);           // Left padding
        tableView.setMinWidth(usernameColumnWidth + reviewCountColumnWidth + avgDateDiffColumnWidth + 10);

        if (!mostActiveUsers.isEmpty()) {
            tableView.setRowFactory(tv -> {
                TableRow<TableData> row = new TableRow<>();
                row.setPrefHeight(rowHeight);
                return row;
            });

            tableView.setItems(mostActiveUsers);
            tableView.setMinHeight((tableView.getItems().size() + 1) * rowHeight);
        } else {
            Label noResultsTablePlaceholder = new Label("No results to show with the specified filters.");
            tableView.setPlaceholder(noResultsTablePlaceholder);
        }

        return tableView;
    }

    private AnchorPane createUserViewNode(UserModelMongo user) {
        Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTUSER.getFxmlFile());
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().add(loadViewItem);

        controllerObjectUser.setData(user);

        anchorPane.setOnMouseClicked(event -> {
            this.userListener.onClickUserListener(event, user);});

        return anchorPane;
    }

    private void addUserToGridPane(AnchorPane userNode) {
        if (columnGridPane == 1) {
            columnGridPane = 0;
            rowGridPane++;
        }

        usersGridPane.add(userNode, columnGridPane++, rowGridPane); //(child,column,row)

        usersGridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
        usersGridPane.setPrefWidth(500);
        usersGridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);

        usersGridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
        usersGridPane.setPrefHeight(400);
        usersGridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);

        GridPane.setMargin(userNode, new Insets(15, 5, 15, 215));
    }

    public void onClickLogout() {
        modelBean.putBean(Constants.CURRENT_USER, null);
        stageManager.switchScene(FxmlView.WELCOMEPAGE);
    }

    public void onClickYourProfileButton() {
        stageManager.showWindow(FxmlView.USERPROFILEPAGE);
        stageManager.closeStageButton(this.logoutButton);
    }

    public void onClickAccountInfoButton() {
        stageManager.showWindow(FxmlView.ACCOUNTINFOPAGE);
        stageManager.closeStageButton(this.accountInfoButton);
    }

    public void onClickRefreshButton(){
        currentlyShowing = UsersToFetch.ALL_USERS;
        onSelectChoiceBoxOption();  // The same actions that are performed when clicking a choice box option have to be performed
    }

    public void onKeyTypedSearchBar() {
        String searchString = textFieldSearch.getText();

        if (searchString.isEmpty()) {
            searchResultsList.setVisible(false);
        } else {
            searchResultsList.setVisible(true);
        }

        ObservableList<String> usernamesContainingSearchString = FXCollections.observableArrayList(
                ((List<String>)modelBean.getBean(Constants.USERS_USERNAMES)).stream()
                        .filter(tag -> tag.toLowerCase().contains(searchString.toLowerCase())).toList());
        System.out.println("[DEBUG] filtered usernames list size: " + usernamesContainingSearchString.size());

        searchResultsList.setItems(usernamesContainingSearchString);
        int LIST_ROW_HEIGHT = 24;
        if (usernamesContainingSearchString.size() > 10) {
            searchResultsList.setPrefHeight(10 * LIST_ROW_HEIGHT + 2);
        } else if (usernamesContainingSearchString.isEmpty()){
            searchResultsList.setVisible(false);
        } else {
            searchResultsList.setPrefHeight(usernamesContainingSearchString.size() * LIST_ROW_HEIGHT + 2);
        }

        // Highlight matching search substring in result strings
        searchResultsList.setCellFactory(boardgameResult -> new ListCell<String>() {
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

    @FXML
    public void onMouseClickedListView() {
        searchResultsList.setVisible(false);

        selectedSearchUser = searchResultsList.getSelectionModel().getSelectedItem().toString();
        textFieldSearch.setText(selectedSearchUser);

        searchUsers();
    }

    private void searchUsers() {
        currentlyShowing = UsersToFetch.SEARCH_RESULTS;
        resetPageVars();
        List<UserModelMongo> retrievedUsers = (List<UserModelMongo>)
                fetchUsers(
                        selectedSearchUser,
                        null,
                        null,
                        null
                );
        users.addAll(retrievedUsers);            // Add new LIMIT users (at most)
        fillGridPane();
        prevNextButtonsCheck(retrievedUsers.size());            // Initialize buttons
    }
}
