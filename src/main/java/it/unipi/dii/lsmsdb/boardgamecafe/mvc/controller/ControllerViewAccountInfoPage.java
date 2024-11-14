package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.PostDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.ReviewDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.*;

@Component
public class ControllerViewAccountInfoPage implements Initializable{

    public enum UserActivity {
        EDIT_INFO, NO_EDIT
    }
    //********* Buttons *********
    @FXML
    private Button yourProfileButton;
    @FXML
    private Button postsFeedButton;
    @FXML
    private Button accountInfoButton;
    @FXML
    private Button searchUserButton;
    @FXML
    private Button boardgamesCollectionButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Button deleteAccountButton;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveChangesButton;
    @FXML
    private Button editAccountInfoButton;


    // ********** Llabels *********
    @FXML
    private Label firstNameLabel;
    @FXML
    private Label lastNameLabel;
    @FXML
    private Label nationalityLabel;
    @FXML
    private Label genderLabel;
    @FXML
    private Label dateOfBirthLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label passwordLabel;

    //********* TextField and its Label Components *********
    @FXML private TextField textFieldFirstName;
    @FXML private Label subLabelFirstName;
    @FXML private TextField textFieldLastName;
    @FXML private Label subLabelLastName;
    @FXML private ComboBox<String> comboBoxNationality;
    @FXML private Label subLabelNationality;
    @FXML private ComboBox<String> comboBoxGender;
    @FXML private Label subLabelGender;
    @FXML private DatePicker datePickerDate;
    @FXML private Label subLabelDate;
    @FXML private TextField textFieldEmail;
    @FXML private Label subLabelEmail;
    @FXML private TextField textFieldUsername;
    @FXML private Label subLabelUsername;
    @FXML private TextField textFieldPassword;
    @FXML private Label subLabelPassword;
    @FXML private TextField textFieldRepeatPassword;
    @FXML private Label subLabelRepeatedPassword;


    //********* Other Components *********
    @FXML
    private ImageView profileImage;

    //********* Autowireds *********
    @Autowired
    private PostDBMongo postDBMongo;
    @Autowired
    private ReviewDBMongo reviewMongoOp;
    @Autowired
    private UserDBMongo userMongoOp;
    @Autowired
    private UserDBNeo4j userDBNeo;
    @Autowired
    private ModelBean modelBean;

    //Stage Manager
    private final StageManager stageManager;
    //User
    private UserModelMongo regUser;
    //Useful Variables
    private UserActivity selectedOperation;


    @Autowired
    @Lazy
    public ControllerViewAccountInfoPage(StageManager stageManager) {
        this.stageManager = stageManager;
    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //resetPage();
        this.accountInfoButton.setDisable(true);
        this.cancelButton.setVisible(false);
        this.saveChangesButton.setVisible(false);
        this.selectedOperation = UserActivity.NO_EDIT;
        regUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        Image image = new Image(Objects.requireNonNull(getClass().
                getResource("/user.png")).toExternalForm());
        this.profileImage.setImage(image);

        this.firstNameLabel.setText(regUser.getName());
        this.lastNameLabel.setText(regUser.getSurname());
        this.nationalityLabel.setText(regUser.getNationality());
        this.genderLabel.setText(regUser.getGender());
        this.dateOfBirthLabel.setText(regUser.getDateOfBirth().toString());
        this.emailLabel.setText(regUser.getEmail());
        this.usernameLabel.setText(regUser.getUsername());
        this.passwordLabel.setText("***************");

        this.textFieldFirstName.setVisible(false);
        this.subLabelFirstName.setVisible(false);
        this.textFieldLastName.setVisible(false);
        this.subLabelLastName.setVisible(false);
        this.comboBoxNationality.setVisible(false);
        this.subLabelNationality.setVisible(false);
        this.comboBoxGender.setVisible(false);
        this.subLabelGender.setVisible(false);
        this.datePickerDate.setVisible(false);
        this.subLabelDate.setVisible(false);
        this.textFieldEmail.setVisible(false);
        this.subLabelEmail.setVisible(false);
        this.textFieldUsername.setVisible(false);
        this.subLabelUsername.setVisible(false);
        this.textFieldPassword.setVisible(false);
        this.subLabelPassword.setVisible(false);
        this.textFieldRepeatPassword.setVisible(false);
        this.subLabelRepeatedPassword.setVisible(false);
    }

    public void onClickEditAccountInfoButton() {
        this.selectedOperation = UserActivity.EDIT_INFO;
        this.cancelButton.setVisible(true);
        this.saveChangesButton.setVisible(true);
        insertNewInfo();
    }
    public void onClickSaveChangesButton() {

    }
    public void onClickCancelButton() {
        resetPage();
    }
    public void onClickDeleteAccountButton() {

    }

    private void initDisplay(){

        Image image = new Image(Objects.requireNonNull(getClass().
                getResource("/user.png")).toExternalForm());
        this.profileImage.setImage(image);

        this.firstNameLabel.setText(regUser.getName());
        this.lastNameLabel.setText(regUser.getSurname());
        this.nationalityLabel.setText(regUser.getNationality());
        this.genderLabel.setText(regUser.getGender());
        this.dateOfBirthLabel.setText(regUser.getDateOfBirth().toString());
        this.emailLabel.setText(regUser.getEmail());
        this.usernameLabel.setText(regUser.getUsername());
        this.passwordLabel.setText("***************");

        setEditFieldsVisibility(false);
    }

    private void insertNewInfo() {
        if(selectedOperation == UserActivity.EDIT_INFO){
            setEditFieldsVisibility(true);
        }
    }

    private void setEditFieldsVisibility(boolean isVisible) {
        this.textFieldFirstName.setVisible(isVisible);
        this.subLabelFirstName.setVisible(isVisible);
        this.textFieldLastName.setVisible(isVisible);
        this.subLabelLastName.setVisible(isVisible);
        this.comboBoxNationality.setVisible(isVisible);
        this.subLabelNationality.setVisible(isVisible);
        this.comboBoxGender.setVisible(isVisible);
        this.subLabelGender.setVisible(isVisible);
        this.datePickerDate.setVisible(isVisible);
        this.subLabelDate.setVisible(isVisible);
        this.textFieldEmail.setVisible(isVisible);
        this.subLabelEmail.setVisible(isVisible);
        this.textFieldUsername.setVisible(isVisible);
        this.subLabelUsername.setVisible(isVisible);
        this.textFieldPassword.setVisible(isVisible);
        this.subLabelPassword.setVisible(isVisible);
        this.textFieldRepeatPassword.setVisible(isVisible);
        this.subLabelRepeatedPassword.setVisible(isVisible);
    }


    private void resetPage() {
        if (this.selectedOperation.equals(UserActivity.EDIT_INFO)) {
            this.selectedOperation = UserActivity.NO_EDIT;
        }
        this.cancelButton.setVisible(true);
        this.saveChangesButton.setVisible(true);
        this.accountInfoButton.setDisable(true);
        initDisplay();
    }


    public LocalDateTime selectDate() {
//        LocalDate selectedDate = this.datePickerDate.getValue();
//        LocalDate currentDate = LocalDate.now();
//
//        if (selectedDate == null) {
//            labelDate.setText("Date of Birth is missing.");
//        } else if (selectedDate.isAfter(currentDate)) {
//            labelDate.setText("Were you born in the future?");
//        } else {
//            LocalDateTime dateTime = selectedDate.atStartOfDay();
//            //String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
//            Period age = Period.between(selectedDate, currentDate);
//
//            if (age.getYears() < 13 ) {
//                labelDate.setText("You must be at least 13 years old.");
//            } else {
//                labelDate.setText("");
//                return dateTime;
//            }
//        }
        return null;
    }

    public String selectEmail() {
//        //se email già presente o bannata: messaggio di errore.
//        Optional<GenericUserModelMongo> user = userDBMongo.
//                findByEmail(this.textFieldEmail.getText());
//        if (user.isPresent()) {
//            UserModelMongo userFromMongo = (UserModelMongo) user.get();
//            if (userFromMongo.isBanned())
//                return "user_banned";
//            return "already_used";
//        }
//        return this.textFieldEmail.getText();
        return "";
    }


    public String  selectUsername(){

//        //se username già presente: messaggio di errore.
//        Optional<GenericUserModelMongo> user = userDBMongo.
//                findByUsername(this.textFieldUsername.getText());
//
//        if (user.isPresent()) { return "already_used"; }
//        return this.textFieldUsername.getText();
        return "";
    }


    public void onClickYourProfileButton() {
        stageManager.showWindow(FxmlView.USERPROFILEPAGE);
        stageManager.closeStageButton(this.yourProfileButton);
    }
    public void onClickBoardgamesButton() {
        stageManager.showWindow(FxmlView.REGUSERBOARDGAMES);
        stageManager.closeStageButton(this.boardgamesCollectionButton);
    }
    public void onClickPostsFeedButton() {
        stageManager.showWindow(FxmlView.REGUSERPOSTS);
        stageManager.closeStageButton(this.postsFeedButton);
    }
    public void onClickSearchUserButton() {
        stageManager.showWindow(FxmlView.SEARCHUSER);
        stageManager.closeStageButton(this.searchUserButton);
    }
    public void onClickLogout(ActionEvent event) {
        modelBean.putBean(Constants.CURRENT_USER, null);
        stageManager.showWindow(FxmlView.WELCOMEPAGE);
        stageManager.closeStageButton(this.logoutButton);
    }


}
