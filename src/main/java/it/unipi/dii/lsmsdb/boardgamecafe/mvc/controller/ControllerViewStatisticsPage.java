package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.AdminModelMongo;
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
    private ModelBean modelBean;
    private GenericUserModelMongo currentUser;

    //Stage Manager
    private final StageManager stageManager;
    //User
    private UserModelMongo regUser;

    public static enum statisticsToShow {
        AVG_AGE_BY_COUNTRY,
        COUNTRIES_WITH_HIGHEST_USER_NUMBER,

    };

    @Autowired
    @Lazy
    public ControllerViewStatisticsPage(StageManager stageManager) {
        this.stageManager = stageManager;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        currentUser = (GenericUserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        if (!currentUser.get_class().equals("admin")){
            currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        } else {
            currentUser = (AdminModelMongo) modelBean.getBean(Constants.CURRENT_USER);
            this.yourProfileButton.setVisible(false);
        }
        this.yourProfileButton.setVisible(false);
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
        modelBean.putBean(Constants.IS_ADMIN, null);
        stageManager.switchScene(FxmlView.WELCOMEPAGE);
    }

    //********** Internal Methods **********
    private void initDisplay() {

    }

    private void resetPage() {}


    private void initComboBox() {
    }

    private void openSelectedAnalyticView(statisticsToShow selectedAnalytic)
    {
        modelBean.putBean(Constants.SELECTED_ANALYTICS, selectedAnalytic);
        stageManager.showWindow(FxmlView.SELECTED_ANALYTIC);
    }
    public void onClickCountriesAnalyticsBtn(ActionEvent event)
    {
        this.openSelectedAnalyticView(statisticsToShow.COUNTRIES_WITH_HIGHEST_USER_NUMBER);
    }

    public void onClickAvgAgeAnalyticsBtn(ActionEvent event)
    {
        this.openSelectedAnalyticView(statisticsToShow.AVG_AGE_BY_COUNTRY);
    }

    public void onClickMostActiveUsersAnalyticsBtn(ActionEvent event)
    {
        //this.openSelectedAnalyticView(statisticsToShow.MOST_ACTIVE_USER);
    }

    public void onClickMostPostedBGamesAnalyticsBtn(ActionEvent event)
    {
        //this.openSelectedAnalyticView(statisticsToShow.MOST_POSTED_BOARDGAME);
    }
}
