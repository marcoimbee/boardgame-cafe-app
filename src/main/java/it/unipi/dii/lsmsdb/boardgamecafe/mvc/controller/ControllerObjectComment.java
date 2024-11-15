package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.CommentDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.CommentDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.CommentService;
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
    private ModelBean modelBean;

    private static UserModelMongo currentUser;

    private Consumer<String> deletedCommentCallback;

    private StageManager stageManager;
    @Autowired
    @Lazy
    public ControllerObjectComment(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    public ControllerObjectComment() {
    }

    public void setData(CommentModelMongo comment, PostModelMongo post, Consumer<String> deletedCommentCallback) {
        currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);

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

        // Setting up button listeners
        deleteButton.setOnAction(event -> onClickDeleteButton(post, comment));
        editButton.setOnAction(event -> onClickEditButton(post, comment));
    }

    @FXML
    public void onClickEditButton(PostModelMongo post, CommentModelMongo comment) {             // TODO TODO TODO TODO !!!!!
        // Implementazione per rimuovere il post
        String title = "Work in Progress";
        String message = "" +
                "A breve verrai reindirizzato alla pagina in cui puoi modificare il Commento.\n";
        stageManager.showInfoMessage(title, message);
    }

    @FXML
    public void onClickDeleteButton(PostModelMongo post, CommentModelMongo comment) {
        boolean userChoice = stageManager.showDeleteCommentInfoMessage();
        if (!userChoice) {
            return;
        }

        try {
            // Neo4J comment deletion
            commentDBNeo4j.deleteAndDetachComment(comment.getId());

            // MongoDB comment deletion
            commentDBMongo.deleteComment(comment);
            postDBMongo.deleteCommentFromPost(post, comment);

            System.out.println("[INFO] Successful comment deletion.");

            deletedCommentCallback.accept(comment.getId());
        } catch (Exception ex) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Try again in a while.");
            System.err.println("[ERROR] onClickDeleteButton@ControllerObjectComment.java raised an exception: " + ex.getMessage());
        }
    }
}
