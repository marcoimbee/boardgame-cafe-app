package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.AdminModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.services.UserService;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class ControllerViewLogin implements Initializable {

    @FXML
    private TextField textFieldUsername;
    @FXML
    private TextField textFieldPassword;
    @FXML
    private Button cancelButton;
    @FXML
    private Button loginButton;

    @Autowired
    private ModelBean modelBean;
    @Autowired
    private UserDBMongo userMongoOp;
    @Autowired
    private UserService serviceUser;

    private final StageManager stageManager;

    @Autowired
    @Lazy
    public ControllerViewLogin(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    public void onClickLogin() {
        List<UserModelMongo> usersPath = new ArrayList<>();
        modelBean.putBean(Constants.USERS_PATH, usersPath);
        String password = textFieldPassword.getText();
        String username = textFieldUsername.getText().trim();
        if (password.isEmpty() && username.isEmpty()) {
            stageManager.showInfoMessage("INFO", "Please insert your username and password");
            return;
        } else if (username.isEmpty()) {
            stageManager.showInfoMessage("INFO", "Please insert your username");
            return;
        } else if (password.isEmpty()) {
            stageManager.showInfoMessage("INFO", "Please insert your password");
            return;
        }

        try {
            Optional<GenericUserModelMongo> genericUser = userMongoOp.findByUsername(username, true);
            if (genericUser.isEmpty()) {
                stageManager.showInfoMessage("INFO", "Wrong username or password");
                this.clearFields();
                return;
            }
            String salt = genericUser.get().getSalt();
            String hashedPassword = serviceUser.getHashedPassword(password, salt);
            if (!genericUser.get().getPasswordHashed().equals(hashedPassword)) {
                stageManager.showInfoMessage("INFO", "Wrong username or password");
                this.clearFields();
                return;
            }

            // Checking if the user is banned
            if (genericUser.get() instanceof UserModelMongo user && user.isBanned()) {
                stageManager.showInfoMessage("INFO", "You have been banned from this application. You will be able to login again if unbanned.");
                this.clearFields();
                return;
            }

            if (genericUser.get().get_class().equals("admin")) {
                AdminModelMongo admin = (AdminModelMongo) genericUser.get();
                modelBean.putBean(Constants.CURRENT_USER, admin);
                stageManager.switchScene(FxmlView.STATISTICS);
                System.out.println("[INFO] LOGGED IN AS ADMIN");
            } else {
                UserModelMongo user = (UserModelMongo) genericUser.get();
                modelBean.putBean(Constants.CURRENT_USER, user);
                stageManager.switchScene(FxmlView.USERPROFILEPAGE);
                System.out.println("[INFO] LOGGED IN AS REGULAR USER");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] onClickLogin()@ControllerViewLogin.java raised an exception: " + e.getMessage());
            stageManager.showInfoMessage("INFO", "Something went wrong. Please try again in a while.");
        }
    }

    private void clearFields() {
        this.textFieldUsername.clear();
        this.textFieldPassword.clear();
    }

    public void onClickCancelButton() {
        stageManager.closeStageButton(this.cancelButton);
        stageManager.showWindow(FxmlView.WELCOMEPAGE);
    }

    public void onClickEnter(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER) this.onClickLogin();
    }
}
