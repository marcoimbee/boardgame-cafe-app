package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BoardgameDBMongo {


    private final static Logger logger = LoggerFactory.getLogger(BoardgameDBMongo.class);

    @Autowired
    private BoardgameRepoMongo boardgameRepoMongoOp;
    @Autowired
    private MongoOperations mongoOperations;

    public BoardgameDBMongo() {}

    public boolean addBoardgame(BoardgameModelMongo boardgame) {
        boolean result = true;
        try {
            boardgameRepoMongoOp.save(boardgame);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deleteBoardgame(BoardgameModelMongo boardgame) {
        try {
            boardgameRepoMongoOp.delete(boardgame);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Optional<BoardgameModelMongo> findBoardgameByName(String boardgameName) {
        Optional<BoardgameModelMongo> boardgame = Optional.empty();
        try {
            boardgame = boardgameRepoMongoOp.findByBoardgameName(boardgameName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return boardgame;
    }

    public boolean updateBoardgameMongo(String id, BoardgameModelMongo newBoardgame) {
        boolean result = true;
        try {
            Optional<BoardgameModelMongo> boardgame = boardgameRepoMongoOp.findById(id);
            if (boardgame.isPresent()) {
                boardgame.get().setBoardgameName(newBoardgame.getBoardgameName());
                boardgame.get().setThumbnail(newBoardgame.getThumbnail());
                boardgame.get().setImage(newBoardgame.getImage());
                boardgame.get().setDescription(newBoardgame.getDescription());
                boardgame.get().setYearPublished(newBoardgame.getYearPublished());
                boardgame.get().setMinPlayers(newBoardgame.getMinPlayers());
                boardgame.get().setMaxPlayers(newBoardgame.getMaxPlayers());
                boardgame.get().setPlayingTime(newBoardgame.getPlayingTime());
                boardgame.get().setMinAge(newBoardgame.getMinAge());
                boardgame.get().setBoardgameCategoryList(newBoardgame.getBoardgameCategoryList());
                boardgame.get().setBoardgameDesignerList(newBoardgame.getBoardgameDesignerList());
                boardgame.get().setBoardgamePublisherList(newBoardgame.getBoardgamePublisherList());
                boardgame.get().setReviews(newBoardgame.getReviews());

                boardgameRepoMongoOp.save(boardgame.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public List<BoardgameModelMongo> findRecentBoardgames(int limit, int skip) {
        List<BoardgameModelMongo> boardgames = null;
        try {
            Query query = new Query();
            query.with(Sort.by(Sort.Direction.DESC, "yearPublished"));
            query.skip(skip).limit(limit);
            boardgames = mongoOperations.find(query, BoardgameModelMongo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return boardgames;
    }

}