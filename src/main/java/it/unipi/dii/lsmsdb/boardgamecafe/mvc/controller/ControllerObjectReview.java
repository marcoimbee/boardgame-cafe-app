package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.services.ReviewService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    private ReviewModelMongo review;

    @Autowired
    private ReviewService reviewService; // Iniezione del servizio

    private StageManager stageManager;

    public ControllerObjectReview() {
    }

    public void setData(ReviewModelMongo review) {

        this.review = review;

        this.editButton.setDisable(true);
        this.deleteButton.setDisable(true);

        String creationDate = review.getDateOfReview().toString();

        authorLabel.setText(review.getUsername());
        dateOfReviewLabel.setText(creationDate);
        tagBoardgameLabel.setText(review.getBoardgameName());
        ratingLabel.setText(String.valueOf(review.getRating()));
        bodyTextLabel.setText(review.getBody());
    }


    @FXML
    public void onClickEditButton(ActionEvent event) {
        // Implementazione per rimuovere il post
        String title = "ToDo Message";
        String message = "" +
                "A breve verrai reindirizzato alla pagina in cui puoi modificare la review.\n";

        stageManager.showInfoMessage(title, message);
    }

    @FXML
    public void onClickDeleteButton(ActionEvent event) {
        // Implementazione per commentare il post
        String title = "ToDo Message";
        String message = "" +
                "A breve questo messaggio si aggiornerà dicendoti 'Review Eliminata Correttamente'.\n";

        stageManager.showInfoMessage(title, message);
    }

}
