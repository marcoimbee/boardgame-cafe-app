package it.unipi.dii.lsmsdb.boardgamecafe.mvc.controller.listener;

import javafx.scene.input.MouseEvent;

public interface BoardgameListener {
    void onClickBoardgameListener(MouseEvent mouseEvent, String boardgameId);
}
