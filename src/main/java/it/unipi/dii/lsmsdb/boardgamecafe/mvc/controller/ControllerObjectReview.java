package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.AdminModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.services.ReviewService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;


@Component
public class ControllerObjectReview {

    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    protected Label authorLabel;
    @FXML
    protected Label dateOfReviewLabel;
    @FXML
    protected Label tagBoardgameLabel;
    @FXML
    protected Label ratingLabel;
    @FXML
    protected TextArea bodyTextLabel;


    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ModelBean modelBean;
    @Autowired
    @Lazy
    private StageManager stageManager;


    private ReviewModelMongo review;
    private static GenericUserModelMongo currentUser;
    private Consumer<String> deletedReviewCallback;


    public ControllerObjectReview() {
    }

    public void setData(ReviewModelMongo review, Consumer<String> deletedReviewCallback) {
        currentUser = (GenericUserModelMongo) modelBean.getBean(Constants.CURRENT_USER);

        if (!currentUser.get_class().equals("admin"))
        {
            currentUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
            // Removing the possibility to edit and delete a review if the current user is not the author
            if (!currentUser.getUsername().equals(review.getUsername())) {
                editButton.setVisible(false);
                deleteButton.setVisible(false);
            }
        }
        else
        {
            currentUser = (AdminModelMongo) modelBean.getBean(Constants.CURRENT_USER);
            editButton.setDisable(true);
            deleteButton.setVisible(true);
        }

        this.review = review;
//        this.editButton.setDisable(false);
//        this.deleteButton.setDisable(false);
        String creationDate = review.getDateOfReview().toString();

        // Setting up callback functions
        this.deletedReviewCallback = deletedReviewCallback;

        this.authorLabel.setText(review.getUsername());
        this.dateOfReviewLabel.setText(creationDate);
        this.tagBoardgameLabel.setText(review.getBoardgameName());
        this.ratingLabel.setText(String.valueOf(review.getRating()));
        this.bodyTextLabel.setText(review.getBody());



        // Setting up button listeners
        deleteButton.setOnAction(event -> onClickDeleteButton(review));
        editButton.setOnAction(event -> onClickEditButton(review));
    }

    @FXML
    public void onClickEditButton(ReviewModelMongo review) {
        modelBean.putBean(Constants.SELECTED_REVIEW, review);
        stageManager.showWindow(FxmlView.EDIT_REVIEW);            // Do not close underlying page, just show the little review editing window
    }

    @FXML
    public void onClickDeleteButton(ReviewModelMongo review) {
        boolean userChoice = stageManager.showDeleteReviewInfoMessage();
        if (!userChoice) {
            return;
        }

        try {
            if (currentUser.get_class().equals("user")){
                UserModelMongo user = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
                reviewService.deleteReview(review, user);

                System.out.println("[INFO] Successfully deleted a review.");

                modelBean.putBean(Constants.DELETED_REVIEW, review);
                deletedReviewCallback.accept(review.getId());
            }

        } catch (Exception ex) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Try again in a while.");
            System.err.println("[ERROR] onClickDeleteButton@ControllerObjectReview.java raised an exception: " + ex.getMessage());
        }
    }
}
