package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.AdminModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class ControllerViewStatisticsPage implements Initializable{

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
    @FXML
    private Button topCommentedTaggedPostBtn;

    @Autowired
    private ModelBean modelBean;

    private GenericUserModelMongo currentUser;
    private final StageManager stageManager;

    public enum statisticsToShow {
        AVG_AGE_BY_COUNTRY,
        COUNTRIES_WITH_HIGHEST_USER_NUMBER,
        MOST_POSTED_BOARDGAMES,
        MOST_ACTIVE_USERS,
        MOST_COMMENTED_TAGGED_POST
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
        stageManager.switchScene(FxmlView.WELCOMEPAGE);
    }

    private void initComboBox() {}

    private void openSelectedAnalyticView(statisticsToShow selectedAnalytic) {
        modelBean.putBean(Constants.SELECTED_ANALYTICS, selectedAnalytic);
        stageManager.showWindow(FxmlView.SELECTED_ANALYTIC);
    }

    public void onClickCountriesAnalyticsBtn() {
        this.openSelectedAnalyticView(statisticsToShow.COUNTRIES_WITH_HIGHEST_USER_NUMBER);
    }

    public void onClickAvgAgeAnalyticsBtn() {
        this.openSelectedAnalyticView(statisticsToShow.AVG_AGE_BY_COUNTRY);
    }

    public void onClickMostActiveUsersAnalyticsBtn() {
        modelBean.putBean(Constants.SELECTED_ANALYTICS, statisticsToShow.MOST_ACTIVE_USERS);
        stageManager.showWindow(FxmlView.SEARCHUSER);
        stageManager.closeStageButton(this.searchUserButton);
    }

    public void onClickMostPostedBGamesAnalyticsBtn() {
        modelBean.putBean(Constants.SELECTED_ANALYTICS, statisticsToShow.MOST_POSTED_BOARDGAMES);
        stageManager.showWindow(FxmlView.REGUSERBOARDGAMES);
        stageManager.closeStageButton(this.postsFeedButton);
    }

    public void onClickTopCommentedTaggedPostBtn() {
        modelBean.putBean(Constants.SELECTED_ANALYTICS, statisticsToShow.MOST_COMMENTED_TAGGED_POST);
        stageManager.showWindow(FxmlView.REGUSERPOSTS);
        stageManager.closeStageButton(this.postsFeedButton);
    }
}
