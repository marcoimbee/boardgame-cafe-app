package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.BoardgameDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;


@Component
public class ControllerViewEditPostPage implements Initializable {
    @FXML
    public Button cancelButton;
    @FXML
    public Button submitButton;
    @FXML
    public TextField bodyTextLabel;
    @FXML
    public TextField titleTextLabel;
    @FXML
    public Label titleBodyText;
    @FXML
    public Label postTitle;
    @FXML
    public TextField boardgameTextLabel;

    private static PostModelMongo selectedPost;

    @Autowired
    private ModelBean modelBean;

    @Autowired
    @Lazy
    private StageManager stageManager;

    @Autowired
    private PostDBMongo postDBMongo;
    @Autowired
    private BoardgameDBMongo boardgameDBMongo;

    public ControllerViewEditPostPage() {}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectedPost = (PostModelMongo) modelBean.getBean(Constants.SELECTED_POST);

        this.boardgameTextLabel.setText(selectedPost.getTag());
        this.boardgameTextLabel.setDisable(true);
        this.titleTextLabel.setText(selectedPost.getTitle());
        this.bodyTextLabel.setText(selectedPost.getText());

        // set boardgame names bean if not already set - needed to check the updated boardgame tag existence
        if (modelBean.getBean(Constants.BOARDGAME_LIST) == null ) {
            List<String> boardgameNames = boardgameDBMongo.getBoardgameTags();
            modelBean.putBean(Constants.BOARDGAME_LIST, boardgameNames);
        }
    }

    private boolean noChangesWereMade(String updatedTitle, String updatedBody, String updatedBoardgame) {
        return (updatedBody.equals(selectedPost.getText()) &&
                updatedTitle.equals(selectedPost.getTitle()) &&
                updatedBoardgame.equals(selectedPost.getTag()));
    }

    public void onClickCancelButton() {
        String updatedBoardgame = this.boardgameTextLabel.getText();
        String updatedTitle = this.titleTextLabel.getText();
        String updatedBody = this.bodyTextLabel.getText();

        if (noChangesWereMade(updatedTitle, updatedBody, updatedBoardgame)) {
            stageManager.closeStage();
            return;
        }

        boolean userChoice = stageManager.showUpdatePostInfoMessage();
        if (userChoice)
            stageManager.closeStage();
    }

    public void onClickSubmitButton() {
        String updatedBoardgame = this.boardgameTextLabel.getText();
        String updatedTitle = this.titleTextLabel.getText();
        String updatedBody = this.bodyTextLabel.getText();

        if (noChangesWereMade(updatedTitle, updatedBody, updatedBoardgame)) {      // Nothing was updated, ok to close and no further action
            stageManager.closeStage();
            return;
        }

        // Changes were made, actually update post now
        try {
            // Update mongoDB post in post collection
            PostModelMongo updatedPost = selectedPost;
            updatedPost.setTag(updatedBoardgame);
            updatedPost.setTitle(updatedTitle);
            updatedPost.setText(updatedBody);
            postDBMongo.updatePost(selectedPost.getId(), updatedPost);

            // Setting updated in model bean to retrieve them in post details page for UI update
            modelBean.putBean(Constants.UPDATED_POST, updatedPost);

            System.out.println("[INFO] Successfully updated a post.");
            stageManager.closeStage();
        } catch (Exception ex) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Try again  in a while.");
            System.err.println("[ERROR] onClickSubmitButton@ControllerViewEditPostPage.java raised an exception: " + ex.getMessage());
        }
    }
}
