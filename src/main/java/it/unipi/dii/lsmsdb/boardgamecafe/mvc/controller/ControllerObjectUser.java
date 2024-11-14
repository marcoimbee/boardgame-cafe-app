package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.UserListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.CommentService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

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

    private static UserModelMongo currentUser;
    private static List<String> currentUserFollowedList;

    @Autowired
    UserDBNeo4j userDBNeo4j;

    @Autowired
    @Lazy
    public ControllerObjectUser(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    public ControllerObjectUser() {
    }

    public void setData(UserModelMongo user) {
        this.user = user;

        // UserModelMongo currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        currentUserFollowedList = (List<String>) modelBean.getBean(Constants.CURRENT_USER_FOLLOWED_LIST);

        if (user == currentUser){
            this.followButton.setDisable(true);
        }

        if (currentUserFollowedList.contains(user.getUsername())) {
            followButton.setText(" Unfollow");
        } else {
            followButton.setText(" Follow");
        }

        Image image = new Image(Objects.requireNonNull(getClass().
                                getResource("/user.png")).toExternalForm());
        this.profileImage.setImage(image);
        this.firstNameLabel.setText(user.getName());
        this.lastNameLabel.setText(user.getSurname());
        this.nationalityLabel.setText(user.getNationality());
        this.usernameLabel.setText(user.getUsername());

        followButton.setOnAction(event -> onClickFollowButton(user, (Button) event.getSource()));
    }

    public void onClickFollowButton(UserModelMongo selectedUser, Button clickedButton) {
        try {
            String selectedUserUsername = selectedUser.getUsername();
            String currentUserUsername = userDBNeo4j.findByUsername(currentUser.getUsername()).get().getUsername();

            boolean followed = currentUserFollowedList.contains(selectedUserUsername);

            if (!followed) {
                // Add new Neo4J relationship
                userDBNeo4j.followUser(currentUserUsername, selectedUserUsername);

                // Adding username to collection and updating model bean
                currentUserFollowedList.add(selectedUserUsername);
                modelBean.putBean(Constants.CURRENT_USER_FOLLOWED_LIST, currentUserFollowedList);

                // Update follow/unfollow button graphics
                clickedButton.setText(" Unfollow");

                System.out.println("[INFO] " + currentUserUsername + " followed " + selectedUserUsername);
            } else {
                // Remove Neo4J relationship
                userDBNeo4j.unfollowUser(currentUserUsername, selectedUserUsername);

                // Removing username to collection and updating model bean
                currentUserFollowedList.remove(selectedUserUsername);
                modelBean.putBean(Constants.CURRENT_USER_FOLLOWED_LIST, currentUserFollowedList);

                // Update follow/unfollow button graphics
                clickedButton.setText(" Follow");

                System.out.println("[INFO] " + currentUserUsername + " stopped following " + selectedUserUsername);
            }
        } catch (Exception e) {
            stageManager.showInfoMessage("INFO", "An error occurred. Please try again in a while.");
            System.err.println("[ERROR] onClickFollowButton@ControllerObjectUser.java raised an exception: " + e.getMessage());
        }
    }
}
