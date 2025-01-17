package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.BoardgameListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ControllerObjectBoardgame implements Initializable {
    @FXML
    public AnchorPane anchorPane;
    @FXML
    private ImageView bgameImage;
    @FXML
    protected Label lblBoardgameName;
    @FXML
    protected Tooltip tooltipBoardgameText;
    @FXML
    private Label lblRating;

    private BoardgameModelNeo4j boardgame;

    private BoardgameListener boardgameClickListener;

    public ControllerObjectBoardgame() {}

    // Caching in memory per le immagini, migliora l'efficienza dell'applicazione ed evita l'eventuale
    // scaricamento multiplo di una stessa immagine dal server
    private static final ConcurrentHashMap<String, Image> imageCache = new ConcurrentHashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        this.lblBoardgameName.setTooltip(null);
        this.lblRating.setTooltip(null);
    }

    public void setData(BoardgameModelNeo4j boardgame, BoardgameListener listener, AnchorPane anchorPane, Double ratingForThisGame) {

        this.boardgame = boardgame;
        this.boardgameClickListener = listener;
        if (ratingForThisGame != null)
        {
            this.lblRating.setText("Rating: " + String.format("%.1f", ratingForThisGame));
            this.lblRating.setVisible(true);
        }
        else
            this.lblRating.setVisible(false);

        String imageBoardgameURL = boardgame.getImage(); // URL dell'immagine
        String nameBoardgameResource = boardgame.getBoardgameName();

        Image image = getImageFromCache(imageBoardgameURL); // Tenta di recuperare l'immagine dalla cache
        lblBoardgameName.setText(nameBoardgameResource);
        if (image == null) { // Se l'immagine non è nella cache
            try {
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
                        catch (Exception e) // Se il link è sbagliato, si mostra un'immagine di default
                        {
                            System.out.println("Exception with downloading the image -> " + e.getMessage());
                            String imagePath = getClass().getResource("/images/noImage.jpg").toExternalForm();
                            Image image = new Image(imagePath);
                            return image;
                        }
                    }
                };
                imageDownloadTask.setOnSucceeded(e -> {
                    Image downloadedImage = imageDownloadTask.getValue();
                    ImageView bgameImage =((ImageView)anchorPane.lookup("#bgameImage"));
                    bgameImage.setImage(downloadedImage);
                    bgameImage.setAccessibleText(boardgame.getDescription()
                            .replaceAll("&#[0-9]+;", "").replaceAll("&[a-zA-Z0-9]+;", ""));
                    ((Label)anchorPane.lookup("#lblBoardgameName")).setText(nameBoardgameResource);
                });
                imageDownloadTask.setOnFailed(e ->
                {
                    System.out.println("Eccezione Download image -> " + imageDownloadTask.getException().getMessage());
                    String imagePath = getClass().getResource("/images/noImage.jpg").toExternalForm();
                    ImageView bgameImage =((ImageView)anchorPane.lookup("#bgameImage"));
                    Image imageNoAvailable = new Image(imagePath);
                    bgameImage.setImage(imageNoAvailable);
                    bgameImage.setAccessibleText(boardgame.getDescription()
                            .replaceAll("&#[0-9]+;", "").replaceAll("&[a-zA-Z0-9]+;", ""));
                    ((Label)anchorPane.lookup("#lblBoardgameName")).setText(nameBoardgameResource);
                } );

                Thread imageDownloadThread = new Thread(imageDownloadTask);
                imageDownloadThread.setDaemon(true); // Rende il thread secondario
                imageDownloadThread.start();
            }
            catch (Exception e) {
                System.out.println("Exception with URL image -> " + e.getMessage());
            }
        }
        else {
            bgameImage.setImage(getImageFromCache(imageBoardgameURL));
            bgameImage.setAccessibleText(boardgame.getDescription());
        }
    }

    public static Image getImageFromCache(String imageURL) {
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

    //    public void onMouseEnteredOnImageBoardgame(MouseEvent event)
//    {
//        //System.out.println("Evento entered");
//        ImageView imageEvent = (ImageView)event.getSource();
//        tooltipBoardgameText.setText(imageEvent.getAccessibleText()); // Truncate the description to 50 characters.
//        tooltipBoardgameText.show(imageEvent, event.getSceneX(), event.getSceneY());
//    }
//
    public void onMouseExitedFromImageBoardgame(MouseEvent event)
    {
        tooltipBoardgameText.hide();
    }

    public void onMouseMovedOnImageBoardgame(MouseEvent event)
    {
        ImageView imageEvent = (ImageView)event.getSource();
        tooltipBoardgameText.setText(imageEvent.getAccessibleText()); // Truncate the description to 50 characters.
        tooltipBoardgameText.show(bgameImage, event.getScreenX() + 10, event.getScreenY() + 10); // Offset di 10px
    }

}
