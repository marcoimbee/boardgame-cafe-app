package it.unipi.dii.lsmsdb.boardgamecafe.services;

//import it.unipi.dii.lsmsdb.phoneworld.App;
//import it.unipi.dii.lsmsdb.phoneworld.model.Phone;
//import it.unipi.dii.lsmsdb.phoneworld.repository.mongo.PhoneMongo;
//import it.unipi.dii.lsmsdb.phoneworld.repository.mongo.ReviewMongo;

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
    private BoardgameDBNeo4j boardgameNeoOp;
    @Autowired
    private ReviewDBMongo reviewMongoOp;


    private final static Logger logger = LoggerFactory.getLogger(BoardgameService.class);

    public boolean deleteBoardgame(BoardgameModelMongo boardgame) {

        String boardgameName = boardgame.getBoardgameName();
        String boardgameIdId = boardgame.getBoardgameId();
        try {

            //--- Deleting From MONGO DB ---
            if (!boardgameMongoOp.deleteBoardgame(boardgame)) {
                logger.error("Error in deleting the phone from MongoDB");
                return false;
            }
            //--- Deleting From NEO4J DB ---
            if (!boardgameNeoOp.deleteBoardgameDetach(boardgameName)) {
                logger.error("Error in deleting the board game and its relationship from Neo4j");
                return false;
            }
            //--- Deleting From MONGO DB ---
            if (!reviewMongoOp.deleteReviewByBoardgameName(boardgameName)) {
                logger.error("Error in deleting the reviews of the phone from the reviews collection");
                return false;
            }

            /*
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

    public boolean insertBoardgame(BoardgameModelMongo boardgameMongo, BoardgameModelNeo4j boardgameNeo4j) {

        boolean result = true;

        // Gestione MongoDB
        if (!boardgameMongoOp.addBoardgame(boardgameMongo)) {
            logger.error("Error in adding the user to MongoDB");
            return false;
        }

        // Gestione Neo4j
        if (!boardgameNeoOp.addBoardgame(boardgameNeo4j)) {
            logger.error("Error in adding the board game to Neo4j");
            if (!boardgameMongoOp.deleteBoardgame(boardgameMongo)) {
                logger.error("Error in deleting the board game from MongoDB");
            }
            return false;
        }

        return result;
    }

}
