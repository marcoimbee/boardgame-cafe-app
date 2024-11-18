package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.BoardgameListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Constants;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ControllerViewBoardgameDetails implements Initializable {

    private BoardgameModelMongo boargame;
    @FXML
    private ImageView imgViewBoardgame;
    @FXML
    private Label lblBoardgameName;
    @FXML
    private TextArea txtAreaDescription;
    @FXML
    private Label lblYearPublished;
    @FXML
    private Label lblMinPlayers;
    @FXML
    private Label lblMaxPlayers;
    @FXML
    private Label lblPlayingTime;
    @FXML
    private Label lblMinAge;
    @FXML
    private Button backButton;

    @Autowired
    private ModelBean modelBean;

    private BoardgameModelMongo boardgame;

    private BoardgameListener boardgameListener;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        this.boardgame = (BoardgameModelMongo)modelBean.getBean(Constants.SELECTED_BOARDGAME);
        this.prepareScene();
    }

    private void prepareScene()
    {
        this.setImage();
        this.lblBoardgameName.setText(this.boardgame.getBoardgameName());
        this.lblMinPlayers.setText(String.valueOf(this.boardgame.getMinPlayers()));
        this.lblMaxPlayers.setText(String.valueOf(this.boardgame.getMaxPlayers()));
        this.lblMinAge.setText(String.valueOf(this.boardgame.getMinAge()));
        this.lblPlayingTime.setText(String.valueOf(this.boardgame.getPlayingTime()));
        this.lblYearPublished.setText(String.valueOf(this.boardgame.getYearPublished()));
        this.txtAreaDescription.setText(this.boardgame.getDescription());
    }

    private void setImage()
    {
        Image imageInCache = ControllerObjectBoardgame.getImageFromCache(this.boardgame.getImage());
        if (imageInCache != null)
        {
            this.imgViewBoardgame.setImage(imageInCache);
            return;
        }
        try {
            URI uri = new URI(this.boardgame.getImage()); // Crea URI
            URL url = uri.toURL(); // Converti a URL
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "JavaFX Application");

            try (InputStream inputStream = connection.getInputStream()) {
                byte[] imageBytes = readFullInputStream(inputStream);
                Image downloadedImage = new Image(new ByteArrayInputStream(imageBytes));
                this.imgViewBoardgame.setImage(downloadedImage);
            }
        }
        catch (Exception e)  { System.out.println("ControllerViewBoardgameDetails: download boardgame image failed"); }
    }

    private byte[] readFullInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public void onClickBackButton(MouseEvent event)
    {

    }
}
