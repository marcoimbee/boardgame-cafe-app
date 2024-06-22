package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;

public interface BoardgameListener {

    public void onClickListener(javafx.scene.input.MouseEvent mouseEvent, BoardgameModelMongo boardgame);
}
