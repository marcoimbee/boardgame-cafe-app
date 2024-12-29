package it.unipi.dii.lsmsdb.boardgamecafe.services;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.CommentModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.*;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.BoardgameDBNeo4j;

import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.CommentDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import jakarta.transaction.Transactional;
import org.bson.Document;
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
//    @Autowired
//    private CommentDBMongo commentMongoOp;

    private final static Logger logger = LoggerFactory.getLogger(BoardgameService.class);

    @Transactional
    public boolean insertBoardgame(BoardgameModelMongo boardgameMongo) {

        try{
            String boardgameNameUsed = boardgameMongo.getBoardgameName();
            Optional<BoardgameModelMongo> boardgameNameToCompare = boardgameMongoOp.
                                                                   findBoardgameByName(boardgameNameUsed);
            // Check if the Boardgame already exists
            if (boardgameNameToCompare.isPresent()) {
                throw new RuntimeException("InsertBoardgame Exception: Boardgame not added.\n" +
                                           "A Board-Game with this name already exists in the database. " +
                                           "Change the name!");
            }
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

    @Transactional
    public boolean deleteBoardgame(BoardgameModelMongo boardgame) {

        String boardgameName = boardgame.getBoardgameName();

        try {
            //--- Deleting reviews both from MongoDB and Neo4j ---
            if (!deleteBoardgameReviews(boardgame)){
                throw new RuntimeException("\nError while deleting the review from User list.");
            }

            //--- Deleting posts both from MongoDB and Neo4j ---
            if (!deleteBoardgamePosts(boardgameName)){
                throw new RuntimeException("\nError while deleting the post from Boardgame list.");
            }

            //--- Deleting Boardgame From MONGO DB ---
            if (!boardgameMongoOp.deleteBoardgame(boardgame)) {
                throw new RuntimeException("\nError while deleting the Boargame from MongoDB.");
            }
            //--- Deleting Boardgame From NEO4J DB ---
            if (!boardgameNeo4jOp.deleteBoardgameDetach(boardgameName)) {
                throw new RuntimeException("\nError while deleting the Board Game from Neo4j.");
            }
            System.out.println("\nBoardgame Deleted from MongoDB and Neo4j");

        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            return false;
        }

        return true;
    }

    private boolean deleteBoardgameReviews(BoardgameModelMongo boardgame) {

        List<ReviewModelMongo> boardgameReviewsList = boardgame.getReviews();
        if (boardgameReviewsList.isEmpty())
        {
            System.out.println("\nThere are no REVIEWS to eliminate for this boardgame");
        } else {
            // Delete reviews in user collection
            for (ReviewModelMongo review : boardgameReviewsList) {
                UserModelMongo user = (UserModelMongo) userMongoOp.findByUsername(review.getUsername(), false).get();
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
            System.out.println("\nReviews regarding the Boardgame elminated " +
                                  "both from Mongo DB and Neo4j, also from its related Authors");
        }
        return true;
    }

    private boolean deleteBoardgamePosts(String boardgameName) {

        List<PostModelMongo> posts = postMongoOp.findByTag(boardgameName);
        if (posts.isEmpty())
        {
            System.out.println("\nThere are no POSTS to eliminate for this boardgame");
        } else {

            // --- Delete Comments from their own mongo collection and from neo4j based on post ---
            for (PostModelMongo post : posts) {
//                if (!commentMongoOp.deleteByPost(post.getId())) {
//                    logger.error("Error in deleting comments in posts about boardgame in MongoDB");
//                    return false;
//                }
                if (!commentNeo4jOp.deleteByPost(post.getId())) {
                    logger.error("Error in deleting comments in posts about boardgame in Neo4j");
                    return false;
                }
            }

            // --- Delete Posts from Mongo DB ---
            if (!postMongoOp.deleteByTag(boardgameName)) {
                logger.error("Error in deleting posts about boardgame in MongoDB");
                return false;
            }
            // --- Delete Posts from Neo4j and all its relationships ---
            if (!postNeo4jOp.deleteByReferredBoardgame(boardgameName)) {
                logger.error("Error in deleting posts about boardgame in Neo4j");
                return false;
            }
            System.out.println("\nPosts and related Comments regarding the Boardgame" +
                                  " elminated both from Mongo DB and Neo4j");
        }
        return true;
    }

    @Transactional
    public boolean updateBoardgame(BoardgameModelMongo boardgameMongo) {
        try {

            String boardgameId = boardgameMongo.getId();
            String boardgameName = boardgameMongo.getBoardgameName();
            String thumbnail = boardgameMongo.getThumbnail();
            int yearPublished = boardgameMongo.getYearPublished();

            BoardgameModelNeo4j boardgameNeo4j = new BoardgameModelNeo4j(
                                                     boardgameId,
                                                     boardgameName,
                                                     thumbnail,
                                                     yearPublished);

            //Gestione MongoDB
            if (!boardgameMongoOp.updateBoardgameMongo(boardgameId, boardgameMongo)) {
                throw new RuntimeException("\nError in updating the Board Game in MongoDB.");
            }

            //Gestione Neo4j (NEW)
            if(!boardgameNeo4jOp.updateBoardgameNeo4j(boardgameId, boardgameNeo4j)){
                throw new RuntimeException("\nError in updating the Board Game on Neo4j.");
            }

        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            return false;
        }
        return true;
    }

    public List<BoardgameModelMongo> suggestBoardgamesWithPostsByFollowedUsers(String username, int skipCounter) {
        try {
            // Get suggested boardgames' Ids from Neo4J
            List<String> suggestedBoardgamesId = boardgameNeo4jOp.
                    getBoardgamesWithPostsByFollowedUsers(username, 10, skipCounter);
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
