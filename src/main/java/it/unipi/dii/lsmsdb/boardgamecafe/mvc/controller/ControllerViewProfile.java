package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

//import it.unipi.dii.lsmsdb.phoneworld.App;
//import it.unipi.dii.lsmsdb.phoneworld.Constants;
//import it.unipi.dii.lsmsdb.phoneworld.model.User;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.ResourceBundle;

@Component
public class ControllerViewProfile implements Initializable {

    @FXML private Button buttonDetails;
    @FXML private Button buttonUpdate;
    @FXML private Button buttonCancel;
    @FXML private Label labelUsername;
    @FXML private Label labelFirstName;
    @FXML private Label labelLastName;
    @FXML private Label labelGender;
    @FXML private Label labelCountry;
    @FXML private Label labelCity;
    @FXML private Label labelStreet;
    @FXML private Label labelBirthday;
    @FXML private Label labelEmail;
    @FXML private ImageView imageViewProfile;

    private final StageManager stageManager;

    @Autowired @Lazy
    public ControllerViewProfile(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        User user = (User) App.getInstance().getModelBean().getBean(Constants.CURRENT_USER);
        this.labelUsername.setText(user.getUsername());
        this.labelFirstName.setText(user.getFirstName());
        this.labelLastName.setText(user.getLastName());
        this.labelGender.setText(user.getGender());
        this.labelCountry.setText(user.getCountry());
        this.labelCity.setText(user.getCity());
        this.labelStreet.setText(user.getStreetName() + " " + user.getStreetNumber());
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(user.getDateOfBirth());
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        month++;
        int year = calendar.get(Calendar.YEAR);
        day--;
        this.labelBirthday.setText(year + "-" + month + "-" + day);
        this.labelEmail.setText(user.getEmail());
        this.imageViewProfile.setImage(new Image("user.png"));

    }

    public void onClickUpdate() {
        stageManager.closeStage(this.buttonUpdate);
        stageManager.showWindow(FxmlView.UPDATE);
    }

    public void onClickDetails() {
        App.getInstance().getModelBean().putBean(Constants.SELECTED_USER, null);
        stageManager.closeStage(this.buttonDetails);
        stageManager.showWindow(FxmlView.DETAILS_USER);
    }
    public void onClickCancel() {
        stageManager.closeStage(this.buttonCancel);
    }
}
