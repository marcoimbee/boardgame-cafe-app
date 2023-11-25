package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameNeo4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.Neo4jOperations;

public class BoardgameNeo4jDB {

    @Autowired
    BoardgameRepositoryNeo4j boardgameNeo4jDB;
    @Autowired
    Neo4jOperations neo4jOperations;

    public BoardgameRepositoryNeo4j getUserNeo4jDB() {
        return boardgameNeo4jDB;
    }


    public boolean addBoardgame(BoardgameNeo4j boardgameNeo4j) {
        boolean result = true;
        try {
            boardgameNeo4jDB.save(boardgameNeo4j);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }
}
