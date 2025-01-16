package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.services.ReviewService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class ControllerViewEditReviewPage implements Initializable {
    @FXML
    public TextField bodyTextLabel;
    @FXML
    public Slider ratingSlider;
    @FXML
    public Button cancelButton;
    @FXML
    public Button submitButton;

    @Autowired
    private ModelBean modelBean;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    @Lazy
    private StageManager stageManager;
    private static ReviewModelMongo selectedReview;

    public ControllerViewEditReviewPage() {}

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectedReview = (ReviewModelMongo) modelBean.getBean(Constants.SELECTED_REVIEW);

        this.ratingSlider.setValue(selectedReview.getRating());
        this.bodyTextLabel.setText(selectedReview.getBody());
    }

    @FXML
    public void onClickCancelButton() {
        String updatedBody = this.bodyTextLabel.getText();
        int updateRating = (int) this.ratingSlider.getValue();

        if (updatedBody.equals(selectedReview.getBody()) && updateRating == selectedReview.getRating()) {        // No changes were made
            stageManager.closeStage();
            return;
        }

        boolean userChoice = stageManager.showUpdatePostInfoMessage();      // Ask confirmation, as changes were made
        if (userChoice) {
            stageManager.closeStage();
        }
    }

    @FXML
    public void onClickSubmitButton() {
        String updatedBody = this.bodyTextLabel.getText();
        int updateRating = (int) this.ratingSlider.getValue();

        if (updatedBody.equals(selectedReview.getBody()) && updateRating == selectedReview.getRating()) {      // Nothing was updated, ok to close and no further action
            stageManager.closeStage();
            return;
        }

        // Changes were made, actually update review
        try {
            // Update mongoDB review in reviews collection
            ReviewModelMongo updatedReview = selectedReview;
            int oldRating = selectedReview.getRating();
            updatedReview.setBody(updatedBody);
            updatedReview.setRating(updateRating);
            reviewService.updateReview(updatedReview, oldRating);

            // Setting updated in model bean to retrieve them in post details page for UI update
            modelBean.putBean(Constants.UPDATED_REVIEW, updatedReview);

            System.out.println("[INFO] Successfully updated a review.");
            stageManager.closeStage();
        } catch (Exception ex) {
            stageManager.showInfoMessage("INFO", "Something went wrong. Please try again in a while.");
            System.err.println("[ERROR] onClickSubmitButton@ControllerViewEditReviewPage.java raised an exception: " + ex.getMessage());
        }
    }
}
