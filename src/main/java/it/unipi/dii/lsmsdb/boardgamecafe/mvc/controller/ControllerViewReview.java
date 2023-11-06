/*
package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

//import it.unipi.dii.lsmsdb.phoneworld.App;
//import it.unipi.dii.lsmsdb.phoneworld.Constants;
//import it.unipi.dii.lsmsdb.phoneworld.model.Phone;
//import it.unipi.dii.lsmsdb.phoneworld.model.Review;
//import it.unipi.dii.lsmsdb.phoneworld.model.User;
//import it.unipi.dii.lsmsdb.phoneworld.repository.mongo.ReviewMongo;
//import it.unipi.dii.lsmsdb.phoneworld.services.ServiceReview;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

@Component
public class ControllerViewReview implements Initializable {

    @FXML private Spinner<Integer> spinnerRating;
    @FXML private Button buttonCancel;
    @FXML private Button buttonServiceReview;
    @FXML private TextArea textAreabody;
    @FXML private TextField textFieldTitle;

    private final StageManager stageManager;

    @Autowired
    private ReviewMongoDB reviewMongo;

    @Autowired
    private ServiceReview serviceReview;

    @Autowired @Lazy
    public ControllerViewReview(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @FXML
    void onClickCancel(ActionEvent event) {
        stageManager.closeStage(this.buttonCancel);
        boolean isUpdate = (boolean) App.getInstance().getModelBean().getBean(Constants.IS_UPDATE);
        if (isUpdate) {
            stageManager.showWindow(FxmlView.DETAILS_USER);
            return;
        }
        stageManager.showWindow(FxmlView.DETAILS_PHONES);
    }

    @FXML
    void onClickService(ActionEvent event) {
        boolean isUpdate = (boolean) App.getInstance().getModelBean().getBean(Constants.IS_UPDATE);
        User user = (User) App.getInstance().getModelBean().getBean(Constants.CURRENT_USER);
        Phone phone = (Phone) App.getInstance().getModelBean().getBean(Constants.SELECTED_PHONE);
        String title = this.textFieldTitle.getText();
        int rating = this.spinnerRating.getValue();
        String body = this.textAreabody.getText();
        if (title.isEmpty() && body.isEmpty()) {
            stageManager.showInfoMessage("ERROR", "You have to insert title and body!");
            return;
        }
        if (title.isEmpty()) {
            stageManager.showInfoMessage("ERROR", "You have to insert the title!");
            return;
        }
        if (body.isEmpty()) {
            stageManager.showInfoMessage("ERROR", "You have to insert the body!");
            return;
        }
        Date dateOfReview = new Date();
        if (!isUpdate) {
            Review newReview = new Review.ReviewBuilder(rating, dateOfReview, title, body).username(user.getUsername()).
                    phoneName(phone.getName()).build();
            if (!serviceReview.insertReview(newReview,phone, user)) {
                stageManager.showInfoMessage("ERROR", "Error in adding the review for this phone!");
                return;
            }
            stageManager.closeStage(this.buttonServiceReview);
            stageManager.showWindow(FxmlView.DETAILS_PHONES);
            stageManager.showInfoMessage("INFO", "You added a review to this phone");
        } else {
            Review selectedReview = (Review) App.getInstance().getModelBean().getBean(Constants.SELECTED_REVIEW);
            if (!serviceReview.updateReview(selectedReview, user, rating, dateOfReview, title, body)) {
                stageManager.showInfoMessage("ERROR", "Error in updating the review for this phone!");
                return;
            }
            stageManager.closeStage(this.buttonServiceReview);
            stageManager.showWindow(FxmlView.DETAILS_USER);
            stageManager.showInfoMessage("INFO", "You updated your review of this phone");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        boolean isUpdate = (boolean) App.getInstance().getModelBean().getBean(Constants.IS_UPDATE);
        SpinnerValueFactory<Integer> valueFactoryRating = new SpinnerValueFactory.IntegerSpinnerValueFactory(1,5);
        this.spinnerRating.setValueFactory(valueFactoryRating);
        if (!isUpdate) {
            this.buttonServiceReview.setText("ADD");
            this.spinnerRating.getValueFactory().setValue(1);
        } else {
            Review review = (Review) App.getInstance().getModelBean().getBean(Constants.SELECTED_REVIEW);
            if (review == null) {
                stageManager.showInfoMessage("ERROR", "Error in updating review!");
                return;
            }
            this.buttonServiceReview.setText("UPDATE");
            this.spinnerRating.getValueFactory().setValue(review.getRating());
            this.textFieldTitle.setText(review.getTitle());
            this.textAreabody.setText(review.getBody());
        }
    }
}

*/