package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class ControllerObjectCreateReview {
    @FXML
    protected Label usernameLabel;
    @FXML
    protected Label timestampLabel;
    @FXML
    protected Label postLabel;
    @FXML
    private Button submitButton;
    @FXML
    private Button cancelButton;
    @FXML
    protected TextField bodyTextLabel;
    @FXML
    private Slider ratingSlider;

    private ReviewModelMongo review;
    private StageManager stageManager;

    @Autowired
    @Lazy
    public ControllerObjectCreateReview(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    public ControllerObjectCreateReview() {}

    public void setData() {}

    @FXML
    public void onClickSubmitButton() {}

    @FXML
    public void onClickCancelButton() {}
}
