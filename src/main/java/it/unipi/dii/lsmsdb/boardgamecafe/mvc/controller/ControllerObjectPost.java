package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.PostService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
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
    @FXML
    protected Label tagBoardgameLabel;

    private static final Map<String, String> commentCache = new HashMap<>();

    private PostModelMongo post;

    private PostListener postListener;
    @Autowired
    private PostService postService; // Iniezione del servizio
    @Autowired
    private PostDBNeo4j postDBNeo4j;

    @Autowired
    private ModelBean modelBean;

    private static UserModelMongo currentUser;

    public ControllerObjectPost() {
    }

    public void setData(PostModelMongo post, PostListener listener) {

        currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);

        this.post = post;
        this.postListener = listener;

        this.likeButton.setDisable(true);
        this.commentButton.setDisable(true);
        this.removeButton.setDisable(true);

        String creationDate = post.getTimestamp().toString();
//        String commentsCounter = "";
//        if (commentCache.containsKey(post.getId())) {
//            commentsCounter = commentCache.get(post.getId());
//        } else {
//            // Calcola e memorizza il conteggio dei commenti
//            int sizeCommentsList = post.getComments().size();
//            commentsCounter = String.valueOf(sizeCommentsList);
//            commentCache.put(post.getId(), commentsCounter);
//        }

        this.authorLabel.setText(post.getUsername());
        this.timestampLabel.setText(creationDate);
        this.commentsLabel.setText(String.valueOf(post.getComments().size()));
        if (post.getTag() == null) {
            this.tagBoardgameLabel.setText("[ No Reference ]");
        } else {
            this.tagBoardgameLabel.setText(post.getTag());
        }

        updateLikesLabel(post);

        //updateLikeButton(post.getUsername(), post.getId());
        textTitleLabel.setText("TITLE:" + " " + post.getTitle());

        // Buttons settings
        if (!currentUser.getUsername().equals(post.getUsername())) {
            removeButton.setVisible(false);         // Current user is not the creator of the post, then he must be unable to remove it
        }
    }

    public void likeDislikePost(ActionEvent event) {
        // Da sistemare l'aggiornamento dei like al post su cui viene premuto LIKE o DISLIKE
        String username = "CurrentUsername"; // Ottieni l'username attuale, ad esempio dal contesto dell'app
        String postId = "CurrentPostId"; // Ottieni l'ID del post attuale

        // Effettua l'operazione di like/dislike
        postService.likeOrDislikePost(username, postId);

        // Aggiorna il conteggio dei like
        // updateLikesLabel(postId);
        //updateLikeButton(username, postId);
    }

    private void updateLikesLabel(PostModelMongo post) {
        //int likeCount = postDBNeo4j.findTotalLikesByPostID(postId);
        likesLabel.setText(String.valueOf(post.getLikeCount()));
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
