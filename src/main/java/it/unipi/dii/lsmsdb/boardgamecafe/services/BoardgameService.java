package it.unipi.dii.lsmsdb.boardgamecafe.services;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.*;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.BoardgameDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.PostDBNeo4j;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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
    private PostDBNeo4j postNeo4jOp;
    @Autowired
    private PostDBMongo postMongoOp;

    @Transactional
    public boolean insertBoardgame(BoardgameModelMongo boardgameMongo) {
        try{
            String boardgameNameUsed = boardgameMongo.getBoardgameName();
            Optional<BoardgameModelMongo> boardgameNameToCompare = boardgameMongoOp.
                                                                   findBoardgameByName(boardgameNameUsed);
            // Check if the Boardgame already exists
            if (boardgameNameToCompare.isPresent()) {
                throw new RuntimeException("A boardgame with this name already exists in the database.");
            }

            // MongoDB management
            BoardgameModelMongo insertedBoardgameMongo = boardgameMongoOp.addBoardgame(boardgameMongo);
            if (insertedBoardgameMongo == null) {
                throw new RuntimeException("Error while inserting the new Boardgame into MongoDB.");
            }

            BoardgameModelNeo4j boardgameForNeo4j = BoardgameModelNeo4j.castBoardgameMongoInBoardgameNeo(insertedBoardgameMongo);

            // Neo4J management
            BoardgameModelNeo4j insertedBoardgameNeo4j = boardgameNeo4jOp.addBoardgame(boardgameForNeo4j);
            if (insertedBoardgameNeo4j == null) {
                if (!boardgameMongoOp.deleteBoardgame(insertedBoardgameMongo)) {        // Deleting from MongoDB if Neo4J insertion fails
                    throw new RuntimeException ("Error while deleting the boardgame from MongoDB");
                }
                throw new RuntimeException("Error while inserting the new Boardgame into Neo4J. Rolling back...");
            }

            return true;
        } catch (RuntimeException e) {
            System.err.println("[ERROR] insertBoardgame()@BoardgameService.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public boolean deleteBoardgame(BoardgameModelMongo boardgame) {
        String boardgameName = boardgame.getBoardgameName();
        try {
            // Deleting reviews both from MongoDB and Neo4j
            if (!deleteBoardgameReviews(boardgame)){
                throw new RuntimeException("Error while deleting reviews after a boardgame's deletion.");
            }

            // Deleting posts both from MongoDB and Neo4j
            if (!deleteBoardgamePosts(boardgameName)){
                throw new RuntimeException("Error while deleting posts after a boardgame's deletion.");
            }

            // Deleting boardgame from MongoDB
            if (!boardgameMongoOp.deleteBoardgame(boardgame)) {
                throw new RuntimeException("Error while deleting a boardgame from MongoDB.");
            }

            // Deleting boardgame from Neo4J
            if (!boardgameNeo4jOp.deleteBoardgameDetach(boardgameName)) {
                throw new RuntimeException("Error while deleting a boardgame from Neo4J.");
            }

            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] deleteBoardgame()@BoardgameService.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    private boolean deleteBoardgameReviews(BoardgameModelMongo boardgame) {
        // Delete reviews in their own collection
        if (!reviewMongoOp.deleteReviewByBoardgameName(boardgame.getBoardgameName())) {
            return false;
        }
        return true;
    }

    private boolean deleteBoardgamePosts(String boardgameName) {
        List<PostModelMongo> posts = postMongoOp.findByTag(boardgameName);
        if (!posts.isEmpty()) {
            // Delete posts from MongoDB
            if (!postMongoOp.deleteByTag(boardgameName)) {
                return false;
            }
            // Delete posts from Neo4J and all their relationships
            if (!postNeo4jOp.deleteByReferredBoardgame(boardgameName)) {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public boolean updateBoardgame(BoardgameModelMongo boardgameMongo, String oldBoardgameName) {
        try {
            String boardgameId = boardgameMongo.getId();
            String boardgameName = boardgameMongo.getBoardgameName();
            String boardgameImage = boardgameMongo.getImage();
            String boardgameDescription = boardgameMongo.getDescription();
            int boardgameYearPublished = boardgameMongo.getYearPublished();

            BoardgameModelNeo4j boardgameNeo4j = new BoardgameModelNeo4j(
                                                     boardgameId,
                                                     boardgameName,
                                                     boardgameImage,
                                                     boardgameDescription,
                                                     boardgameYearPublished);

            // MongoDB management
            if (!boardgameMongoOp.updateBoardgameMongo(boardgameId, boardgameMongo)) {
                throw new RuntimeException("Error while updating a boardgame in MongoDB.");
            }

            // Editing the reviews related to the boardgame - only do this if the boardgame name was updated
            if (!boardgameName.equals(oldBoardgameName)) {
                if (!reviewMongoOp.updateReviewsAfterBoardgameUpdate(oldBoardgameName, boardgameName)) {
                    throw new RuntimeException("Error while updating the reviews of the updated boardgame in MongoDB.");
                }
                if (!postMongoOp.updatePostsAfterBoardgameUpdate(oldBoardgameName, boardgameName)) {
                    throw new RuntimeException("Error while updating the posts of the updated boardgame in MongoDB.");
                }

            }

            // Neo4j management
            if(!boardgameNeo4jOp.updateBoardgameNeo4j(oldBoardgameName, boardgameNeo4j)){
                throw new RuntimeException("Error while updating a boardgame in Neo4J.");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] updateBoardgame()@BoardgameService.java raised an exception: " + e.getMessage());
            throw new RuntimeException("Error while updating a boardgame in Neo4J.");
        }
        return true;
    }

    public List<BoardgameModelNeo4j> suggestBoardgamesWithPostsByFollowedUsers(String username, int skipCounter) {
        try {
            // Get suggested boardgames' Ids from Neo4J
            List<BoardgameModelNeo4j> suggestedBoardgames = boardgameNeo4jOp.
                    getBoardgamesWithPostsByFollowedUsers(username, 10, skipCounter);

            return suggestedBoardgames;
        } catch (Exception e) {
            System.err.println("[ERROR] suggestBoardgamesWithPostsByFollowedUsers()@BoardgameService.java raised an exception: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
