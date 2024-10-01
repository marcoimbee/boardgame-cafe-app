package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;


import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class ControllerViewLogin implements Initializable {

    @FXML
    private Button cancelButton;
    @FXML
    private Button loginButton;
    private final StageManager stageManager;

    private final static Logger logger = LoggerFactory.getLogger(ControllerViewLogin.class);

    @Autowired @Lazy
    public ControllerViewLogin(StageManager stageManager) {
        this.stageManager = stageManager;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    public void onClickLogin(ActionEvent actionEvent)
    {
        stageManager.closeStage(this.cancelButton);
        stageManager.showWindow(FxmlView.USERPOFILEPAGE);
    }
    public void onClickCancelButton(ActionEvent actionEvent)
    {
        stageManager.closeStage(this.cancelButton);
        stageManager.showWindow(FxmlView.WELCOMEPAGE);
    }


}
