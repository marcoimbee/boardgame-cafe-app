package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.UserListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.services.CommentService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ControllerObjectUser {

    @FXML
    private Button followButton;
    @FXML
    private Button viewProfileButton;
    @FXML
    protected Label usernameLabel;
    @FXML
    protected Label firstNameLabel;
    @FXML
    protected Label lastNameLabel;
    @FXML
    protected Label nationalityLabel;
    @FXML
    protected ImageView profileImage;

    private AnchorPane anchorPaneSelected;

    private UserModelMongo user;

    @Autowired
    private ModelBean modelBean;

    private StageManager stageManager;
    @Autowired
    @Lazy
    public ControllerObjectUser(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    public ControllerObjectUser() {
    }

    public void setData(UserModelMongo user) {

        this.user = user;

        UserModelMongo currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);

        if(user == currentUser){
            this.followButton.setDisable(true);
        }

        Image image = new Image(Objects.requireNonNull(getClass().
                                getResource("/user.png")).toExternalForm());
        this.profileImage.setImage(image);
        this.firstNameLabel.setText(user.getName());
        this.lastNameLabel.setText(user.getSurname());
        this.nationalityLabel.setText(user.getNationality());
        this.usernameLabel.setText(user.getUsername());
    }

    @FXML
    public void onClickFollowButton(ActionEvent event) {
        // Implementazione per rimuovere il post
        String title = "Work in Progress";
        String message = "" +
                "A breve ti sarà possibile seguire questo utente tramite questo button.\n";
        stageManager.showInfoMessage(title, message);
    }



}
