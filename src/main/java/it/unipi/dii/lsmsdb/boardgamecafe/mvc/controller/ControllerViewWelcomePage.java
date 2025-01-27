package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import javafx.application.Platform;
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

    public void onClickLogIn() {
        stageManager.switchScene(FxmlView.LOGIN);
    }

    public void onClickSignUpWPage() {
        stageManager.showWindow(FxmlView.SIGNUP);
    }

    public void onClickGuestUser() {
        stageManager.showWindow(FxmlView.GUESTBOARDGAMES);
        stageManager.closeStageButton(this.guestUserButton);
    }

    public void onClickQuitAppButton() {
        stageManager.closeStageButton(this.quitAppButton);
        Platform.exit();
        System.exit(0);
    }
}
