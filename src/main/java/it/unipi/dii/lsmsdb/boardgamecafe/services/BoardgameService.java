package it.unipi.dii.lsmsdb.boardgamecafe.services;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.BoardgameDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.ReviewDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.BoardgameDBNeo4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BoardgameService {

    @Autowired
    private BoardgameDBMongo boardgameMongoOp;
    @Autowired
    private BoardgameDBNeo4j boardgameNeo4jOp;
    @Autowired
    private ReviewDBMongo reviewMongoOp;

    private final static Logger logger = LoggerFactory.getLogger(BoardgameService.class);


    public boolean insertBoardgame(BoardgameModelMongo boardgameMongo,
                                   BoardgameModelNeo4j boardgameNeo4j) {
        boolean result = true;

        // Gestione MongoDB
        if (!boardgameMongoOp.addBoardgame(boardgameMongo)) {
            logger.error("Error in adding the user to MongoDB");
            return false;
        }

        // Gestione Neo4j
        if (!boardgameNeo4jOp.addBoardgame(boardgameNeo4j)) {
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
        String boardgameIdId = boardgame.getBoardgameId();
        try {

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
            //--- Deleting From MONGO DB ---
            if (!reviewMongoOp.deleteReviewByBoardgameName(boardgameName)) {
                logger.error("Error in deleting the reviews of the Board Game from the reviews collection");
                return false;
            }

            /*
            Gestione Neo4j (OLD)
            if (!App.getInstance().getPhoneNeo4j().deletePhoneRelationships(phoneId)) {
                logger.error("Error in deleting phone's relationships");
                return false;
            }
            if (!App.getInstance().getPhoneNeo4j().deletePhoneOnly(phoneId)) {
                logger.error("Error in deleting the phone from Neo4j");
                return false;
            }
            */

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    public boolean updateBoardgame(BoardgameModelMongo boardgameMongo) {
        try {

            String boardgameId = boardgameMongo.getBoardgameId();
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

            /*
            Gestione Neo4j (OLD)
            if (!App.getInstance().getPhoneNeo4j().updatePhone(phoneId, brand, picture, releaseYear)) {
                logger.error("Error in updating the phone on Neo4j");
                return false;
            }
            */
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
