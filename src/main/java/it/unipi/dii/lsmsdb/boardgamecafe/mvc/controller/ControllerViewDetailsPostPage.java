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
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.CommentDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.CommentService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.event.ActionEvent;
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
    private Button exitButton;
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
    private PostDBNeo4j postDBNeo4j;
    @Autowired
    private ControllerObjectComment controllerObjectComment;
    @Autowired
    private ModelBean modelBean;
    @Autowired
    private UserDBNeo4j userNeo4jDB;
    @Autowired
    private CommentDBNeo4j commentDBNeo4j;
    @Autowired
    private CommentService serviceComment;

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
            // Delete post from neo4j and its comments
            commentDBNeo4j.deleteByPost(post.getId());
            postDBNeo4j.deletePost(post.getId());

            // Delete post from mongodb and its comments
            postDBMongo.deletePost(post);
            commentDBMongo.deleteByPost(post.getId());

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

    public void onClickRefreshButton(ActionEvent event) {
        cleanFetchAndFill();
    }

    public void onClickExitButton(ActionEvent event) {
        stageManager.closeStageButton(this.exitButton);
    }

    public void onClickLikeButton(ActionEvent event) {
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
        //clear variables
        commentGridPane.getChildren().clear();
        comments.clear();
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

    void setGridPaneColumnAndRow(){

        columnGridPane = 0;
        rowGridPane = 1;
    }

    public void onClickAddCommentButton() {
        try {
            this.addCommentButton.setDisable(true);
            // Carica l'FXML del commento modificabile
            Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTCREATECOMMENT.getFxmlFile());

            // Ottieni i controlli di input definiti nel FXML
            TextField commentTextArea = (TextField) loadViewItem.lookup("#bodyTextLabel"); // Assumi che bodyTextLabel sia modificabile nel FXML
            Button submitCommentButton = (Button) loadViewItem.lookup("#submitButton");  // Assumi che submitButton esista solo in FXML modificabile
            Button cancelCommentButton = (Button) loadViewItem.lookup("#cancelButton");
            commentTextArea.setPromptText("Write Your Comment Here...");

            //AddButton Behaviour
            submitCommentButton.setOnAction(e ->
            {
                String commentText = commentTextArea.getText();
                if (commentText.isEmpty()) {
                    stageManager.showInfoMessage("Error", "Comment Cannot Be Empty.");
                    return;
                }
                // Crea un nuovo CommentModelMongo e salva il commento nel database
                CommentModelMongo newComment = new CommentModelMongo(
                        this.post.getId(),  //Id del post in cui sto commentatndo
                        post.getUsername(),  // username del "creatore! (Deve essere CurrentUser)
                        commentText,    //Contenuto testuale del commento
                        new Date()  //Timestamp
                );
                //Ottenimento user da neo4j per fare l'add locale nella insertComment
                Optional<UserModelNeo4j> userFromNeo = userNeo4jDB.
                                                       findByUsername(newComment.getUsername());
                if (userFromNeo.isPresent()) {
                    UserModelNeo4j userNeo4j = userFromNeo.get();

                    boolean savedComment = serviceComment.
                            insertComment(newComment, this.post, userNeo4j);

                    if (savedComment) {
                        stageManager.showInfoMessage("Success", "Comment Added Successfully.");
                        cleanFetchAndFill(); //Per mostrare subito tutti commenti compreso quello appena aggiunto
                        this.addCommentButton.setDisable(false);
                    } else {
                        stageManager.showInfoMessage("Error", "Failed to add comment.");
                    }
                } else {
                    stageManager.showInfoMessage("Error", "No User presents in Neo4j.");
                }
            });

            //CancelButton Behaviour
            cancelCommentButton.setOnAction(e -> {
                this.addCommentButton.setDisable(false);
                cleanFetchAndFill();
            });

            AnchorPane addCommentBox = new AnchorPane();
            addCommentBox.getChildren().add(loadViewItem);

            if (comments.isEmpty()){
                resetPage();
                commentGridPane.add(addCommentBox, 0, rowGridPane);
            } else {
                resetPage();
                commentGridPane.add(addCommentBox, 0, 0);
            }
            GridPane.setMargin(addCommentBox, new Insets(8, 5, 10, 90));

        } catch (Exception e) {
            e.printStackTrace();
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
        // Setting up what method should be called upon comment deletion
        deletedCommentCallback = this::updateUIAfterCommentDeletion;

        commentGridPane.getChildren().clear();

        //per mettere un solo elemento correttamente nel gridpane
        if (comments.size() == 1) {
            columnGridPane = 0;
            rowGridPane = 0;
        } else {
            setGridPaneColumnAndRow();
        }

        try {
            if (comments.isEmpty()) {
                loadViewMessageInfo();
            } else {
                for (CommentModelMongo comment : comments) {
                    Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTCOMMENT.getFxmlFile());

                    AnchorPane anchorPane = new AnchorPane();
                    anchorPane.getChildren().add(loadViewItem);

                    // Setting comment data - including callbacks for actions to be taken upon comment modification or deletion
                    controllerObjectComment.setData(comment, this.post, deletedCommentCallback);

                    //choice number of column
                    if (columnGridPane == 1) {
                        columnGridPane = 0;
                        rowGridPane++;
                    }

                    commentGridPane.add(anchorPane, columnGridPane++, rowGridPane); //(child,column,row)
                    //DISPLAY SETTINGS
                    //set grid width
                    commentGridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
                    commentGridPane.setPrefWidth(500);
                    commentGridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);
                    //set grid height
                    commentGridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
                    commentGridPane.setPrefHeight(400);
                    commentGridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
                    //GridPane.setMargin(anchorPane, new Insets(25));
                    GridPane.setMargin(anchorPane, new Insets(4, 5, 10, 90));
                }
            }
        } catch (Exception e) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Try again in a while.");
            System.err.println("[ERROR] fillGridPane@ControllerViewDetailsPostPage.java raised an exception: " + e.getMessage());
        }
    }

    private void cleanFetchAndFill(){
        resetPage();
        Optional<PostModelMongo> postFromMongo = postDBMongo.findById(this.post.getId());
        postFromMongo.ifPresent(postModelMongo -> post = postModelMongo);
        post.getComments().sort(Comparator.comparing(CommentModelMongo::getTimestamp).reversed());
        comments.addAll(post.getComments());
        fillGridPane();
    }

}
