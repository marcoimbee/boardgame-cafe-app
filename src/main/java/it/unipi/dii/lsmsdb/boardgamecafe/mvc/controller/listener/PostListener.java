package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import javafx.scene.input.MouseEvent;

public interface PostListener {
    void onClickPostListener(MouseEvent mouseEvent, PostModelMongo post);
}
