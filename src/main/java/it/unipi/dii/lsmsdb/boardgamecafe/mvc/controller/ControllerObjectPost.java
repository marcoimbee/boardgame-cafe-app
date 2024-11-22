package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.CommentDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.PostService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;

@Component
public class ControllerObjectPost {

    @FXML
    private Button commentButton;
    @FXML
    private Button likeButton;
    @FXML
    private Button deleteButton;
    @FXML
    protected Label authorLabel;
    @FXML
    protected Label timestampLabel;
    @FXML
    protected Label commentsLabel;
    @FXML
    protected Label counterLikesLabel;
    @FXML
    protected TextArea textTitleLabel;
    @FXML
    protected Label tagBoardgameLabel;
    @FXML
    FontAwesomeIconView iconLikeButton;
    private static final Map<String, String> commentCache = new HashMap<>();

    private PostModelMongo post;

    private PostListener postListener;
    @Autowired
    private PostService postService; // Iniezione del servizio
    @Autowired
    private PostDBNeo4j postDBNeo4j;
    @Autowired
    private PostDBMongo postDBMongo;
    @Autowired
    private CommentDBMongo commentDBMongo;

    @Autowired
    private ModelBean modelBean;

    private static UserModelMongo currentUser;
    private StageManager stageManager;

    private Consumer<String> deletedPostCallback;

    private final List<String> buttonLikeMessages = new ArrayList<>(Arrays.asList("Like", "Dislike"));

    private Boolean likeIsPresent = null;

    @Autowired
    @Lazy
    public ControllerObjectPost(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    public ControllerObjectPost() {
    }

    public void setData(PostModelMongo post, PostListener listener, Consumer<String> deletedPostCallback) {

        currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);

        this.post = post;
        this.postListener = listener;

        this.commentButton.setDisable(true);

        String creationDate = post.getTimestamp().toString();
        this.authorLabel.setText(post.getUsername());
        this.timestampLabel.setText(creationDate);
        this.commentsLabel.setText(String.valueOf(post.getComments().size()));
        if (post.getTag() == null) {
            this.tagBoardgameLabel.setText("[ No Reference ]");
        } else {
            this.tagBoardgameLabel.setText(post.getTag());
        }

        textTitleLabel.setText("TITLE:" + " " + post.getTitle());

        if(post != null && currentUser != null){
            // Buttons settings
            if (!currentUser.getUsername().equals(post.getUsername())) {
                deleteButton.setVisible(false);         // Current user is not the creator of the post, then he must be unable to remove it
            } else {
                deleteButton.setVisible(true);
                deleteButton.setDisable(false);
            }
        }

        this.deletedPostCallback = deletedPostCallback;
        updateLikesLabel(null, post);
        setTextLikeButton(post.getId(), currentUser.getUsername(), null, null);
        deleteButton.setOnAction(event -> onClickDeleteButton(post));
        likeButton.setOnAction(event -> onClickLikeButton(post, event));
    }

    public void setTextLikeButton(String postId, String currentUser, Button button, FontAwesomeIconView icon)
    // Se il like c'è, il button ha funzione dislike. Il contrario altrimenti
    {
        Button workingButton = (button != null) ? button : this.likeButton;
        FontAwesomeIconView workingIcon = (icon != null) ? icon : this.iconLikeButton;
        this.likeIsPresent = this.postService.hasLikedPost(currentUser, postId);
        workingIcon.setIcon((this.likeIsPresent) ? FontAwesomeIcon.THUMBS_DOWN : FontAwesomeIcon.THUMBS_UP);
        workingButton.setText((this.buttonLikeMessages.get((this.likeIsPresent) ? 1 : 0)));
    }

    public void onClickLikeButton(PostModelMongo post, ActionEvent event)
    {
        String username = currentUser.getUsername();
        String postId = post.getId();
        postService.likeOrDislikePost(username, postId);
        FontAwesomeIconView icon = (FontAwesomeIconView) ((Button)event.getSource()).getGraphic();
        setTextLikeButton(post.getId(), username, (Button) event.getSource(), icon);
        updateLikesLabel(event, post);
    }

    private void updateLikesLabel(ActionEvent event, PostModelMongo post)
    {
        Label workingLikeCountLbl = (event == null) ?
                this.counterLikesLabel : (Label) ((Button) event.getSource()).getParent().lookup("#counterLikesLabel");
        int likeCount = (event == null) ?
                post.getLikeCount() : postDBNeo4j.findTotalLikesByPostID(post.getId());
        post.setLikeCount(likeCount);
        workingLikeCountLbl.setText(String.valueOf(likeCount));
    }

    public void onClickDeleteButton(PostModelMongo post) {
        boolean userChoice = stageManager.showDeletePostInfoMessage();
        if (!userChoice) {
            return;
        }

        try {
            // Neo4J post deletion
            postDBNeo4j.deletePost(post.getId());

            // MongoDB post deletion
            postDBMongo.deletePost(post);
            commentDBMongo.deleteByPost(post.getId());

            System.out.println("[INFO] Successful post deletion");

            deletedPostCallback.accept(post.getId());
        } catch (Exception ex) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Try again in a while.");
            System.err.println("[ERROR] onClickDeleteButton@ControllerObjectPost.java raised an exception: " + ex.getMessage());
        }
    }
}

