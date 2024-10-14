package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import javafx.scene.image.Image;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener.BoardgameListener;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import org.springframework.stereotype.Component;

@Component
public class ControllerObjectBoardgame {
    @FXML
    private ImageView imageSrc;
    @FXML
    protected Label boardgameName;

    private BoardgameListener boardgameListener;

    public ControllerObjectBoardgame() {}

    // Caching in memory per le immagini, migliora l'efficienza dell'applicazione ed evita l'eventuale
    // scaricamento multiplo di una stessa immagine dal server
    private static final Map<String, Image> imageCache = new ConcurrentHashMap<>();

    public void setData(BoardgameModelMongo boardgame) {

        String imageBoardgameURL = boardgame.getImage(); // URL dell'immagine
        String nameBoardgameResource = boardgame.getBoardgameName();

        Image image = getImageFromCache(imageBoardgameURL); // Tenta di recuperare l'immagine dalla cache

        if (image == null) { // Se l'immagine non è nella cache
            try {
                URI uri = new URI(imageBoardgameURL); // Create a URI object from the URL string
                URL url = uri.toURL(); // Convert the URI to a URL object using toURL()

                URLConnection connection = url.openConnection();
                connection.setRequestProperty("User-Agent", "JavaFX Application");
                InputStream inputStream = connection.getInputStream();
                byte[] imageBytes = readFullInputStream(inputStream);

                image = new Image(new ByteArrayInputStream(imageBytes)); // Crea l'oggetto Image
                addImageToCache(imageBoardgameURL, image); // Aggiungi l'immagine alla cache

            } catch (URISyntaxException | IOException e) { // Handle both URISyntaxException and IOException
                e.printStackTrace();
            }
        }

        Image selectedImage = image;
        boardgameName.setText(nameBoardgameResource);
        imageSrc.setImage(selectedImage);
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

    /*
    public void setData(BoardgameModelMongo boardgame){
        String imageBoardgameResource = boardgame.getImage();
        String nameBoardgameResource = boardgame.getBoardgameName();
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imageBoardgameResource)));
        boardgameName.setText(nameBoardgameResource);
        imageSrc.setImage(image);
    }*/

}
