package it.unipi.dii.lsmsdb.boardgamecafe.services;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.*;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.BoardgameDBNeo4j;

import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.CommentDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BoardgameService {

    @Autowired
    private BoardgameDBMongo boardgameMongoOp;
    @Autowired
    private BoardgameDBNeo4j boardgameNeo4jOp;
    @Autowired
    private ReviewDBMongo reviewMongoOp;
    @Autowired
    private UserDBMongo userMongoOp;
    @Autowired
    private PostDBNeo4j postNeo4jOp;
    @Autowired
    private PostDBMongo postMongoOp;
    @Autowired
    private CommentDBNeo4j commentNeo4jOp;
    @Autowired
    private CommentDBMongo commentMongoOp;

    private final static Logger logger = LoggerFactory.getLogger(BoardgameService.class);


    public boolean insertBoardgame(BoardgameModelMongo boardgameMongo) {
        boolean result = true;

        // Gestione MongoDB
        if (!boardgameMongoOp.addBoardgame(boardgameMongo)) {
            logger.error("Error in adding the user to MongoDB");
            return false;
        }
        // Aggiorna id
        boardgameMongo = boardgameMongoOp.findBoardgameByName(boardgameMongo.getBoardgameName()).get();

        // Gestione Neo4j
        if (!boardgameNeo4jOp.addBoardgame(new BoardgameModelNeo4j(boardgameMongo.getId(), boardgameMongo.getBoardgameName(), boardgameMongo.getThumbnail(), boardgameMongo.getYearPublished()))) {
            logger.error("Error in adding the board game to Neo4j");
            if (!boardgameMongoOp.deleteBoardgame(boardgameMongo)) {
                logger.error("Error in deleting the board game from MongoDB");
            }
            return false;
        }

        return result;
    }

    public boolean deleteBoardgame(BoardgameModelMongo boardgame) {
        String boardgameName = boardgame.getBoardgameName();
        try {
            //--- Deleting reviews ---
            if (!deleteBoardgameReviews(boardgame))
                return false;

            //--- Deleting posts
            if (!deleteBoardgamePosts(boardgameName))
                return false;

            //--- Deleting From MONGO DB ---
            if (!boardgameMongoOp.deleteBoardgame(boardgame)) {
                logger.error("Error in deleting the Board Game from MongoDB");
                return false;
            }
            //--- Deleting From NEO4J DB ---
            if (!boardgameNeo4jOp.deleteBoardgameDetach(boardgameName)) {
                logger.error("Error in deleting the board game and its relationship from Neo4j");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean deleteBoardgameReviews(BoardgameModelMongo boardgame) {
        // Delete reviews in user collection
        for (ReviewModelMongo review : boardgame.getReviews()) {
            UserModelMongo user = (UserModelMongo) userMongoOp.findByUsername(review.getUsername()).get();
            user.deleteReview(review.getId());
            if (!userMongoOp.updateUser(user.getId(), user, "user")) {
                logger.error("Error in deleting reviews about boardgame in user collection");
                return false;
            }
        }

        // delete reviews in their own collection
        if (!reviewMongoOp.deleteReviewByBoardgameName(boardgame.getBoardgameName())) {
            logger.error("Error in deleting reviews about boardgame");
            return false;
        }
        return true;
    }

    private boolean deleteBoardgamePosts(String boardgameName) {
        List<PostModelMongo> posts = postMongoOp.findByTag(boardgameName);

        // delete comments
        for (PostModelMongo post: posts) {
            if (!commentMongoOp.deleteByPost(post.getId())) {
                logger.error("Error in deleting comments in posts about boardgame in MongoDB");
                return false;
            }
            if (!commentNeo4jOp.deleteByPost(post.getId())) {
                logger.error("Error in deleting comments in posts about boardgame in Neo4j");
                return false;
            }
        }

        // Delete posts
        if (!postMongoOp.deleteByTag(boardgameName)) {
            logger.error("Error in deleting posts about boardgame in MongoDB");
            return false;
        }
        if (!postNeo4jOp.deleteByReferredBoardgame(boardgameName)) {
            logger.error("Error in deleting posts about boardgame in Neo4j");
            return false;
        }
        return true;
    }

    public boolean updateBoardgame(BoardgameModelMongo boardgameMongo) {
        try {

            String boardgameId = boardgameMongo.getId();
            String boardgameName = boardgameMongo.getBoardgameName();
            String image = boardgameMongo.getImage();
            int yearPublished = boardgameMongo.getYearPublished();

            BoardgameModelNeo4j boardgameNeo4j = new BoardgameModelNeo4j(boardgameId,
                    boardgameName,
                    image, yearPublished);

            //Gestione MongoDB
            if (!boardgameMongoOp.updateBoardgameMongo(boardgameId, boardgameMongo)) {
                logger.error("Error in updating the Board Game in MongoDB");
                return false;
            }

            //Gestione Neo4j (NEW)
            if(!boardgameNeo4jOp.updateBoardgameNeo4j(boardgameId, boardgameNeo4j)){
                logger.error("Error in updating the Board Game on Neo4j");
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
