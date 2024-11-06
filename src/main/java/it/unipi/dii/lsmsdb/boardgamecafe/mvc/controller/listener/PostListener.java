package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import javafx.scene.input.MouseEvent;
import org.springframework.stereotype.Component;


public interface PostListener {

    public void onClickPostListener(MouseEvent mouseEvent, PostModelMongo post);
}
