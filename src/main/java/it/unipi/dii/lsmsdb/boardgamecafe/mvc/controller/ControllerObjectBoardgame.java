package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import javafx.concurrent.Task;
import javafx.fxml.Initializable;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javafx.scene.image.Image;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.BoardgameListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.springframework.stereotype.Component;

import javax.tools.Tool;

@Component
public class ControllerObjectBoardgame implements Initializable {
    @FXML
    private ImageView bgameImage;
    @FXML
    protected Label lblBoardgameName;
    @FXML
    protected Tooltip tooltipBoardgameText;

    private BoardgameModelMongo boardgame;

    private BoardgameListener boardgameListener;

    public ControllerObjectBoardgame() {}

    // Caching in memory per le immagini, migliora l'efficienza dell'applicazione ed evita l'eventuale
    // scaricamento multiplo di una stessa immagine dal server
    private static final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        tooltipBoardgameText.hide();
    }

    public void setData(BoardgameModelMongo boardgame, BoardgameListener listener, AnchorPane anchorPane) {

        this.boardgame = boardgame;
        this.boardgameListener = listener;

        String imageBoardgameURL = boardgame.getImage(); // URL dell'immagine
        String nameBoardgameResource = boardgame.getBoardgameName();

        Image image = getImageFromCache(imageBoardgameURL); // Tenta di recuperare l'immagine dalla cache

        if (image == null) { // Se l'immagine non è nella cache
            Task<Image> imageDownloadTask = new Task<>() {
                @Override
                protected Image call() throws Exception {
                    URI uri = new URI(imageBoardgameURL); // Crea URI
                    URL url = uri.toURL(); // Converti a URL
                    URLConnection connection = url.openConnection();
                    connection.setRequestProperty("User-Agent", "JavaFX Application");

                    try (InputStream inputStream = connection.getInputStream()) {
                        byte[] imageBytes = readFullInputStream(inputStream);
                        Image downloadedImage = new Image(new ByteArrayInputStream(imageBytes));
                        addImageToCache(imageBoardgameURL, downloadedImage); // Cache l'immagine scaricata
                        return downloadedImage;
                    }
                }
            };
            imageDownloadTask.setOnSucceeded(e -> {
                Image downloadedImage = imageDownloadTask.getValue();
                ImageView bgameImage =((ImageView)anchorPane.lookup("#bgameImage"));
                bgameImage.setImage(downloadedImage);
                bgameImage.setAccessibleText(boardgame.getDescription());
                ((Label)anchorPane.lookup("#lblBoardgameName")).setText(nameBoardgameResource);
            });
            imageDownloadTask.setOnFailed(e -> System.out.println("Eccezione Download image -> " + imageDownloadTask.getException().getMessage()) );

            Thread imageDownloadThread = new Thread(imageDownloadTask);
            imageDownloadThread.setDaemon(true); // Rende il thread secondario
            imageDownloadThread.start();
        }
        else {
            bgameImage.setImage(getImageFromCache(imageBoardgameURL));
            lblBoardgameName.setText(nameBoardgameResource);
            bgameImage.setAccessibleText(boardgame.getDescription());
        }


    }

    private Image getImageFromCache(String imageURL) {
        return imageCache.get(imageURL);
    }

    private void addImageToCache(String imageURL, Image image) {
        imageCache.put(imageURL, image);
    }

    // Metodo per leggere completamente un InputStream
    private static byte[] readFullInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        return byteArrayOutputStream.toByteArray();
    }

    public void onMouseEnteredOnImageBoardgame(MouseEvent event)
    {
        //System.out.println("Evento entered");
        ImageView imageEvent = (ImageView)event.getSource();
        tooltipBoardgameText.setText(imageEvent.getAccessibleText()); // Truncate the description to 50 characters.
        tooltipBoardgameText.show(imageEvent, event.getSceneX(), event.getSceneY());
    }

    public void onMouseExitedFromImageBoardgame(MouseEvent event)
    {
        tooltipBoardgameText.hide();
    }

    public void onMouseMovedOnImageBoardgame(MouseEvent event)
    {
        tooltipBoardgameText.setShowDelay(javafx.util.Duration.ZERO); // Mostra subito la tooltip
        tooltipBoardgameText.show(bgameImage, event.getScreenX() + 10, event.getScreenY() + 10); // Offset di 10px
    }

    @FXML
    void mouseClick(MouseEvent mouseEvent) {
        boardgameListener.onClickBoardgameListener(mouseEvent, boardgame);
    }

}
