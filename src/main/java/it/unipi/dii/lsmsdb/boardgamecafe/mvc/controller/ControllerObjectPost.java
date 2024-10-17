package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.PostService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
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

    private static final Map<String, String> commentCache = new HashMap<>();

    @Autowired
    private PostService postService; // Iniezione del servizio

    @Autowired
    private PostDBNeo4j postDBNeo4j;

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
            // Calcola e memorizza il conteggio dei commenti
            int sizeCommentsList = post.getComments().size();
            commentsCounter = String.valueOf(sizeCommentsList);
            commentCache.put(post.getId(), commentsCounter);
        }

        authorLabel.setText(post.getUsername());
        timestampLabel.setText(creationDate);
        commentsLabel.setText(commentsCounter);

        updateLikesLabel(post.getId());
        //updateLikeButton(post.getUsername(), post.getId());
        textTitleLabel.setText("TITLE:" + " " + post.getTitle());
    }

    public void likeDislikePost(ActionEvent event) {
        String username = "CurrentUsername"; // Ottieni l'username attuale, ad esempio dal contesto dell'app
        String postId = "CurrentPostId"; // Ottieni l'ID del post attuale

        // Effettua l'operazione di like/dislike
        postService.likeOrDislikePost(username, postId);

        // Aggiorna il conteggio dei like
        updateLikesLabel(postId);
        //updateLikeButton(username, postId);
    }

    private void updateLikesLabel(String postId) {
        int likeCount = postDBNeo4j.findTotalLikesByPostID(postId);
        likesLabel.setText(String.valueOf(likeCount));
    }

    private void updateLikeButton(String usernameCurrUser, String postId) {
        if (postService.hasLikedPost(usernameCurrUser, postId)) {
            likeButton.setText("Dislike");
        } else {
            likeButton.setText("Like");
        }
    }

    public void removePost(ActionEvent event) {
        // Implementazione per rimuovere il post
    }

    public void commentPost(ActionEvent event) {
        // Implementazione per commentare il post
    }
}
