package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.HashMap;
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
    private boolean toFind = false;

    /**
     * method that set the data of the item and set the videogame class variable that will
     * be passed at the onClickListener method by the click function
     * @param boardgame clicked
     * param boardgameListener object to identify the correct on click listener
     * param toFind parameter that discriminate if make or not the db call
     * @throws FileNotFoundException
     *
     * , BoardgameListener boardgameListener, boolean toFind
     */


    // Cache in memoria per le immagini per migliorare l'efficienza dell'applicazione ed evitare di scaricare
    // più volte una stessa immagine dal server
    private static final Map<String, Image> imageCache = new HashMap<>();

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
                // Potresti impostare un'immagine di placeholder o mostrare un messaggio di errore
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
        //this.boardgame = boardgame;
        //this.boardgameListener = boardgameListener;
        //this.toFind = toFind;
        String imageBoardgameResource = boardgame.getImage();
        String nameBoardgameResource = boardgame.getBoardgameName();
        Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imageBoardgameResource)));
        boardgameName.setText(nameBoardgameResource);
        imageSrc.setImage(image);
    }*/

    /**
     * method that call the on click listener that is redefined in the different fill grid pane
     * in the different pages,
     * the listener has as parameter userFromDB that is the object user retrieved from the db
     * using the find method applied to the username obtained by the setData
     *
     * @param mouseEvent of the mouse when the videogame community item is clicked

    @FXML
    public void clickMouse(javafx.scene.input.MouseEvent mouseEvent) {

        if(this.toFind){
            VideogameCommunity videogameFromDB = videogameService.find(videogame.getId());
            videogameListener.onClickListener(mouseEvent, videogameFromDB);
        }
        else{
            videogameListener.onClickListener(mouseEvent, videogame);
        }
    }*/
}
