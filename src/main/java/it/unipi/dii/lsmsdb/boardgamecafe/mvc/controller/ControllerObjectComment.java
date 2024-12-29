package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.CommentDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.CommentDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.CommentService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private CommentDBMongo commentDBMongo;
    @Autowired
    private UserDBMongo userDBMongo;
    @Autowired
    private CommentDBNeo4j commentDBNeo4j;
    @Autowired
    private PostDBMongo postDBMongo;
    @Autowired
    private CommentService serviceComment;
    @Autowired
    private ModelBean modelBean;
    @Autowired
    @Lazy
    private StageManager stageManager;

    private CommentModelMongo comment;
    private UserModelMongo commentAuthor;
    private static GenericUserModelMongo currentUser;

    private Consumer<String> deletedCommentCallback;

    public ControllerObjectComment() {}

    public void setData(CommentModelMongo comment, PostModelMongo post, Consumer<String> deletedCommentCallback) {
        currentUser = (GenericUserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        if (currentUser == null)
            throw new RuntimeException("No logged");
        if (!currentUser.get_class().equals("admin"))
            currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        else
            currentUser = (AdminModelMongo) modelBean.getBean(Constants.CURRENT_USER);

        this.comment = comment;
        this.commentAuthor = (UserModelMongo) userDBMongo.findByUsername(comment.getUsername(), false).get();       // Retrieving the comment's author

        this.editButton.setDisable(false);
        this.deleteButton.setDisable(false);
        String creationDate = comment.getTimestamp().toString();

        // Setting up callback functions
        this.deletedCommentCallback = deletedCommentCallback;

        // Setting up labels
        if (commentAuthor.isBanned()) {
            this.usernameLabel.setText("[Banned user]");
            this.bodyTextLabel.setText("[Banned user]");
        } else {
            this.usernameLabel.setText(comment.getUsername());
            this.bodyTextLabel.setText(comment.getText());
        }

        this.timestampLabel.setText(creationDate);
        this.postLabel.setText(post.getTitle());

        // Removing the possibility to edit and delete a comment if the current user is not the author
        if (!currentUser.getUsername().equals(comment.getUsername())) {
            editButton.setVisible(false);
            deleteButton.setVisible(false);
        }

        // Setting up admin controls
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
            serviceComment.deleteComment(comment, post);

            System.out.println("[INFO] Successful comment deletion.");

            modelBean.putBean(Constants.DELETED_COMMENT, comment);
            deletedCommentCallback.accept(comment.getId());
        } catch (Exception ex) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Try again in a while.");
            System.err.println("[ERROR] onClickDeleteButton@ControllerObjectComment.java raised an exception: " + ex.getMessage());
        }
    }
}
