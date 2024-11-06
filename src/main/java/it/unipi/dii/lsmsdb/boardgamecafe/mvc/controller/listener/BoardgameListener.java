package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import javafx.scene.input.MouseEvent;
import org.springframework.stereotype.Component;


public interface BoardgameListener {

    public void onClickBoardgameListener(MouseEvent mouseEvent, BoardgameModelMongo boardgame);
}
