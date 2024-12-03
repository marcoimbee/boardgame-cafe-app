package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.PostListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.*;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.UserService;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ControllerViewAccountInfoPage implements Initializable{

    public enum UserActivity {
        EDIT_INFO, NO_EDIT
    }
    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern pattern = Pattern.compile(EMAIL_REGEX);

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
    @FXML
    private Button clearFieldsButton;
    @FXML
    private Button statisticsButton;

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
    @FXML
    private Label reminderLabel;

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
    @FXML private TextField textFieldPassword;
    @FXML private Label subLabelPassword;
    @FXML private TextField textFieldRepeatPassword;
    @FXML private Label subLabelRepeatedPassword;

    //********* TextField Check Boxes *********
    @FXML
    private CheckBox flagFirstName;
    @FXML
    private CheckBox flagLastName;
    @FXML
    private CheckBox flagNationality;
    @FXML
    private CheckBox flagGender;
    @FXML
    private CheckBox flagDateOfBirth;
    @FXML
    private CheckBox flagEmail;
    @FXML
    private CheckBox flagPassword;

    //********* TextField Icons *********
    @FXML
    private FontAwesomeIconView iconFirstName;
    @FXML
    private FontAwesomeIconView iconLastName;
    @FXML
    private FontAwesomeIconView iconNationality;
    @FXML
    private FontAwesomeIconView iconGender1;
    @FXML
    private FontAwesomeIconView iconGender2;
    @FXML
    private FontAwesomeIconView iconCalendar;
    @FXML
    private FontAwesomeIconView iconEmail;
    @FXML
    private FontAwesomeIconView iconPassword;
    @FXML
    private FontAwesomeIconView iconRepeatPassword;
    @FXML
    private FontAwesomeIconView iconClearFields;
    @FXML
    private FontAwesomeIconView iconSaveChanges;
    @FXML
    private FontAwesomeIconView iconCancel;


    //********* Others View Components *********
    @FXML
    private ImageView profileImage;

    //********* Autowireds *********
    @Autowired
    private UserDBMongo userDBMongo;
    @Autowired
    private UserService serviceUser;
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
        this.initComboBox();
        initDisplay();
    }

    //********** On Click Button Methods **********
    public void onClickEditAccountInfoButton() {
        this.selectedOperation = UserActivity.EDIT_INFO;
        this.cancelButton.setVisible(true);
        this.saveChangesButton.setVisible(true);
        setEditFieldsVisibility(true);
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

    public void onClickStatisticsButton() {
        stageManager.switchScene(FxmlView.STATISTICS);
    }
    public void onClickCancelButton() {
        clearFields(); initDisplay();
    }
    public void onClickClearFieldsButton() {
        clearFields();
    }
    public void onClickDeleteAccountButton() {

        boolean userChoice = stageManager.showDeleteAccountInfoMessage();
        if (!userChoice) {
            return;
        }
        if (serviceUser.deleteUser(regUser)){
            modelBean.putBean(Constants.CURRENT_USER, null);
            stageManager.switchScene(FxmlView.WELCOMEPAGE);
            stageManager.showInfoMessage("Delete Operation", "Your Account Was Successfully " +
                    "Deleted From BoardGame-Cafè App." +
                    "\n\n\t\t\tWe Hope You Can Sign-Up Again Soon.");
        } else {
            modelBean.putBean(Constants.CURRENT_USER, null);
            stageManager.switchScene(FxmlView.WELCOMEPAGE);
            stageManager.showInfoMessage("Delete Operation", "An Unexpected Error Occurred " +
                    "While Deleting Your Account From BoardGame-Café_App." +
                    "\n\n\t\t\tPlease contact the administrator.");
        }
    }
    public void onClickSaveChangesButton() {

        if(regUser != null){
            // Ottenere i dati dai campi di input
            String firstName = this.textFieldFirstName.getText();
            String lastName = this.textFieldLastName.getText();
            String country = this.comboBoxNationality.getValue();
            String gender = this.comboBoxGender.getValue();
            LocalDateTime dateOfBirth = selectDate();
            String email = selectEmail();
            String password = this.textFieldPassword.getText();
            String repeatedPassword = this.textFieldRepeatPassword.getText();
            // Gestione delle checkbox
            boolean updateFirstName = this.flagFirstName.isSelected();
            boolean updateLastName = this.flagLastName.isSelected();
            boolean updateNationality = this.flagNationality.isSelected();
            boolean updateGender = this.flagGender.isSelected();
            boolean updateDateOfBirth = this.flagDateOfBirth.isSelected();
            boolean updateEmail = this.flagEmail.isSelected();
            boolean updatePassword = this.flagPassword.isSelected();

            // Verifica che almeno una checkbox sia selezionata
            if (!(updateFirstName || updateLastName || updateNationality || updateGender ||
                    updateDateOfBirth || updateEmail || updatePassword)) {
                clearFields();
                stageManager.showInfoMessage("Error", "Please select at least one field box to update.");
                return;
            }

            // Variabile di validazione
            boolean isValid = true;

            // Validazione condizionata in base alla selezione delle checkbox
            if (updateFirstName) {
                if (firstName.isEmpty()) {
                    subLabelFirstName.setText("First Name is missing.");
                    isValid = false;
                } else {
                    subLabelFirstName.setText("");
                }
            }
            if (updateLastName) {
                if (lastName.isEmpty()) {
                    subLabelLastName.setText("Last Name is missing.");
                    isValid = false;
                } else {
                    subLabelLastName.setText("");
                }
            }
            if (updateNationality) {
                if (country == null) {
                    subLabelNationality.setText("Country is missing.");
                    isValid = false;
                } else {
                    subLabelNationality.setText("");
                }
            }
            if (updateGender) {
                if (gender == null) {
                    subLabelGender.setText("Gender is missing.");
                    isValid = false;
                } else {
                    subLabelGender.setText("");
                }
            }
            if (updateDateOfBirth) {
                if (dateOfBirth == null) {
                    subLabelDate.setText("Date of Birth is missing.");
                    isValid = false;
                } else {
                    subLabelDate.setText("");
                }
            }
            if (updateEmail) {
                if (email.isEmpty()) {
                    subLabelEmail.setText("E-mail is missing.");
                    isValid = false;
                } else if (email.equals("user_banned")) {
                    subLabelEmail.setText("E-mail already used by banned user.");
                    isValid = false;
                } else if (email.equals("already_used")) {
                    subLabelEmail.setText("E-mail already used.");
                    isValid = false;
                } else if (!validateEmail(email)) {
                    subLabelEmail.setText("E-mail not valid.");
                    isValid = false;
                } else {
                    subLabelEmail.setText("");
                }
            }
            if (updatePassword) {
                if (password.isEmpty()) {
                    subLabelPassword.setText("Password is missing.");
                    subLabelRepeatedPassword.setText("");
                    isValid = false;
                } else if (repeatedPassword.isEmpty()) {
                    subLabelPassword.setText("");
                    subLabelRepeatedPassword.setText("Repeat the password.");
                    isValid = false;
                } else if (!password.equals(repeatedPassword)) {
                    subLabelPassword.setText("");
                    subLabelRepeatedPassword.setText("The two passwords above do not match.");
                    isValid = false;
                } else {
                    subLabelPassword.setText("");
                    subLabelRepeatedPassword.setText("");
                }
            }

            if (isValid) {
                // Esegui l'aggiornamento del modello utente come prima
                UserModelMongo newUser = new UserModelMongo();

                if (updateFirstName) newUser.setName(firstName);
                else newUser.setName(regUser.getName());

                if (updateLastName) newUser.setSurname(lastName);
                else newUser.setSurname(regUser.getSurname());

                if (updateNationality) newUser.setNationality(country);
                else newUser.setNationality(regUser.getNationality());

                if (updateGender) newUser.setGender(gender);
                else newUser.setGender(regUser.getGender());

                if (updateDateOfBirth) newUser.setDateOfBirth(dateOfBirth.toLocalDate());
                else newUser.setDateOfBirth(regUser.getDateOfBirth());

                if (updateEmail) newUser.setEmail(email);
                else newUser.setEmail(regUser.getEmail());

                if (updatePassword) {
                    newUser.setSalt(regUser.getSalt());
                    String hashedPassword = serviceUser.getHashedPassword(password,newUser.getSalt());
                    newUser.setPasswordHashed(hashedPassword);
                } else {
                    newUser.setSalt(regUser.getSalt());
                    newUser.setPasswordHashed(regUser.getPasswordHashed());
                }

                newUser.setId(regUser.getId());
                newUser.setUsername(regUser.getUsername());
                newUser.setBanned(regUser.isBanned());
                newUser.setReviews(regUser.getReviews());

                if (updateDbms(newUser)){
                    modelBean.putBean(Constants.CURRENT_USER, newUser);
                    stageManager.showInfoMessage("Update Info: ",
                            "Your account information has been successfully updated!");
                    initDisplay();
                }
            }
        } else {
            stageManager.showInfoMessage("Update Error: ",
                    "There Is No Logged-In User To Update ");
        }
    }


    //********** Internal Methods **********
    private void initDisplay(){
        clearFields();
        regUser = (UserModelMongo) modelBean.getBean(Constants.CURRENT_USER);
        this.accountInfoButton.setDisable(true);
        this.selectedOperation = UserActivity.NO_EDIT;

        String formattedDate = regUser.getDateOfBirth().format(DateTimeFormatter.ofPattern("MM-dd-yyyy"));
        Image image = new Image(Objects.requireNonNull(getClass().
                getResource("/user.png")).toExternalForm());
        this.profileImage.setImage(image);

        this.firstNameLabel.setText(regUser.getName());
        this.lastNameLabel.setText(regUser.getSurname());
        this.nationalityLabel.setText(regUser.getNationality());
        this.genderLabel.setText(regUser.getGender());
        this.dateOfBirthLabel.setText(formattedDate);
        this.emailLabel.setText(regUser.getEmail());
        this.usernameLabel.setText(regUser.getUsername());
        this.passwordLabel.setText("***************");

        setEditFieldsVisibility(false);
    }

    private boolean updateDbms(UserModelMongo newUser){

        boolean mongoUpdateUser = userDBMongo.updateUser(newUser.getId(), newUser, "user");

        if (!mongoUpdateUser) {
            stageManager.showInfoMessage("Update Error: ",
                    "There was an error updating your account information. " +
                            "Please try again.");
            initDisplay();
            return false;
        }
        return true;
    }

    private void setEditFieldsVisibility(boolean isVisible) {
        //********** Actual Labels **********
        this.firstNameLabel.setDisable(isVisible);
        this.lastNameLabel.setDisable(isVisible);
        this.nationalityLabel.setDisable(isVisible);
        this.genderLabel.setDisable(isVisible);
        this.dateOfBirthLabel.setDisable(isVisible);
        this.emailLabel.setDisable(isVisible);
        this.usernameLabel.setDisable(isVisible);
        this.passwordLabel.setDisable(isVisible);
        this.reminderLabel.setVisible(isVisible);
        //********** TextFields & SubLabels **********
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
        this.textFieldPassword.setVisible(isVisible);
        this.subLabelPassword.setVisible(isVisible);
        this.textFieldRepeatPassword.setVisible(isVisible);
        this.subLabelRepeatedPassword.setVisible(isVisible);
        //********** Icons **********
        this.iconFirstName.setVisible(isVisible);
        this.iconLastName.setVisible(isVisible);
        this.iconNationality.setVisible(isVisible);
        this.iconGender1.setVisible(isVisible);
        this.iconGender2.setVisible(isVisible);
        this.iconCalendar.setVisible(isVisible);
        this.iconEmail.setVisible(isVisible);
        this.iconPassword.setVisible(isVisible);
        this.iconRepeatPassword.setVisible(isVisible);
        this.iconClearFields.setVisible(isVisible);
        this.iconCancel.setVisible(isVisible);
        this.iconSaveChanges.setVisible(isVisible);
        //********** Related CheckBoxes **********
        this.flagFirstName.setVisible(isVisible);
        this.flagLastName.setVisible(isVisible);
        this.flagNationality.setVisible(isVisible);
        this.flagGender.setVisible(isVisible);
        this.flagDateOfBirth.setVisible(isVisible);
        this.flagEmail.setVisible(isVisible);
        this.flagPassword.setVisible(isVisible);
        //********** Related Buttons **********
        this.cancelButton.setVisible(isVisible);
        this.saveChangesButton.setVisible(isVisible);
        this.clearFieldsButton.setVisible(isVisible);
        this.deleteAccountButton.setDisable(isVisible);
        this.editAccountInfoButton.setDisable(isVisible);
    }

    public void clearFields(){
        //********** TextFields & SubLabels **********
        this.textFieldFirstName.clear();
        this.textFieldFirstName.setPromptText("First Name");
        this.subLabelFirstName.setText("");
        this.textFieldLastName.clear();
        this.textFieldLastName.setPromptText("Laset Name");
        this.subLabelLastName.setText("");
        this.comboBoxNationality.setValue(null);
        this.comboBoxNationality.setPromptText("Nationality");
        this.subLabelNationality.setText("");
        this.comboBoxGender.setValue(null);
        this.comboBoxGender.setPromptText("Gender");
        this.subLabelGender.setText("");
        this.datePickerDate.setValue(null);
        this.datePickerDate.setPromptText("Date Of Birth");
        this.subLabelDate.setText("");
        this.textFieldEmail.clear();
        this.textFieldEmail.setPromptText("E-mail");
        this.subLabelEmail.setText("");
        this.textFieldPassword.clear();
        this.textFieldPassword.setPromptText("Password");
        this.subLabelPassword.setText("");
        this.textFieldRepeatPassword.clear();
        this.textFieldRepeatPassword.setPromptText("Repeat Password");
        this.subLabelRepeatedPassword.setText("");
        //********** Related CheckBoxes **********
        this.flagFirstName.setSelected(false);
        this.flagLastName.setSelected(false);
        this.flagNationality.setSelected(false);
        this.flagGender.setSelected(false);
        this.flagDateOfBirth.setSelected(false);
        this.flagEmail.setSelected(false);
        this.flagPassword.setSelected(false);
    }

    private void resetPage() {}

    public LocalDateTime selectDate() {
        LocalDate selectedDate = this.datePickerDate.getValue();
        LocalDate currentDate = LocalDate.now();

        if (selectedDate == null) {
            subLabelDate.setText("Date of Birth is missing.");
        } else if (selectedDate.isAfter(currentDate)) {
            subLabelDate.setText("Were you born in the future?");
        } else {
            LocalDateTime dateTime = selectedDate.atStartOfDay();
            //String formattedDate = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
            Period age = Period.between(selectedDate, currentDate);

            if (age.getYears() < 13 ) {
                subLabelDate.setText("You must be at least 13 years old.");
            } else {
                subLabelDate.setText("");
                return dateTime;
            }
        }
        return null;
    }

    public String selectEmail() {
        //se email già presente o bannata: messaggio di errore.
        Optional<GenericUserModelMongo> user = userDBMongo.
                findByEmail(this.textFieldEmail.getText());
        if (user.isPresent()) {
            UserModelMongo userFromMongo = (UserModelMongo) user.get();
            if (userFromMongo.isBanned())
                return "user_banned";
            return "already_used";
        }
        return this.textFieldEmail.getText();
    }

    public static boolean validateEmail(String email) {
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void initComboBox() {
        this.comboBoxGender.getItems().addAll("Male","Female", "Other");
        this.comboBoxNationality.getItems().addAll("Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla", "Antarctica",
                "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas",
                "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan",
                "Bolivia", "Bonaire Sint Eustatius and Saba", "Bosnia and Herzegovina", "Botswana", "Bouvet Island",
                "Brazil", "British Indian Ocean Territory", "Brunei Darussalam", "Bulgaria", "Burkina Faso", "Burundi",
                "Cabo Verde", "Cambodia", "Cameroon", "Canada", "Cayman Islands", "Central African Republic", "Chad",
                "Chile", "China", "Christmas Island", "Cocos Islands", "Colombia", "Comoros", "Congo", "Congo",
                "Cook Islands", "Costa Rica", "Croatia", "Cuba", "Curaçao", "Cyprus", "Czechia", "Côte d'Ivoire",
                "Denmark", "Djibouti", "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador",
                "Equatorial Guinea", "Eritrea", "Estonia", "Eswatini", "Ethiopia", "Falkland Islands", "Faroe Islands",
                "Fiji", "Finland", "France", "French Guiana", "French Polynesia", "French Southern Territories",
                "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada",
                "Guadeloupe", "Guam", "Guatemala", "Guernsey", "Guinea", "Guinea-Bissau", "Guyana", "Haiti",
                "Heard Island and McDonald Islands", "Holy See", "Honduras", "Hong Kong", "Hungary", "Iceland",
                "India", "Indonesia", "Iran", "Iraq", "Ireland", "Isle of Man", "Israel", "Italy", "Jamaica",
                "Japan", "Jersey", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "North Korea", "South Korea",
                "Kuwait", "Kyrgyzstan", "Lao", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya", "Liechtenstein",
                "Lithuania", "Luxembourg", "Macao", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta",
                "Marshall Islands", "Martinique", "Mauritania", "Mauritius", "Mayotte", "Mexico", "Micronesia",
                "Moldova", "Monaco", "Mongolia", "Montenegro", "Montserrat", "Morocco", "Mozambique", "Myanmar",
                "Namibia", "Nauru", "Nepal", "Netherlands", "New Caledonia", "New Zealand", "Nicaragua", "Niger",
                "Nigeria", "Niue", "Norfolk Island", "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau",
                "Palestine State of", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Pitcairn",
                "Poland", "Portugal", "Puerto Rico", "Qatar", "Republic of North Macedonia", "Romania",
                "Russian Federation", "Rwanda", "Réunion", "Saint Barthélemy", "Saint Helena Ascension and Tristan da Cunha",
                "Saint Kitts and Nevis", "Saint Lucia", "Saint Martin", "Saint Pierre and Miquelon",
                "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia",
                "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Sint Maarten", "Slovakia", "Slovenia",
                "Solomon Islands", "Somalia", "South Africa", "South Georgia and the South Sandwich Islands", "South Sudan",
                "Spain", "Sri Lanka", "Sudan", "Suriname", "Svalbard and Jan Mayen", "Sweden", "Switzerland",
                "Syrian Arab Republic", "Taiwan", "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", "Togo", "Tokelau",
                "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Turks and Caicos Islands",
                "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United States Minor Outlying Islands",
                "United States of America", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Viet Nam", "Virgin Islands",
                "Virgin Islands", "Wallis and Futuna", "Western Sahara", "Yemen", "Zambia", "Zimbabwe", "Åland Islands");
    }

}
