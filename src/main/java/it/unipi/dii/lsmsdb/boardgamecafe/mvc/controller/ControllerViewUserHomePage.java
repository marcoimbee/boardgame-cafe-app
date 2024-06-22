package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class ControllerViewUserHomePage {

    @FXML
    private Button profileButton;
    @FXML
    private Button accountDetailsButton;
    @FXML
    private Button boardgamesCollectionButton;
    @FXML
    private Button boardgamePostsButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button searchButton;
    @FXML
    private Button clearFieldButton;

    private final StageManager stageManager;

    @Autowired
    @Lazy
    public ControllerViewUserHomePage(StageManager stageManager) {
        this.stageManager = stageManager;
    }


    public void onClickBoardgamePosts(ActionEvent actionEvent) {
    }

    public void onClickBoardgamesColletcion() {
    }

    public void onClickYourProfile(ActionEvent actionEvent) {
    }

    public void onClickAccountDetails(ActionEvent actionEvent) {
        stageManager.showWindow(FxmlView.SIGNUP);
    }

    public void onClickSearch() {
    }

    public void onClickClearField() {
    }

    public void onClickLogout(ActionEvent actionEvent)
    {
        stageManager.showWindow(FxmlView.WELCOMEPAGE);
        stageManager.closeStage(this.logoutButton);
    }
}
