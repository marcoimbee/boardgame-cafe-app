package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.CommentDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.*;

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
    private ControllerObjectComment controllerObjectComment;
    @Autowired
    private ModelBean modelBean;
    @Autowired
    private UserDBNeo4j userNeo4jDB;
    @Autowired
    private CommentService serviceComment;

    private final StageManager stageManager;

    private List<CommentModelMongo> comments = new ArrayList<>();

    private PostModelMongo post;


    //Utils Variables
    private int columnGridPane = 0;
    private int rowGridPane = 0;
    private int skipCounter = 0;
    private final static int SKIP = 10; //how many posts to skip per time
    private final static int LIMIT = 10; //how many posts to show for each page

    private final static Logger logger = LoggerFactory.getLogger(PostDBMongo.class);

    @Autowired
    @Lazy
    public ControllerViewDetailsPostPage(StageManager stageManager) {
        this.stageManager = stageManager;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
    }

    public void onClickDeleteButton(ActionEvent event) {
    }

    public void onClickEditButton(ActionEvent event) {
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

    public void onClickAddCommentButton(ActionEvent event) {
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

    private void loadViewMessagInfo(){
        Parent loadViewItem = stageManager.loadViewNode(FxmlView.INFOMSGCOMMENTS.getFxmlFile());
        AnchorPane noContentsYet = new AnchorPane();
        noContentsYet.getChildren().add(loadViewItem);

        resetPage();
        commentGridPane.add(noContentsYet, 0, 0);

        if (!comments.isEmpty()){
            resetPage();
            commentGridPane.add(noContentsYet, 0, rowGridPane);
        }
        GridPane.setMargin(noContentsYet, new Insets(330, 100, 100, 265));
    }

    @FXML
    void fillGridPane() {

        //per mettere un solo elemento correttamente nel gridpane
        if (comments.size() == 1) {
            columnGridPane = 0;
            rowGridPane = 0;
        } else {
            setGridPaneColumnAndRow();
        }

        try {
            if (comments.isEmpty()) {
                loadViewMessagInfo();
            }
            for (CommentModelMongo comment : comments) { // iterando lista di posts

                Parent loadViewItem = stageManager.loadViewNode(FxmlView.OBJECTCOMMENT.getFxmlFile());

                AnchorPane anchorPane = new AnchorPane();
                anchorPane.getChildren().add(loadViewItem);

                controllerObjectComment.setData(comment, this.post);

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
                GridPane.setMargin(anchorPane, new Insets(4,5,10,90));

            }
        } catch (Exception e) {
            e.printStackTrace();
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
