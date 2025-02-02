package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.CommentModel;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
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

    @FXML
    private Button likeButton;
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
    @FXML
    private GridPane commentGridPane;
    @FXML
    private ScrollPane scrollSet;


    @Autowired
    @Lazy
    private StageManager stageManager;
    @Autowired
    private PostDBMongo postDBMongo;
    @Autowired
    private PostDBNeo4j postDBNeo4j;
    @Autowired
    private ControllerObjectComment controllerObjectComment;
    @Autowired
    private ModelBean modelBean;
    @Autowired
    private UserDBNeo4j userNeo4jDB;
    @Autowired
    private PostService postService;
    @Autowired
    private UserDBMongo userDBMongo;

    private List<CommentModel> comments = new ArrayList<>();
    private PostModelMongo post;
    private static GenericUserModelMongo currentUser;
    private boolean shiftDownSingleObjectGridPane;
    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;
    private final static int SKIP = 10;     // How many posts to skip each time
    private final static int LIMIT = 10;    // How many posts to show for each page
    private final List<String> buttonLikeMessages = new ArrayList<>(Arrays.asList("Like", "Dislike"));
    private Consumer<String> deletedCommentCallback;
    private UserModelMongo postAuthor;

    public ControllerViewDetailsPostPage() {}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.shiftDownSingleObjectGridPane = false;

        currentUser = (GenericUserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        if (!currentUser.get_class().equals("admin")) {
            currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        } else {
            currentUser = (AdminModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        }

        this.previousButton.setDisable(true);
        this.nextButton.setDisable(true);
        resetPage();
        comments.clear();

        post = (PostModelMongo) modelBean.getBean(Constants.SELECTED_POST);

        this.postAuthor = (UserModelMongo) userDBMongo.findByUsername(post.getUsername(), false).get();
        if (postAuthor.isBanned()) {
            this.usernameLabel.setText("[Banned user]");
            this.postTitleTextArea.setText("[Banned user]");
            this.postBodyTextArea.setText("[Banned user]");
        } else {
            this.usernameLabel.setText(post.getUsername());
            this.postTitleTextArea.setText(post.getTitle());
            this.postBodyTextArea.setText(post.getText());
        }

        this.setTextLikeButton(currentUser.getUsername(), post.getId());
        this.tagBoardgameLabel.setText(post.getTag());
        this.timestampLabel.setText(post.getTimestamp().toString());
        this.counterLikesLabel.setText(String.valueOf(post.getLikeCount()));
        this.counterCommentsLabel.setText(String.valueOf(post.getComments().size()));

        comments.addAll(getData(this.post));
        fillGridPane();

        // Setting up buttons depending on if the current user is who created the post that's being visualized
        if (!currentUser.getUsername().equals(post.getUsername())) {
            editButton.setVisible(false);       // Making the edit button invisible
            deleteButton.setVisible(false);     // Making the delete button invisible
        }

        if (currentUser.get_class().equals("admin")) {
            editButton.setVisible(false);
            deleteButton.setVisible(true);
            likeButton.setDisable(true);
            addCommentButton.setDisable(true);
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

        modelBean.putBean(Constants.OPENED_POST, "1");
    }

    public void onFocusGained() {
        PostModelMongo updatedPost = (PostModelMongo) modelBean.getBean(Constants.SELECTED_POST);
        this.tagBoardgameLabel.setText(updatedPost.getTag());
        if (this.postAuthor.isBanned()) {
            this.usernameLabel.setText("[Banned user]");
            this.postTitleTextArea.setText("[Banned user]");
            this.postBodyTextArea.setText("[Banned user]");
        } else {
            this.usernameLabel.setText(post.getUsername());
            this.postTitleTextArea.setText(post.getTitle());
            this.postBodyTextArea.setText(post.getText());
        }

        // Potentially update a comment
        CommentModel updatedComment = (CommentModel) modelBean.getBean(Constants.UPDATED_COMMENT);
        if (updatedComment != null) {
            modelBean.putBean(Constants.UPDATED_COMMENT, null);

            comments.replaceAll(comment -> comment.getId().equals(updatedComment.getId()) ? updatedComment : comment);
            commentGridPane.getChildren().clear();
            fillGridPane();
        }
    }

    /*
        Called whenever the author user of a comment decides to delete that comment.
        This method updates the comments list and updates UI
     */
    public void updateUIAfterCommentDeletion(String deletedCommentId) {
        comments.removeIf(comment -> comment.getId().equals(deletedCommentId));
        post.getComments().removeIf(comment -> comment.getId().equals(deletedCommentId));
        this.counterCommentsLabel.setText(String.valueOf(post.getComments().size()));
        commentGridPane.getChildren().clear();
        cleanFetchAndFill();
        prevNextButtonsCheck(post.getComments());
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
            stageManager.showInfoMessage("INFO", "Something went wrong. Please try again in a while.");
            System.err.println("[ERROR] onClickDeleteButton()@ControllerViewDetailsPostPage.java raised an exception: " + ex.getMessage());
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
        comments.addAll(post.getComments());
        fillGridPane();
        prevNextButtonsCheck(comments);
        this.counterCommentsLabel.setText(String.valueOf(comments.size()));
    }

    public void onClickCloseButton() {
        stageManager.closeStageButton(this.closeButton);
    }

    public void onClickLikeButton() {
        String username = currentUser.getUsername();
        String postId = post.getId();
        postService.likeOrDislikePost(username, postId);
        this.setTextLikeButton(username, postId);
    }

    public void setTextLikeButton(String username, String postId) {
        FontAwesomeIconView icon = (FontAwesomeIconView)this.likeButton.getGraphic();
        boolean likeIsPresent = this.postService.hasLikedPost(username, postId);
        icon.setIcon((likeIsPresent) ? FontAwesomeIcon.THUMBS_DOWN : FontAwesomeIcon.THUMBS_UP);
        this.likeButton.setText(this.buttonLikeMessages.get((likeIsPresent) ? 1 : 0));
        post = postDBMongo.findById(post.getId()).get();
        this.counterLikesLabel.setText(String.valueOf(post.getLikeCount()));
    }

    @FXML
    void onClickNext() {
        commentGridPane.getChildren().clear();
        comments.clear();
        skipCounter += SKIP;
        comments.addAll(getData(this.post));
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    @FXML
    void onClickPrevious() {
        commentGridPane.getChildren().clear();
        comments.clear();
        skipCounter -= SKIP;
        comments.addAll(getData(this.post));
        fillGridPane();
        scrollSet.setVvalue(0);
    }

    void resetPage() {
        commentGridPane.getChildren().clear();
        comments.clear();
        skipCounter = 0;
        previousButton.setDisable(true);
        nextButton.setDisable(true);
        scrollSet.setVvalue(0);
        shiftDownSingleObjectGridPane = false;
    }

    void prevNextButtonsCheck(List<CommentModel> comments) {
        if (comments.size() > 0) {
            if (comments.size() <= LIMIT) {
                if (skipCounter <= 0) {
                    previousButton.setDisable(true);
                    nextButton.setDisable(true);
                } else {
                    previousButton.setDisable(false);
                    nextButton.setDisable(true);
                }
            } else {
                if (skipCounter <= 0) {
                    previousButton.setDisable(true);
                    nextButton.setDisable(false);
                } else {
                    previousButton.setDisable(false);
                    nextButton.setDisable(false);
                }
            }
        } else {
            if (skipCounter <= 0) {
                previousButton.setDisable(true);
                nextButton.setDisable(true);
            } else {
                previousButton.setDisable(false);
                nextButton.setDisable(true);
            }
        }
    }

    private List<CommentModel> getData(PostModelMongo post) {
        List<CommentModel> comments = post.getComments();
        int start = Math.min(skipCounter, comments.size());
        int end = Math.min(start + LIMIT, comments.size());
        List<CommentModel> commentsSubList = comments.subList(start, end);
        List<CommentModel> commentsToNextButton = (skipCounter == 0) ? comments : commentsSubList;
        prevNextButtonsCheck(commentsToNextButton);
        return commentsSubList;
    }

    public void onClickAddCommentButton() {
        try {
            scrollSet.setVvalue(0);
            this.addCommentButton.setDisable(true);
            Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTCREATECOMMENT.getFxmlFile());

            TextField commentTextArea = (TextField) loadViewItem.lookup("#bodyTextLabel");
            Button submitCommentButton = (Button) loadViewItem.lookup("#submitButton");
            Button cancelCommentButton = (Button) loadViewItem.lookup("#cancelButton");
            commentTextArea.setPromptText("Write your comment here...");

            AnchorPane addCommentBox = new AnchorPane();
            addCommentBox.getChildren().add(loadViewItem);

            if (!comments.isEmpty()) {
                if (comments.size() == 1) {
                    shiftDownSingleObjectGridPane = true;
                } else {
                    shiftDownSingleObjectGridPane = false;
                }
                commentGridPane.getChildren().clear();
                fillGridPane();
                commentGridPane.add(addCommentBox, 0, 1);
            } else {
                commentGridPane.add(addCommentBox, 0, 1);
            }
            GridPane.setMargin(addCommentBox, new Insets(8, 5, 10, 90));

            // Submit comment button behavior
            submitCommentButton.setOnAction(e -> {
                String commentText = commentTextArea.getText();
                if (commentText.isEmpty()) {
                    stageManager.showInfoMessage("Error", "A comment cannot be empty.");
                    return;
                }

                CommentModel newComment = new CommentModel(
                        currentUser.getUsername(),        // Current user is commenting this post
                        commentText,
                        new Date()                        // Comment creation date
                );

                UserModelNeo4j currentUserNeo = userNeo4jDB.findByUsername(currentUser.getUsername()).get();
                boolean savedComment = postService.insertComment(newComment, this.post, currentUserNeo);     // MongoDB + Neo4J comment insertion

                if (savedComment) {
                    stageManager.showInfoMessage("Success", "Comment added successfully.");

                    comments.add(0, newComment);      // Adding the new comment to the comment list

                    commentGridPane.getChildren().clear();
                    fillGridPane();             // Displaying update

                    this.addCommentButton.setDisable(false);        // Restore button
                    modelBean.putBean(Constants.ADDED_COMMENT, newComment);         // Saving info about the newly inserted comment to update UI in posts feed page

                    this.counterCommentsLabel.setText(String.valueOf(post.getComments().size()));  // Update post details page UI - increase comment count
                } else {
                    stageManager.showInfoMessage("INFO", "Something went wrong. Please try again in a while.");
                }
                onClickRefreshButton();
                prevNextButtonsCheck(comments);
            });

            // Discard comment button behavior
            cancelCommentButton.setOnAction(e -> {
                boolean userChoice = stageManager.showDiscardCommentInfoMessage();
                if (userChoice) {
                    this.addCommentButton.setDisable(false);
                    cleanFetchAndFill();
                }
            });
        } catch (Exception e) {
            System.err.println("[ERROR] onClickAddCommentButton()@ControllerViewDetailsPostPage.java raised an exception: " + e.getMessage());
            stageManager.showInfoMessage("INFO", "Something went wrong. Please try again in a while.");
        }
    }

    private void cleanFetchAndFill() {
        resetPage();
        comments.addAll(getData(this.post));
        fillGridPane();
    }


    private void loadViewMessageInfo() {
        Parent loadViewItem = stageManager.loadViewNode(FxmlView.INFOMSGCOMMENTS.getFxmlFile());
        AnchorPane noContentsYet = new AnchorPane();
        noContentsYet.getChildren().add(loadViewItem);

        commentGridPane.add(noContentsYet, 0, 1);
        GridPane.setMargin(noContentsYet, new Insets(18, 5, 15, 270));
    }

    @FXML
    void fillGridPane() {
        columnGridPane = 0;       // Needed to correctly position a single element in the grid pane
        if (comments.size() == 1) {
            if (shiftDownSingleObjectGridPane) {
                rowGridPane = 2;
            } else {
                rowGridPane = 0;
            }
        } else {
            rowGridPane++;
        }

        try {
            if (comments.isEmpty()) {
                loadViewMessageInfo();
            } else {
                for (CommentModel comment : comments) {
                    AnchorPane commentNode = createCommentViewNode(comment);
                    addCommentToGridPane(commentNode);
                }
            }
        } catch (Exception e) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Try again in a while.");
            System.err.println("[ERROR] fillGridPane()@ControllerViewDetailsPostPage.java raised an exception: " + e.getMessage());
        }
    }

    private AnchorPane createCommentViewNode(CommentModel comment) {
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
