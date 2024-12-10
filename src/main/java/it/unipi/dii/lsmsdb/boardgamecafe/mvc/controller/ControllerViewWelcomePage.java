package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class ControllerViewWelcomePage {

    @FXML
    private Button logInButton;
    @FXML
    private Button signUpButton;
    @FXML
    private Button guestUserButton;
    @FXML
    private Button quitAppButton;

    private final StageManager stageManager;

    @Autowired
    @Lazy
    public ControllerViewWelcomePage(StageManager stageManager) {
        this.stageManager = stageManager;
    }


    public void onClickLogIn(ActionEvent event) {
        stageManager.switchScene(FxmlView.LOGIN);
    }

    public void onClickSignUpWPage(ActionEvent event) {
        stageManager.showWindow(FxmlView.SIGNUP);
    }

    public void onClickGuestUser(ActionEvent actionEvent) {
        stageManager.showWindow(FxmlView.GUESTPOSTS);
        stageManager.closeStageButton(this.guestUserButton);
    }

    public void onClickQuitAppButton(ActionEvent actionEvent)
    {
        stageManager.closeStageButton(this.quitAppButton);
        Platform.exit();
        System.exit(0);
    }
}
