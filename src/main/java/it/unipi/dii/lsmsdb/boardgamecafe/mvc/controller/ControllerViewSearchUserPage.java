package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.UserListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.BoardgameDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.PostService;
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
    private Button searchButton;
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
    private Button testButton;
    @FXML
    private TextField textFieldSearch;
    @FXML
    private GridPane postGridPane;
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
            "All posts",
            "Users which posted about a game you posted about too",         // suggestUsersByCommonBoardgamePosted@UserService
            "Users that enjoy the same posts as you",       // suggestUsersByCommonLikedPosts@UserService
            "Influencers in the boardgames community"      // suggestInfluencerUsers@UserService
    );

    // User Variables
    private List<UserModelMongo> users = new ArrayList<>();

    //Utils Variables
    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;            // Ho many times the user clicked on the 'Next' button
    private final static int SKIP = 10;     // How many users to skip each time
    private final static int LIMIT = 10;    // How many users to show in each page

    private enum UsersToFetch {
        ALL_USERS,
        USERS_WITH_COMMONLY_POSTED_BOARDGAMES,
        USERS_WITH_COMMONLY_LIKED_POSTS,
        INFLUENCER_USERS,
        SEARCH_RESULTS,
    };
    private static UsersToFetch currentlyShowing;       // Global indicator of what users are being shown on the page

    private static int currentPage;
    private static List<Integer> visitedPages;
    private static boolean visualizedLastPost;      // Keeps track of whether the user has reached the last reachable page or not;

    // Search functionalities
    private List<String> usersUsernames;            // TODO: move this + its init into model bean
    private static String selectedSearchTag;

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

        // Prefetch boardgame tags for the search function and init search functionalities variables
        searchResultsList.setVisible(false);

        users.addAll(fetchUsers(null));

        fillGridPane();

        currentlyShowing = UsersToFetch.ALL_USERS;            // Static var init TODO: MODIFY

        // Choice box init
        whatUsersToShowChoiceBox.setValue(whatUsersToShowList.get(0));      // Default choice box string
        whatUsersToShowChoiceBox.setItems(whatUsersToShowList);                 // Setting the other options in choice box

        // Adding listeners to option selection: change indicator of what is displayed on the screen and retrieve results
        whatUsersToShowChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateCurrentlyShowing(newValue);
            onSelectChoiceBoxOption();
        });

        onSelectChoiceBoxOption();        // Show posts by followed users by default

        // Prefetch boardgame tags for the search function and init search functionalities variables
        searchResultsList.setVisible(false);

        long startTime = System.currentTimeMillis();
        usersUsernames = userDBMongo.getUserUsernames();    // TODO: maybe move into model bean? (fetch once at start and the it's always there)
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("[INFO] Fetched " + usersUsernames.size() + " usernames in " + elapsedTime + " ms");
        selectedSearchTag = null;

//        currentUser = ((UserModelMongo) modelBean.getBean(Constants.CURRENT_USER)).getUsername();
        currentUser = "blackpanda723";
    }

    private void updateCurrentlyShowing(String choiceBoxValue) {
        if (choiceBoxValue.equals(whatUsersToShowList.get(0))) {
            currentlyShowing = UsersToFetch.ALL_USERS;
        }

        if (choiceBoxValue.equals(whatUsersToShowList.get(1))) {
            currentlyShowing = UsersToFetch.USERS_WITH_COMMONLY_POSTED_BOARDGAMES;
        }

        if (choiceBoxValue.equals(whatUsersToShowList.get(2))) {
            currentlyShowing = UsersToFetch.USERS_WITH_COMMONLY_LIKED_POSTS;
        }

        if (choiceBoxValue.equals(whatUsersToShowList.get(3))) {
            currentlyShowing = UsersToFetch.INFLUENCER_USERS;
        }

        if (choiceBoxValue.equals(whatUsersToShowList.get(3))) {
            currentlyShowing = UsersToFetch.SEARCH_RESULTS;
        }
    }

    public void onSelectChoiceBoxOption() {
        resetPageVars();
        selectedSearchTag = null;
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
        visualizedLastPost = false;
    }

    public void onClickBoardgamesButton() {
        stageManager.showWindow(FxmlView.REGUSERBOARDGAMES);
        stageManager.closeStageButton(this.boardgamesButton);
    }

    public void onClickPostsFeedButton() {
        stageManager.showWindow(FxmlView.REGUSERPOSTS);
        stageManager.closeStageButton(this.postsFeedButton);
    }

    public void onClickSearch() {
        currentlyShowing = UsersToFetch.SEARCH_RESULTS;
        resetPageVars();
        List<UserModelMongo> retrievedUsers = fetchUsers(selectedSearchTag);
        users.addAll(retrievedUsers);            // Add new LIMIT users (at most)
        fillGridPane();
        prevNextButtonsCheck(retrievedUsers.size());            // Initialize buttons
    }

    public void onClickClearField() {
        this.textFieldSearch.clear();           // When clearing the search box, we reset the view to make it show the default shown users
        currentlyShowing = UsersToFetch.ALL_USERS;
        onSelectChoiceBoxOption();
    }

    @FXML
    void onClickNext() {
        postGridPane.getChildren().clear();

        List<UserModelMongo> retrievedUsers = new ArrayList<>();
        currentPage++;
        if (!visitedPages.contains(currentPage)) {
            // New users need to be retrieved from the DB when visiting a page further from the furthest visited page
            skipCounter += SKIP;
            retrievedUsers = fetchUsers(null);        // Fetching new users
            users.addAll(retrievedUsers);            // Adding fetched users to the user list
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
        postGridPane.getChildren().clear();

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

        if (onFurthestPage && retrievedUsersSize == 0 && !visualizedLastPost) {
            nextButton.setDisable(false);   // Keep enabled if we are on the furthest visited page up to now, we re-visited it, and we didn't reach the end
        } else {
            boolean morePostsAvailable = (retrievedUsersSize == SKIP);          // If we retrieved SKIP users, likely there will be more available in the DB
            nextButton.setDisable(onFurthestPage && !morePostsAvailable);       // Disable if on last page and if retrieved less than SKIP users
        }
    }

    private List<UserModelMongo> fetchUsers(String username){
        System.out.println("[INFO] New data has been fetched");
        switch (currentlyShowing) {
            case ALL_USERS:
                return userDBMongo.findAllUsersWithLimit(LIMIT, skipCounter);
            case SEARCH_RESULTS:
                GenericUserModelMongo searchedUserGeneric = userDBMongo.findByUsername(username).get();
                System.out.println("[DEBUG] found this user: " + searchedUserGeneric);
                return (List<UserModelMongo>) searchedUserGeneric;
            case USERS_WITH_COMMONLY_LIKED_POSTS:
                return userService.suggestUsersByCommonLikedPosts(currentUser, LIMIT, skipCounter);
            case USERS_WITH_COMMONLY_POSTED_BOARDGAMES:
                userService.suggestUsersByCommonBoardgamePosted(currentUser, LIMIT, skipCounter);
            case INFLUENCER_USERS:
                return userService.suggestInfluencerUsers(10, 10, 10, 10);
        }

        return new ArrayList<>();
    }

    void setGridPaneColumnAndRow(){
        columnGridPane = 0;
        rowGridPane = 1;
    }

    private void loadViewMessageInfo(){
        Parent loadViewItem = stageManager.loadViewNode(FxmlView.INFOMSGPOSTS.getFxmlFile());
        AnchorPane noContentsYet = new AnchorPane();
        noContentsYet.getChildren().add(loadViewItem);

        if (!users.isEmpty()){
            resetPageVars();
            postGridPane.add(noContentsYet, 0, rowGridPane);
        } else {
            resetPageVars();
            postGridPane.add(noContentsYet, 0, 1);
        }
        GridPane.setMargin(noContentsYet, new Insets(123, 200, 200, 392));
    }

    @FXML
    void fillGridPane() {
        if (users.size() == 1) {
            columnGridPane = 0;
            rowGridPane = 0;
        } else {
            setGridPaneColumnAndRow();
        }

        userListener = (MouseEvent mouseEvent, UserModelMongo user) -> {
            modelBean.putBean(Constants.SELECTED_USER, user);
            stageManager.switchScene(FxmlView.USERPROFILEPAGE);
        };
        postGridPane.getChildren().clear();


        try {
            if (users.isEmpty()) {
                loadViewMessageInfo();
            } else {
                // Creating an item for each user: displaying posts in [skipCounter, skipCounter + LIMIT - 1]
                int startUser = skipCounter;
                int endUser = skipCounter + LIMIT - 1;
                if (endUser > users.size()) {
                    endUser = users.size() - 1;
                    visualizedLastPost = true;
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
                    GridPane.setMargin(anchorPane, new Insets(15, 5, 15, 180));
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
        stageManager.showWindow(FxmlView.SIGNUP);
    }

    public void onClickRefreshButton(){
        //ToDO: qua andrebbe fatto clean/fetch/fill.
        // chidere a marco in base all'implementazione fatta all'interno di questo controller.
        // Il button è già stato inserito nella view grafica e mappato su questo controller.
    }

    public void onKeyTypedSearchBar() {
        String searchString = textFieldSearch.getText();

        if (searchString.isEmpty()) {
            searchResultsList.setVisible(false);
        } else {
            searchResultsList.setVisible(true);
        }

        ObservableList<String> usersContainingSearchString = FXCollections.observableArrayList(usersUsernames.stream()
                .filter(username -> username.toLowerCase().contains(searchString.toLowerCase())).toList());
        System.out.println("[DEBUG] filtered usernames list size: " + usersContainingSearchString.size());

        searchResultsList.setItems(usersContainingSearchString);
        int LIST_ROW_HEIGHT = 24;
        if (usersContainingSearchString.size() > 10) {
            searchResultsList.setPrefHeight(10 * LIST_ROW_HEIGHT + 2);
        } else if (usersContainingSearchString.isEmpty()){
            searchResultsList.setVisible(false);
        } else {
            searchResultsList.setPrefHeight(usersContainingSearchString.size() * LIST_ROW_HEIGHT + 2);
        }

        // Highlight matching search substring in result strings
        searchResultsList.setCellFactory(usernameResult -> new ListCell<String>() {
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

        selectedSearchTag = searchResultsList.getSelectionModel().getSelectedItem().toString();
        textFieldSearch.setText(selectedSearchTag);
    }
}
