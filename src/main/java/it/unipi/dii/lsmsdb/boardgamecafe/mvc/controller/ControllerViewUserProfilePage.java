package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class ControllerViewUserProfilePage implements Initializable{

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
    @FXML
    private Button nextButton;
    @FXML
    private Button previousButton;

    private final StageManager stageManager;

    @Autowired
    @Lazy
    public ControllerViewUserProfilePage(StageManager stageManager) {
        this.stageManager = stageManager;
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.profileButton.setDisable(true);
        this.nextButton.visibleProperty().setValue(false);
        this.previousButton.visibleProperty().setValue(false);
    }

    public void onClickBoardgamePosts(ActionEvent actionEvent) {
        stageManager.showWindow(FxmlView.REGUSERPOSTS);
        stageManager.closeStage(this.boardgamePostsButton);
    }

    public void onClickBoardgamesColletcion() {
        stageManager.showWindow(FxmlView.REGUSERBOARDGAMES);
        stageManager.closeStage(this.boardgamesCollectionButton);
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

    public void onClickSearchUserButton(ActionEvent event) {
    }

    public void onClickPrevious(ActionEvent event) {
    }

    public void onClickNext(ActionEvent event) {
    }
}
