package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import javafx.scene.input.MouseEvent;


public interface UserListener {

    public void onClickUserListener(MouseEvent mouseEvent, UserModelMongo post);
}
