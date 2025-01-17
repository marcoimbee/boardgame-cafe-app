package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import javafx.scene.input.MouseEvent;
import org.springframework.stereotype.Component;


public interface BoardgameListener {

    public void onClickBoardgameListener(MouseEvent mouseEvent, String boardgameId);
}
