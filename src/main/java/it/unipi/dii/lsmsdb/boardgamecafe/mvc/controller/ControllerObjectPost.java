package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;


import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ControllerObjectPost {

    @FXML
    private Button commentButton;
    @FXML
    private Button likeButton;
    @FXML
    private Button removeButton;
    @FXML
    protected Label authorLabel;
    @FXML
    protected Label timestampLabel;
    @FXML
    protected Label commentsLabel;
    @FXML
    protected Label likesLabel;
    @FXML
    protected TextArea textTitleLabel;


    private static final Map<String, Image> likeCache = new HashMap<>();
    private static final Map<String, String> commentCache = new HashMap<>();

    public ControllerObjectPost() {
    }

    public void setData(PostModelMongo post) {

        this.likeButton.setDisable(true);
        this.commentButton.setDisable(true);
        this.removeButton.setDisable(true);

        String creationDate = post.getTimestamp().toString();
        String commentsCounter = "";

        if (commentCache.containsKey(post.getId())) {
            commentsCounter = commentCache.get(post.getId());
        } else {
            // Calculate and cache comments count
            int sizeCommentsList = post.getComments().size();
            commentsCounter = String.valueOf(sizeCommentsList);
            commentCache.put(post.getId(), commentsCounter);
        }

        authorLabel.setText(post.getUsername());
        timestampLabel.setText(creationDate);
        commentsLabel.setText(commentsCounter);
        likesLabel.setText("10k");
        textTitleLabel.setText("TITLE:" + " " + post.getTitle());

    }

    public void removePost(ActionEvent event) {
    }

    public void likeDislikePost(ActionEvent event) {
    }
}
