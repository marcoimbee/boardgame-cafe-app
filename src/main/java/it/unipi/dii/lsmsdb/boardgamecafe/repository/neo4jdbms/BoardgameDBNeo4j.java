package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jOperations;
import org.springframework.stereotype.Component;

import java.util.Optional;


@Component
public class BoardgameDBNeo4j {

    @Autowired
    BoardgameRepoNeo4j boardgameRepoNeo4jOp;
    @Autowired
    Neo4jOperations neo4jOperations;

    public BoardgameRepoNeo4j getUserNeo4jDB() {
        return boardgameRepoNeo4jOp;
    }


    public boolean addBoardgame(BoardgameModelNeo4j boardgameNeo4j) {
        boolean result = true;
        try {
            boardgameRepoNeo4jOp.save(boardgameNeo4j);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deleteBoardgameDetach(String boardgameName) {
        try {
            boardgameRepoNeo4jOp.deleteAndDetachBoardgameByName(boardgameName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean updateBoardgameNeo4j(String id, BoardgameModelNeo4j newBoardgame) {

        boolean result = true;
        try {
            Optional<BoardgameModelNeo4j> boardgameNeo = boardgameRepoNeo4jOp.findById(id);
            if (boardgameNeo.isPresent()) {
                boardgameNeo.get().setName(newBoardgame.getName());
                boardgameNeo.get().setImage(newBoardgame.getImage());
                boardgameNeo.get().setYearPublished(newBoardgame.getYearPublished());

                boardgameRepoNeo4jOp.save(boardgameNeo.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

}
