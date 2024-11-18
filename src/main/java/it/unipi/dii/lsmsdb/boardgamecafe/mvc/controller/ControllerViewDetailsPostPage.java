package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.CommentDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.CommentService;
import it.unipi.dii.lsmsdb.boardgamecafe.services.PostService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;


@Component
public class ControllerViewDetailsPostPage implements Initializable {

    //********** Buttons **********
    @FXML
    private Button likdeButton;
    @FXML
    private Button editButton;
    @FXML
    private Button addCommentButton;
    @FXML
    private Button nextButton;
    @FXML
    private Button previousButton;
    @FXML
    private Button closeButton;
    @FXML
    private Button refreshButton;
    @FXML
    private Button deleteButton;

    //********** Labels **********
    @FXML
    protected Label usernameLabel;
    @FXML
    protected Label timestampLabel;
    @FXML
    protected Label counterCommentsLabel;
    @FXML
    protected Label counterLikesLabel;
    @FXML
    protected TextArea postTitleTextArea;
    @FXML
    protected TextArea postBodyTextArea;
    @FXML
    protected Label tagBoardgameLabel;

    //********** Useful Variables **********
    @FXML
    private GridPane commentGridPane;
    @FXML
    private ScrollPane scrollSet;
    @Autowired
    private CommentDBMongo commentDBMongo;
    @Autowired
    private PostDBMongo postDBMongo;
    @Autowired
    private ControllerObjectComment controllerObjectComment;
    @Autowired
    private ModelBean modelBean;
    @Autowired
    private UserDBNeo4j userNeo4jDB;
    @Autowired
    private CommentService serviceComment;
    @Autowired
    private PostService postService;

    private List<CommentModelMongo> comments = new ArrayList<>();

    private PostModelMongo post;

    private static UserModelMongo currentUser;

    //Utils Variables
    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;
    private final static int SKIP = 10; //how many posts to skip per time
    private final static int LIMIT = 10; //how many posts to show for each page

    @Autowired
    @Lazy
    private StageManager stageManager;

    private Consumer<String> deletedCommentCallback;

    public ControllerViewDetailsPostPage() {}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("[INFO] Loaded ControllerViewDetailsPostPage");
        currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);

        this.previousButton.setDisable(true);
        this.nextButton.setDisable(true);
        resetPage();
        comments.clear();

        post = (PostModelMongo) modelBean.getBean(Constants.SELECTED_POST);
        this.usernameLabel.setText(post.getUsername());
        this.tagBoardgameLabel.setText(post.getTag());
        this.timestampLabel.setText(post.getTimestamp().toString());
        this.postTitleTextArea.setText(post.getTitle());
        this.postBodyTextArea.setText(post.getText());
        this.counterLikesLabel.setText(String.valueOf(post.getLikeCount()));
        this.counterCommentsLabel.setText(String.valueOf(post.getComments().size()));

        post.getComments().sort(Comparator.comparing(CommentModelMongo::getTimestamp).reversed());
        comments.addAll(post.getComments());
        fillGridPane();

        // Setting up buttons depending on if the current user is who created the post that's being visualized
        if (!currentUser.getUsername().equals(post.getUsername())) {
            editButton.setVisible(false);       // Making the edit button invisible
            deleteButton.setVisible(false);     // Making the delete button invisible
        }

        // Page focus listener - needed to potentially update UI when coming back from a post update window
        commentGridPane.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (newScene != null) {
                Stage stage = (Stage) newScene.getWindow();
                stage.focusedProperty().addListener((observableFocus, wasFocused, isNowFocused) -> {
                    if (isNowFocused) {
                        onFocusGained();            // Update UI after post updates
                    }
                });
            }
        });
    }

    public void onFocusGained() {
        PostModelMongo updatedPost = (PostModelMongo) modelBean.getBean(Constants.SELECTED_POST);
        this.tagBoardgameLabel.setText(updatedPost.getTag());
        this.postTitleTextArea.setText(updatedPost.getTitle());
        this.postBodyTextArea.setText(updatedPost.getText());

        // Potentially update a comment
        CommentModelMongo updatedComment = (CommentModelMongo) modelBean.getBean(Constants.UPDATED_COMMENT);
        if (updatedComment != null) {
            modelBean.putBean(Constants.UPDATED_COMMENT, null);

            comments.replaceAll(comment -> comment.getId().equals(updatedComment.getId()) ? updatedComment : comment);
            fillGridPane();
        }
    }

    // Called whenever the author user of a comment decides to delete that comment. This method updates the comments list and updates UI
    public void updateUIAfterCommentDeletion(String deletedCommentId) {
        comments.removeIf(comment -> comment.getId().equals(deletedCommentId));
        this.counterCommentsLabel.setText(String.valueOf(comments.size()));
        fillGridPane();
    }

    public void onClickDeleteButton() {
        boolean userChoice = stageManager.showDeletePostInfoMessage();
        if (!userChoice) {
            return;
        }

        try {
            // Delete Neo4J post node + MongoDB post document + all its comments
            postService.deletePost(post);

            System.out.println("[INFO] A post has been successfully deleted.");

            // Set model bean variable to tell destination page what post was deleted, so that UI can be dynamically updated
            modelBean.putBean(Constants.DELETED_POST, post.getId());

            // Close post details window
            stageManager.closeStage();
        } catch (Exception ex) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Try again in a while.");
            System.err.println("[ERROR] onClickDeleteButton@ControllerViewDetailsPostPage.java raised an exception: " + ex.getMessage());
        }
    }

    public void onClickEditButton() {
        stageManager.showWindow(FxmlView.EDIT_POST);            // Do not close underlying page, just show the little post editing window
    }

    public void onClickRefreshButton() {
        resetPage();
        comments.clear();
        Optional<PostModelMongo> postFromMongo = postDBMongo.findById(this.post.getId());
        postFromMongo.ifPresent(postModelMongo -> post = postModelMongo);
        post.getComments().sort(Comparator.comparing(CommentModelMongo::getTimestamp).reversed());
        comments.addAll(post.getComments());
        fillGridPane();
    }

    public void onClickCloseButton() {
        stageManager.closeStageButton(this.closeButton);
    }

    public void onClickLikeButton() {
    }
    @FXML
    void onClickNext() {
        //clear variables
        commentGridPane.getChildren().clear();
        comments.clear();

        //update the skipcounter
        skipCounter += SKIP;

        //retrieve boardgames
        comments.addAll(getData(this.post.getId()));
        //put all boardgames in the Pane
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    @FXML
    void onClickPrevious() {
        //clear variables
        commentGridPane.getChildren().clear();
        comments.clear();

        //update the skipcounter
        skipCounter -= SKIP;

        //retrieve boardgames
        comments.addAll(getData(this.post.getId()));
        //put all boardgames in the Pane
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    void resetPage() {
        commentGridPane.getChildren().clear();
        skipCounter = 0;
        previousButton.setDisable(true);
        nextButton.setDisable(true);
        scrollSet.setVvalue(0);
    }

    void prevNextButtonsCheck(List<CommentModelMongo> comments){
        if((comments.size() > 0)){
            if((comments.size() < LIMIT)){
                if(skipCounter <= 0 ){
                    previousButton.setDisable(true);
                    nextButton.setDisable(true);
                }
                else{
                    previousButton.setDisable(false);
                    nextButton.setDisable(true);
                }
            }
            else{
                if(skipCounter <= 0 ){
                    previousButton.setDisable(true);
                    nextButton.setDisable(false);
                }
                else{
                    previousButton.setDisable(false);
                    nextButton.setDisable(false);
                }
            }
        }
        else{
            if(skipCounter <= 0 ){
                previousButton.setDisable(true);
                nextButton.setDisable(true);
            }
            else {
                previousButton.setDisable(false);
                nextButton.setDisable(true);
            }
        }
    }

    private List<CommentModelMongo> getData(String postId){

        List<CommentModelMongo> comments = commentDBMongo.findByPost(postId);
        if (comments.isEmpty()) {
            stageManager.showInfoMessage("Info Comments", "No Comments Yet");
        }
        prevNextButtonsCheck(comments);
        return comments;
    }

    public void onClickAddCommentButton() {
        try {
            this.addCommentButton.setDisable(true);
            Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTCREATECOMMENT.getFxmlFile());        // Loading FXML

            TextField commentTextArea = (TextField) loadViewItem.lookup("#bodyTextLabel");
            Button submitCommentButton = (Button) loadViewItem.lookup("#submitButton");
            Button cancelCommentButton = (Button) loadViewItem.lookup("#cancelButton");
            commentTextArea.setPromptText("Write your comment here...");

            AnchorPane addCommentBox = new AnchorPane();
            addCommentBox.getChildren().add(loadViewItem);

            if (comments.isEmpty()){            // Comment box displaying
                resetPage();
                commentGridPane.add(addCommentBox, 0, rowGridPane);
            } else {
                resetPage();
                commentGridPane.add(addCommentBox, 0, 0);
            }
            GridPane.setMargin(addCommentBox, new Insets(8, 5, 10, 90));

            // Submit comment button behavior
            submitCommentButton.setOnAction(e -> {
                String commentText = commentTextArea.getText();
                if (commentText.isEmpty()) {
                    stageManager.showInfoMessage("Error", "A comment cannot be empty.");
                    return;
                }

                CommentModelMongo newComment = new CommentModelMongo(
                        this.post.getId(),                // ID of the post that's being commented
                        currentUser.getUsername(),        // Current user is commenting this post
                        commentText,
                        new Date()                        // Comment creation date
                );

                UserModelNeo4j currentUserNeo = userNeo4jDB.findByUsername(currentUser.getUsername()).get();
                boolean savedComment = serviceComment.insertComment(newComment, this.post, currentUserNeo);     // MongoDB + Neo4J comment insertion

                if (savedComment) {
                    stageManager.showInfoMessage("Success", "Comment added successfully.");

                    comments.add(0, newComment);      // Adding the new comment to the comment list

                    fillGridPane();             // Displaying update

                    this.addCommentButton.setDisable(false);        // Restore button
                    modelBean.putBean(Constants.ADDED_COMMENT, newComment);         // Saving info about the newly inserted comment to update UI in posts feed page

                    this.counterCommentsLabel.setText(String.valueOf(comments.size()));  // Update post details page UI - increase comment count
                } else {
                    stageManager.showInfoMessage("Error", "Failed to add comment.");
                }
            });

            // Discard comment button behavior
            cancelCommentButton.setOnAction(e -> {
                boolean userChoice = stageManager.showDiscardCommentInfoMessage();
                if (userChoice) {
                    this.addCommentButton.setDisable(false);
                    resetPage();
                    fillGridPane();
                }
            });
        } catch (Exception e) {
            stageManager.showInfoMessage("ERROR", "Something went wrong. Please try again in a while.");
        }
    }

    private void loadViewMessageInfo(){
        Parent loadViewItem = stageManager.loadViewNode(FxmlView.INFOMSGCOMMENTS.getFxmlFile());
        AnchorPane noContentsYet = new AnchorPane();
        noContentsYet.getChildren().add(loadViewItem);

        if (!comments.isEmpty()){
            resetPage();
            commentGridPane.add(noContentsYet, 0, rowGridPane);
        } else {
            resetPage();
            commentGridPane.add(noContentsYet, 0, 0);
        }

        GridPane.setMargin(noContentsYet, new Insets(330, 100, 100, 265));
    }

    @FXML
    void fillGridPane() {
        commentGridPane.getChildren().clear();

        if (comments.size() == 1) {         // Needed to correctly position a single element in the gridpane
            columnGridPane = 0;
            rowGridPane = 0;
        } else {
            columnGridPane = 0;
            rowGridPane = 1;
        }

        try {
            if (comments.isEmpty()) {
                loadViewMessageInfo();
            } else {
                for (CommentModelMongo comment : comments) {
                    AnchorPane commentNode = createCommentViewNode(comment);
                    addCommentToGridPane(commentNode);
                }
            }
        } catch (Exception e) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Try again in a while.");
            System.err.println("[ERROR] fillGridPane@ControllerViewDetailsPostPage.java raised an exception: " + e.getMessage());
        }
    }

    private AnchorPane createCommentViewNode(CommentModelMongo comment) {
        Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTCOMMENT.getFxmlFile());
        AnchorPane anchorPane = new AnchorPane();
        anchorPane.getChildren().add(loadViewItem);

        // Setting up what method should be called upon comment deletion
        deletedCommentCallback = this::updateUIAfterCommentDeletion;

        // Setting comment data - including callbacks for actions to be taken upon comment modification or deletion
        controllerObjectComment.setData(comment, this.post, deletedCommentCallback);

        return anchorPane;
    }

    private void addCommentToGridPane(AnchorPane commentNode) {
        if (columnGridPane == 1) {
            columnGridPane = 0;
            rowGridPane++;
        }

        commentGridPane.add(commentNode, columnGridPane++, rowGridPane); //(child,column,row)

        commentGridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
        commentGridPane.setPrefWidth(500);
        commentGridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);

        commentGridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
        commentGridPane.setPrefHeight(400);
        commentGridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);

        GridPane.setMargin(commentNode, new Insets(4, 5, 10, 90));
    }
}
