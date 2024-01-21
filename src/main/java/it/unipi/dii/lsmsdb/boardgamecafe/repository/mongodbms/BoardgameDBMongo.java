package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BoardgameDBMongo {

    public BoardgameDBMongo() {
    }

    //private final static Logger logger = LoggerFactory.getLogger(BoardgameModelMongo.class);

    @Autowired
    private BoardgameRepoMongo BoardgameRepoMongoOp;
    @Autowired
    private MongoOperations mongoOperations;

    public boolean addBoardgame(BoardgameModelMongo boardgame) {
        boolean result = true;
        try {
            BoardgameRepoMongoOp.save(boardgame);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deleteBoardgame(BoardgameModelMongo boardgame) {
        try {
            BoardgameRepoMongoOp.delete(boardgame);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Optional<BoardgameModelMongo> findBoardgameByName(String boardgameName) {
        Optional<BoardgameModelMongo> boardgame = Optional.empty();
        try {
            boardgame = BoardgameRepoMongoOp.findByBoardgameName(boardgameName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return boardgame;
    }
}
