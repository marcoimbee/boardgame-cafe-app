package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.UserListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.services.UserService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


@Component
public class ControllerViewSearchUserPage implements Initializable {
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
        SEARCH_RESULTS
    };
    private static UsersToFetch currentlyShowing;       // Global indicator of what type of user is being shown on the page

    private static int currentPage;
    private static List<Integer> visitedPages;
    private static boolean visualizedLastUser;      // Keeps track of whether the user has reached the last reachable page or not;

    // Search functionalities
    private List<String> userUsernames;
    private static String selectedSearchUser;

    private static String currentUser;

    @Autowired
    @Lazy
    public ControllerViewSearchUserPage(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    @FXML
    public void initialize(URL location, ResourceBundle resources) {
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

        currentUser = ((UserModelMongo) modelBean.getBean(Constants.CURRENT_USER)).getUsername();
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
    }

    public void onSelectChoiceBoxOption() {
        resetPageVars();
        selectedSearchUser = null;
        List<UserModelMongo> retrievedUsers = fetchUsers(null);
        users.addAll(retrievedUsers);            // Add new LIMIT users (at most)
        fillGridPane();
        prevNextButtonsCheck(retrievedUsers.size());            // Initialize buttons
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
            retrievedUsers = fetchUsers(selectedSearchUser);        // Fetching new users
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

    private List<UserModelMongo> fetchUsers(String username){
        System.out.println("[INFO] New data has been fetched");
        switch (currentlyShowing) {             // Decide what type of users need to be fetched
            case ALL_USERS:
                return userDBMongo.findAllUsersWithLimit(LIMIT, skipCounter);
            case USERS_WITH_COMMON_BOARDGAMES_POSTED:
                return userService.suggestUsersByCommonBoardgamePosted(currentUser, LIMIT, skipCounter);
            case USERS_WITH_COMMON_LIKED_POSTS:
                return userService.suggestUsersByCommonLikedPosts(currentUser, LIMIT, skipCounter);
            case INFLUENCER_USERS:
                return userService.suggestInfluencerUsers(10, 10, 10, 10);
            case SEARCH_RESULTS:
                GenericUserModelMongo searchResult = userDBMongo.findByUsername(username).get();
                System.out.println("[DEBUG] searchResult: " + searchResult);
                return List.of((UserModelMongo) searchResult);
        }

        return new ArrayList<>();
    }

    void setGridPaneColumnAndRow(){
        columnGridPane = 0;
        rowGridPane = 1;
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
            setGridPaneColumnAndRow();
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

                    Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTUSER.getFxmlFile());

                    AnchorPane anchorPane = new AnchorPane();
                    anchorPane.getChildren().add(loadViewItem);

                    controllerObjectUser.setData(user);

                    anchorPane.setOnMouseClicked(event -> {
                        this.userListener.onClickUserListener(event, user);});

                    //choice number of column
                    if (columnGridPane == 1) {
                        columnGridPane = 0;
                        rowGridPane++;
                    }

                    usersGridPane.add(anchorPane, columnGridPane++, rowGridPane); //(child,column,row)
                    //DISPLAY SETTINGS
                    //set grid width
                    usersGridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
                    usersGridPane.setPrefWidth(500);
                    usersGridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);
                    //set grid height
                    usersGridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
                    usersGridPane.setPrefHeight(400);
                    usersGridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
                    //GridPane.setMargin(anchorPane, new Insets(25));
                    GridPane.setMargin(anchorPane, new Insets(15, 5, 15, 215));
                }
            }
        } catch (Exception ex) {
            stageManager.showInfoMessage("INFO", "An error occurred while retrieving users. Try again in a while.");
            System.err.println("[ERROR] fillGridPane@ControllerViewSearchUserPage.java raised an exception: " + ex.getMessage());
        }
    }

    public void onClickLogout() {
        stageManager.showWindow(FxmlView.WELCOMEPAGE);
        stageManager.closeStageButton(this.logoutButton);
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
        List<UserModelMongo> retrievedUsers = fetchUsers(selectedSearchUser);
        users.addAll(retrievedUsers);            // Add new LIMIT users (at most)
        fillGridPane();
        prevNextButtonsCheck(retrievedUsers.size());            // Initialize buttons
    }
}
