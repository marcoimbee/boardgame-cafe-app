package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.services.CommentService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

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
    private CommentService commentService; // Iniezione del servizio

    private StageManager stageManager;
    @Autowired
    @Lazy
    public ControllerObjectComment(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    public ControllerObjectComment() {
    }

    public void setData(CommentModelMongo comment, PostModelMongo post) {

        this.comment = comment;
        this.editButton.setDisable(false);
        this.deleteButton.setDisable(false);
        String creationDate = comment.getTimestamp().toString();

        this.usernameLabel.setText(comment.getUsername());
        this.timestampLabel.setText(creationDate);
        this.postLabel.setText(post.getTitle());
        this.bodyTextLabel.setText(comment.getText());
    }

    @FXML
    public void onClickEditButton(ActionEvent event) {
        // Implementazione per rimuovere il post
        String title = "Work in Progress";
        String message = "" +
                "A breve verrai reindirizzato alla pagina in cui puoi modificare il Commento.\n";
        stageManager.showInfoMessage(title, message);
    }

    @FXML
    public void onClickDeleteButton(ActionEvent event) {
        // Implementazione per commentare il post
        String title = "Work in Progress";
        String message = "" +
                "A breve avrai la possibilità di eliminare il Commento.\n";
        stageManager.showInfoMessage(title, message);
    }

}
