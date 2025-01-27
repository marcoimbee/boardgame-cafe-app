package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.AdminModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.PostService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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

    @Autowired
    private PostService postService;
    @Autowired
    private PostDBNeo4j postDBNeo4j;
    @Autowired
    private PostDBMongo postDBMongo;
    @Autowired
    private UserDBMongo userDBMongo;
    @Autowired
    private ModelBean modelBean;

    private PostModelMongo post;
    private PostListener postListener;
    private static GenericUserModelMongo currentUser;
    private StageManager stageManager;
    private Consumer<String> deletedPostCallback;
    private final List<String> buttonLikeMessages = new ArrayList<>(Arrays.asList("Like", "Dislike"));
    private Boolean likeIsPresent = null;
    private UserModelMongo postAuthor;

    @Autowired
    @Lazy
    public ControllerObjectPost(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    public ControllerObjectPost() {}

    public void setData(PostModelMongo post, PostListener listener, Consumer<String> deletedPostCallback) {
        currentUser = (GenericUserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        if (currentUser != null){
            if (!currentUser.get_class().equals("admin")) {
                currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
            } else {
                currentUser = (AdminModelMongo) modelBean.getBean(Constants.CURRENT_USER);
            }
        }
        this.post = post;
        this.postListener = listener;

        String creationDate = post.getTimestamp().toString();

        this.postAuthor = (UserModelMongo) userDBMongo.findByUsername(post.getUsername(), false).get();
        if (postAuthor.isBanned()) {
            this.authorLabel.setText("[Banned user]");
            this.textTitleLabel.setText("[Banned user]");
        } else {
            this.authorLabel.setText(post.getUsername());
            textTitleLabel.setText("TITLE:" + " " + post.getTitle());
        }

        this.timestampLabel.setText(creationDate);
        this.commentsLabel.setText(String.valueOf(post.getComments().size()));
        if (post.getTag() == null) {
            this.tagBoardgameLabel.setText("[ No Reference ]");
        } else {
            this.tagBoardgameLabel.setText(post.getTag());
        }

        // Buttons setting
        boolean loggedAsAdmin = currentUser instanceof AdminModelMongo;
        if(post != null && currentUser != null) {
            if (!loggedAsAdmin && !currentUser.getUsername().equals(post.getUsername())) {
                deleteButton.setVisible(false);         // Current user is not the creator of the post, then he must be unable to remove it
            } else {
                deleteButton.setVisible(true);
                deleteButton.setDisable(false);
            }
        }

        if (currentUser == null) {
            likeButton.setDisable(true);
            deleteButton.setDisable(true);
            return;
        }

        this.deletedPostCallback = deletedPostCallback;
        updateLikesLabel(null, post);
        setTextLikeButton(post, currentUser.getUsername(), null, null);
        deleteButton.setOnAction(event -> onClickDeleteButton(post));

        if (loggedAsAdmin) {
            likeButton.setDisable(true);
            return;
        }

        likeButton.setOnAction(event -> onClickLikeButton(post, event));    // Avoided if logged as Admin
    }

    public void setTextLikeButton(PostModelMongo post, String currentUser, Button button, FontAwesomeIconView icon) {
        Button workingButton = (button != null) ? button : this.likeButton;     // NOTE: if the like exists, the button serves as a dislike button
        FontAwesomeIconView workingIcon = (icon != null) ? icon : this.iconLikeButton;
        this.likeIsPresent = this.postService.hasLikedPost(currentUser, post.getId());
        if (button == null && icon == null) {     // First call
            this.postDBNeo4j.setLikeCount(post.getId(), post.getLikeCount());
        }
        workingIcon.setIcon((this.likeIsPresent) ? FontAwesomeIcon.THUMBS_DOWN : FontAwesomeIcon.THUMBS_UP);
        workingButton.setText((this.buttonLikeMessages.get((this.likeIsPresent) ? 1 : 0)));
    }

    public void onClickLikeButton(PostModelMongo post, ActionEvent event) {
        String username = currentUser.getUsername();
        String postId = post.getId();
        postService.likeOrDislikePost(username, postId);
        FontAwesomeIconView icon = (FontAwesomeIconView) ((Button)event.getSource()).getGraphic();
        setTextLikeButton(post, username, (Button) event.getSource(), icon);
        updateLikesLabel(event, post);
    }

    private void updateLikesLabel(ActionEvent event, PostModelMongo post) {
        post = postDBMongo.findById(post.getId()).get();
        Label workingLikeCountLbl = (event == null) ?
                this.counterLikesLabel : (Label) ((Button) event.getSource()).getParent().lookup("#counterLikesLabel");
        workingLikeCountLbl.setText(String.valueOf(post.getLikeCount()));
    }

    public void onClickDeleteButton(PostModelMongo post) {
        boolean userChoice = stageManager.showDeletePostInfoMessage();
        if (!userChoice) {
            return;
        }

        try {
            postService.deletePost(post);

            System.out.println("[INFO] Successful post deletion");

            deletedPostCallback.accept(post.getId());
        } catch (Exception ex) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Please try again in a while.");
            System.err.println("[ERROR] onClickDeleteButton()@ControllerObjectPost.java raised an exception: " + ex.getMessage());
        }
    }
}
