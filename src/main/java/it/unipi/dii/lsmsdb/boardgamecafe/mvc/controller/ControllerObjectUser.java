package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.UserService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class ControllerObjectUser {
    @FXML
    private Button followButton;
    @FXML
    private Button deleteUserButton;
    @FXML
    private Button banUserButton;
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
    @FXML
    protected AnchorPane userCardAnchorPane;

    private UserModelMongo user;

    @Autowired
    private ModelBean modelBean;

    private StageManager stageManager;

    private static GenericUserModelMongo currentUser;
    private static List<String> currentUserFollowedList;

    @Autowired
    private UserDBNeo4j userDBNeo4j;
    @Autowired
    private UserService userService;

    @Autowired
    @Lazy
    public ControllerObjectUser(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    public ControllerObjectUser() {
    }

    public void setData(UserModelMongo user) {
        this.user = user;

        if (modelBean.getBean(Constants.IS_ADMIN) == null) {
            currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        } else {
            currentUser = (AdminModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        }
        currentUserFollowedList = (List<String>) modelBean.getBean(Constants.CURRENT_USER_FOLLOWED_LIST);

        if (Objects.equals(user.getUsername(), currentUser.getUsername())){
            this.followButton.setDisable(true);
        } else {
            this.followButton.setDisable(false);
        }

        if(modelBean.getBean(Constants.IS_ADMIN) == null){        // Ban and delete buttons are only visible to admin users
            this.banUserButton.setVisible(false);
            this.deleteUserButton.setVisible(false);
        } else {
            this.banUserButton.setVisible(true);
            this.deleteUserButton.setVisible(true);
        }

        if (currentUserFollowedList.contains(user.getUsername())) {
            followButton.setText(" Unfollow");
        } else {
            followButton.setText(" Follow");
        }

        String profileImageFilename = user.isBanned() ? "/images/bannedUser.png" : "/images/user.png";
        Image image = new Image(Objects.requireNonNull(getClass().
                                getResource(profileImageFilename)).toExternalForm());
        this.profileImage.setImage(image);
        this.firstNameLabel.setText(user.getName());
        this.lastNameLabel.setText(user.getSurname());
        this.nationalityLabel.setText(user.getNationality());
        this.usernameLabel.setText(user.getUsername());

        if (user.isBanned()) {      // We're creating the card of a banned user - only admins can see these cards
            this.followButton.setDisable(true);
            this.banUserButton.setText(" Unban");
        }

        followButton.setOnAction(event -> onClickFollowButton(user, (Button) event.getSource()));
        banUserButton.setOnAction(event -> onClickBanUserButton(user));
        deleteUserButton.setOnAction(event -> onClickDeleteUserButton(user));
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

    public void onClickDeleteUserButton(UserModelMongo user){
        boolean adminChoice = stageManager.showDeleteUserInfoMessage();
        if (!adminChoice) {
            return;
        }

        if (!userService.deleteUser(user)) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Please try again in a while.");
        } else {
            stageManager.showInfoMessage("INFO", "'" + user.getUsername() + "' has been successfully deleted from the application.");
            modelBean.putBean(Constants.DELETED_USER, user);
        }
    }

    public void onClickBanUserButton(UserModelMongo user){
        boolean adminChoice;

        if (user.isBanned()) {              // User already banned - unban him
            adminChoice = stageManager.showUnBanUserInfoMessage();
            if (!adminChoice) {
                return;
            }

            if (!userService.unbanUser(user)) {
                stageManager.showInfoMessage("INFO", "Something went wrong. Please try again in a while.");
            } else {
                stageManager.showInfoMessage("INFO", "'" + user.getUsername() + "' has been successfully unbanned from the application.");
                modelBean.putBean(Constants.UNBANNED_USER, user);
            }
        } else {                // Banning user
            adminChoice = stageManager.showBanUserInfoMessage();
            if (!adminChoice) {
                return;
            }

            if (!userService.banUser(user)) {
                stageManager.showInfoMessage("INFO", "Something went wrong. Please try again in a while.");
            } else {
                stageManager.showInfoMessage("INFO", "'" + user.getUsername() + "' has been successfully banned from the application.");
                modelBean.putBean(Constants.BANNED_USER, user);
            }
        }
    }
}
