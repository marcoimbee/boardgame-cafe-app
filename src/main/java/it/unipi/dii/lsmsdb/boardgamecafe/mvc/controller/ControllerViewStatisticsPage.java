package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ControllerViewStatisticsPage implements Initializable{


    //********* Buttons *********
    @FXML
    private Button yourProfileButton;
    @FXML
    private Button postsFeedButton;
    @FXML
    private Button accountInfoButton;
    @FXML
    private Button statisticsButton;
    @FXML
    private Button searchUserButton;
    @FXML
    private Button boardgamesCollectionButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button countriesAnalyticsBtn;
    @FXML
    private Button avgAgeAnalyticsBtn;
    @FXML
    private Button mostActiveUsersAnalyticsBtn;
    @FXML
    private Button mostPostedBGameBtn;


    //********* Autowireds *********
    @Autowired
    private UserDBMongo userDBMongo;
    @Autowired
    private UserService serviceUser;
    @Autowired
    private ModelBean modelBean;

    //Stage Manager
    private final StageManager stageManager;
    //User
    private UserModelMongo regUser;
    //Useful Variables

    private ObservableList<String> whatStatisticToShow = FXCollections.observableArrayList(
            "Show the most posted Boardgame", // --> Show the tag of Post that is the most commented.
            "Show average age of users per country",
            "Show Most Active Users",
            "Show the countries with the highest user number"
    );

    private enum statisticsToShow {
        MOST_POSTED_BOARDGAME,
        AVG_AGE_BY_COUNTRY,
        MOST_ACTIVE_USER,
        COUNTRIES_WITH_HIGHEST_USER_NUMBER,

    };

    private String currentlyShowing;

    @Autowired
    @Lazy
    public ControllerViewStatisticsPage(StageManager stageManager) {
        this.stageManager = stageManager;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.initComboBox();
        this.statisticsButton.setDisable(true);
    }

    //********** On Click Button Methods **********
    public void onClickAccountInfoButton() {
    stageManager.switchScene(FxmlView.ACCOUNTINFOPAGE);
    }
    public void onClickYourProfileButton() {
        stageManager.showWindow(FxmlView.USERPROFILEPAGE);
        stageManager.closeStageButton(this.yourProfileButton);
    }
    public void onClickBoardgamesButton() {
        stageManager.showWindow(FxmlView.REGUSERBOARDGAMES);
        stageManager.closeStageButton(this.boardgamesCollectionButton);
    }
    public void onClickPostsFeedButton() {
        stageManager.showWindow(FxmlView.REGUSERPOSTS);
        stageManager.closeStageButton(this.postsFeedButton);
    }
    public void onClickSearchUserButton() {
        stageManager.showWindow(FxmlView.SEARCHUSER);
        stageManager.closeStageButton(this.searchUserButton);
    }
    public void onClickLogout(ActionEvent event) {
        modelBean.putBean(Constants.CURRENT_USER, null);
        stageManager.showWindow(FxmlView.WELCOMEPAGE);
        stageManager.closeStageButton(this.logoutButton);
    }

    public void onClickShowCountriesButton(ActionEvent event)
    {
        //String selectedAnalytics = this.
        modelBean.putBean(Constants.SELECTED_ANALYTICS, "");
    }


    //********** Internal Methods **********
    private void initDisplay() {

    }

    private void resetPage() {}


    private void initComboBox() {
    }

    public void onClickCountriesAnalyticsBtn(ActionEvent event)
    {
        this.userDBMongo.findCountriesWithMostUsers(10);
    }

    public void onClickAvgAgeAnalyticsBtn(ActionEvent event)
    {

    }

    public void onClickMostActiveUsersAnalyticsBtn(ActionEvent event)
    {

    }

    public void onClickMostPostedBGamesAnalyticsBtn(ActionEvent event)
    {

    }
}
