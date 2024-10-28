package it.unipi.dii.lsmsdb.boardgamecafe.services;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.CommentModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.*;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.BoardgameDBNeo4j;

import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.CommentDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
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

    @Transactional
    public boolean insertBoardgame(BoardgameModelMongo boardgameMongo) {

        try{
            // Gestione MongoDB
            BoardgameModelMongo insertedBoardgameMongo = boardgameMongoOp.addBoardgame(boardgameMongo);
            if (insertedBoardgameMongo == null) {
                throw new RuntimeException("\nError while inserting the new Boardgame into MongoDB.");
            }

            BoardgameModelNeo4j boardgameForNeo4j = new BoardgameModelNeo4j(
                    insertedBoardgameMongo.getId(), insertedBoardgameMongo.getBoardgameName(),
                    insertedBoardgameMongo.getThumbnail(),
                    insertedBoardgameMongo.getYearPublished());

            // Gestione Neo4j
            BoardgameModelNeo4j insertedBoardgameNeo4j = boardgameNeo4jOp.addBoardgame(boardgameForNeo4j);
            if (insertedBoardgameNeo4j == null) {
                // ROLLBACK: Eliminazione da mongo se non avviene il caricamento su neo4j
                if (!boardgameMongoOp.deleteBoardgame(insertedBoardgameMongo)) {
                    throw new RuntimeException ("\nError in deleting the boardgame from MongoDB");
                }
                throw new RuntimeException("\nError while inserting the new Boardgame " +
                                            "into Neo4j - Rollback for MongoDB Done!.");
            }

            System.out.println("\n\nNew Boardgame's ID (MongoDB): " + insertedBoardgameMongo.getId());
            System.out.println("\nNew Boardgame's ID (Neo4j): " + insertedBoardgameNeo4j.getId());

        } catch (RuntimeException e) {
            System.err.println("[ERROR] " + e.getMessage());
            return false;
        }

        return true;
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

    public List<BoardgameModelMongo> suggestBoardgamesWithPostsByFollowedUsers(String username) {
        try {
            // Get suggested boardgames' Ids from Neo4J
            List<String> suggestedBoardgamesId = boardgameNeo4jOp.
                    getBoardgamesWithPostsByFollowedUsers(username, 10);
            // Init empty list of BoardgameModelMongo objects
            List<BoardgameModelMongo> suggestedMongoBoardgames = new ArrayList<>();
            // For each returned Neo4J ID
            for (String boardgameId: suggestedBoardgamesId) {
                // Get Mongo object related to that ID
                Optional<BoardgameModelMongo> suggestedMongoBoardgame = boardgameMongoOp.
                        findBoardgameById(boardgameId);
                // If an object is found in Mongo matching such ID
                suggestedMongoBoardgame.ifPresent(
                        // Add it to the List that'll be returned
                        boardgameModelMongo -> suggestedMongoBoardgames.add(boardgameModelMongo)
                );
            }
            return suggestedMongoBoardgames;
        } catch (Exception e) {
            logger.error("ERROR: " + e.getMessage());
            return new ArrayList<>();       // Return empty list in case of exception
        }
    }
}
