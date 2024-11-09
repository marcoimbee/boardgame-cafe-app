package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.CommentDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.CommentService;
import it.unipi.dii.lsmsdb.boardgamecafe.services.UserService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.expression.spel.ast.OpAnd;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import javax.xml.stream.events.Comment;
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
    private GridPane postGridPane;
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

    public void onClickAddCommentButton(ActionEvent event) {

        //ToDo: Aggiungere switchScene in cui verrè fatto quello che qui è commentato
//        String postId = this.post.getId();
//        String username_test = "g.sferr";
//        String body_test = " commento verifica Refresh Grafico method";
//        Date timestamp = new Date();
//        CommentModelMongo commentTest = new CommentModelMongo(
//                postId,
//                username_test,
//                body_test, timestamp);
//
//        //Operazione che genera correttamente l'id del commento che spunterà poi nell'array dei commenti del post
//        CommentModelMongo commentIntoCommentCollection = commentDBMongo.addComment(commentTest);
//
//        Optional<UserModelNeo4j> userFromNeo = userNeo4jDB.
//                findByUsername(commentIntoCommentCollection.getUsername());
//        if (userFromNeo.isPresent()){
//            UserModelNeo4j userNeo4j = userFromNeo.get();
//
//            boolean serviceTest = serviceComment.
//                    insertComment(commentIntoCommentCollection, this.post, userNeo4j);
//
//            if (serviceTest) {
//                stageManager.showInfoMessage("Info Comments", "Comment Successfully Added into Post");
//            } else {
//                stageManager.showInfoMessage("Info Comments", "Comment NOT Successfully Added into Post");
//            }
//        }
    }

    public void onClickRefreshButton(ActionEvent event) {
        resetPage();
        Optional<PostModelMongo> postFromMongo = postDBMongo.findById(this.post.getId());
        postFromMongo.ifPresent(postModelMongo -> post = postModelMongo);
        post.getComments().sort(Comparator.comparing(CommentModelMongo::getTimestamp).reversed());
        comments.addAll(post.getComments());
        fillGridPane();
    }

    public void onClickExitButton(ActionEvent event) {
        stageManager.closeStageButton(this.exitButton);
    }

    public void onClickLikeButton(ActionEvent event) {
    }
    @FXML
    void onClickNext() {
        //clear variables
        postGridPane.getChildren().clear();
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
        postGridPane.getChildren().clear();
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
        postGridPane.getChildren().clear();
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
    @FXML
    void fillGridPane() {

        columnGridPane = 0;
        rowGridPane = 0;
        setGridPaneColumnAndRow();

        //CREATE FOR EACH POST AN ITEM (ObjectPosts)
        try {
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

                postGridPane.add(anchorPane, columnGridPane++, rowGridPane); //(child,column,row)
                //DISPLAY SETTINGS
                //set grid width
                postGridPane.setMinWidth(Region.USE_COMPUTED_SIZE);
                postGridPane.setPrefWidth(500);
                postGridPane.setMaxWidth(Region.USE_COMPUTED_SIZE);
                //set grid height
                postGridPane.setMinHeight(Region.USE_COMPUTED_SIZE);
                postGridPane.setPrefHeight(400);
                postGridPane.setMaxHeight(Region.USE_COMPUTED_SIZE);
                //GridPane.setMargin(anchorPane, new Insets(25));
                GridPane.setMargin(anchorPane, new Insets(0,5,10,90));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
