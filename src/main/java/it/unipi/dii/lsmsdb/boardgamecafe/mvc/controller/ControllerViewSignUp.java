package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
//import it.unipi.dii.lsmsdb.boardgamecafe.services.UserService;
import it.unipi.dii.lsmsdb.boardgamecafe.services.UserService;
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
public class ControllerViewSignUp implements Initializable {

    @FXML
    private Button cancelButton;
    @FXML
    private Button finishButton;

    @Autowired
    private UserService serviceUser;

    private final StageManager stageManager;

    private final static Logger logger = LoggerFactory.getLogger(ControllerViewSignUp.class);

    @Autowired @Lazy
    public ControllerViewSignUp(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    public void onClickCancel(ActionEvent event)
    {
        stageManager.closeStage(this.cancelButton);
    }

    public void onClickFinish(ActionEvent event)
    {
        //DoStuff - toDo
        stageManager.showInfoMessage("Sign-Up Info: ", "You're Successfully Registered");
        stageManager.closeStage(this.finishButton);

        /* ***
        try {
            //Creazione User dopo aver acquisito le info
            UserModelMongo user = serviceUser.createUser(username,email,password,name,surname,gender,
                    nationality, year, month, day);

            //Inserimento User in Mongo e Neo4j DBs
            if (!serviceUser.insertUser(user)) {
                stageManager.showInfoMessage("ERROR", "Error in adding new user, " +
                        "please try again");
                return;
            }
            stageManager.closeStage(this.buttonSignUp);
            stageManager.switchScene(FxmlView.USER);
        } catch (Exception e) {
            logger.error("Error in adding new user: " + e.getLocalizedMessage());
            e.printStackTrace();
        } *** */
    }

}
