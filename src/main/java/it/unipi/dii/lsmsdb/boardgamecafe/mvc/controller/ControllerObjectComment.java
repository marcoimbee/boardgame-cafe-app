package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.CommentDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.CommentDBNeo4j;
//import it.unipi.dii.lsmsdb.boardgamecafe.services.CommentService;
import it.unipi.dii.lsmsdb.boardgamecafe.services.PostService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class ControllerObjectComment {

    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    protected Label usernameLabel;
    @FXML
    protected Label timestampLabel;
    @FXML
    protected Label postLabel;
    @FXML
    protected TextArea bodyTextLabel;

    private CommentModelMongo comment;

    @Autowired
    private CommentDBMongo commentDBMongo;
    @Autowired
    private CommentDBNeo4j commentDBNeo4j;
    @Autowired
    private PostDBMongo postDBMongo;
    @Autowired
    private PostService postService;

    @Autowired
    private ModelBean modelBean;

    private static GenericUserModelMongo currentUser;

    private Consumer<String> deletedCommentCallback;

    @Autowired
    @Lazy
    private StageManager stageManager;

    public ControllerObjectComment() {}

    public void setData(CommentModelMongo comment, PostModelMongo post, Consumer<String> deletedCommentCallback)
    {
        currentUser = (GenericUserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        if (currentUser == null)
            throw new RuntimeException("No logged");
        if (!currentUser.get_class().equals("admin"))
            currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        else
            currentUser = (AdminModelMongo) modelBean.getBean(Constants.CURRENT_USER);

        this.comment = comment;
        this.editButton.setDisable(false);
        this.deleteButton.setDisable(false);
        String creationDate = comment.getTimestamp().toString();

        // Setting up callback functions
        this.deletedCommentCallback = deletedCommentCallback;

        this.usernameLabel.setText(comment.getUsername());
        this.timestampLabel.setText(creationDate);
        this.postLabel.setText(post.getTitle());
        this.bodyTextLabel.setText(comment.getText());

        // Removing the possibility to edit and delete a comment if the current user is not the author
        if (!currentUser.getUsername().equals(comment.getUsername())) {
            editButton.setVisible(false);
            deleteButton.setVisible(false);
        }

        if (currentUser.get_class().equals("admin")) {
            editButton.setVisible(false);
            deleteButton.setVisible(true);
        }


        // Setting up button listeners
        deleteButton.setOnAction(event -> onClickDeleteButton(post, comment));
        editButton.setOnAction(event -> onClickEditButton(post, comment));
    }

    @FXML
    public void onClickEditButton(PostModelMongo post, CommentModelMongo comment) {
        System.out.println("[DEBUG] Switching to edit page...");
        modelBean.putBean(Constants.SELECTED_COMMENT, comment);
        stageManager.showWindow(FxmlView.EDIT_COMMENT);            // Do not close underlying page, just show the little comment editing window
    }

    @FXML
    public void onClickDeleteButton(PostModelMongo post, CommentModelMongo comment) {
        boolean userChoice = stageManager.showDeleteCommentInfoMessage();
        if (!userChoice) {
            return;
        }

        try {
            postService.deleteComment(comment, post);

            System.out.println("[INFO] Successful comment deletion.");

            modelBean.putBean(Constants.DELETED_COMMENT, comment);
            deletedCommentCallback.accept(comment.getId());
        } catch (Exception ex) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Try again in a while.");
            System.err.println("[ERROR] onClickDeleteButton@ControllerObjectComment.java raised an exception: " + ex.getMessage());
        }
    }
}
